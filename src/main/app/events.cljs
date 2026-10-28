(ns app.events
  (:require [app.db :as db]
            [clojure.string :as str]
            [day8.re-frame.http-fx] ;; Enables the :http-xhrio effect
            [ajax.core :as ajax]
            [re-frame.core :as rf]))

;; Loads the initial database of the application
(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

;; Updates the currently active chat
(rf/reg-event-db
 :select-chat
 (fn [db [_ chat-id]]
   (assoc db :active-chat-id chat-id)))

;; Updates the text of the message input field
(rf/reg-event-db
 :update-input
 (fn [db [_ text]]
   (assoc db :input-text text)))

;; Sends a new message to the active chat
(rf/reg-event-db
 :send-message
 (fn [db _]
   (let [text (:input-text db)
         chat-id (:active-chat-id db)]
     (if-not (str/blank? text)
       (-> db
           (update-in [:chats chat-id :messages] (fnil conj []) {:id (.now js/Date)
                                                                 :sender "Tú"
                                                                 :text text})
           (assoc :input-text ""))
       db))))

;; Switches the authentication form between login and register modes
(rf/reg-event-db
 :change-auth-mode
 (fn [db [_ mode]]
   (-> db
       (assoc-in [:auth-form :mode] mode)
       (assoc :auth-error nil))))

;; Updates a single field of the authentication form
(rf/reg-event-db
 :update-auth-field
 (fn [db [_ field value]]
   (assoc-in db [:auth-form field] value)))

;; Stores the profile photo chosen by the user
(rf/reg-event-db
 :save-profile-photo
 (fn [db [_ data-url]]
   (assoc-in db [:auth-form :photo-url] data-url)))

;; --- AUTHENTICATION REQUESTS TO THE BACKEND ---

(rf/reg-event-fx
 :login
 (fn [{:keys [db]} _]
   (let [{:keys [mode name email password password-confirmation rank photo-url]} (:auth-form db)
         is-registration? (= mode :register)
         uri (if is-registration?
               "http://localhost:3000/api/register"
               "http://localhost:3000/api/login")
         body (if is-registration?
                {:name name
                 :email email
                 :password password
                 :password-confirmation password-confirmation
                 :rank rank
                 :photo-url photo-url}
                {:email email
                 :password password})]
     {:db (assoc db :loading-auth? true :auth-error nil)
      :http-xhrio {:method          :post
                   :uri             uri
                   :params          body
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:auth-success]
                   :on-failure      [:auth-failure]}})))

;; Event fired when authentication (login or register) succeeds
(rf/reg-event-fx
 :auth-success
 (fn [{:keys [db]} [_ response]]
   (let [user (get response :user)
         user-id (:id user)]
     {:db (-> db
              (assoc :loading-auth? false)
              (assoc :authenticated? true)
              (assoc :current-user user)
              (assoc-in [:auth-form :password] "")
              (assoc-in [:auth-form :password-confirmation] ""))
      :dispatch-n [[:load-friend-requests user-id]
                   [:load-friends user-id]]})))

;; Event fired when authentication fails
(rf/reg-event-db
 :auth-failure
 (fn [db [_ response]]
   (let [error-message (get-in response [:response :message] "Error communicating with the server")]
     (-> db
         (assoc :loading-auth? false)
         (assoc :auth-error error-message)))))

(rf/reg-event-db
 :logout
 (fn [db _]
   (-> db
       (assoc :authenticated? false)
       (assoc :current-user nil))))

;; --- FRIEND REQUESTS AND FRIENDS ---

;; Toggles the settings menu visibility
(rf/reg-event-db
 :toggle-settings
 (fn [db _]
   (update db :settings-open? not)))

;; Updates the ID of the friend the user wants to add
(rf/reg-event-db
 :update-friend-id
 (fn [db [_ value]]
   (assoc db :friend-id-input value)))

;; Sends a friend request to the backend
(rf/reg-event-fx
 :send-friend-request
 (fn [{:keys [db]} _]
   (let [current-user-id (get-in db [:current-user :id])
         target-id (str (:friend-id-input db))]
     {:db (assoc db :sending-friend-request? true)
      :http-xhrio {:method          :post
                   :uri             "http://localhost:3000/api/friendship/send"
                   :params          {:from-id current-user-id :to-id target-id}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:friend-request-sent]
                   :on-failure      [:friend-request-sent-failed]}})))

;; Handles the result of sending a friend request
(rf/reg-event-fx
 :friend-request-sent
 (fn [{:keys [db]} _]
   (let [user-id (get-in db [:current-user :id])]
     {:db (assoc db :sending-friend-request? false :friend-id-input "")
      :dispatch-n [[:load-friend-requests user-id]
                   [:load-friends user-id]]})))

(rf/reg-event-db
 :friend-request-sent-failed
 (fn [db _]
   (assoc db :sending-friend-request? false)))

;; Loads the pending friend requests of the current user
(rf/reg-event-fx
 :load-friend-requests
 (fn [{:keys [db]} [_ user-id]]
   (let [uid (or user-id (get-in db [:current-user :id]))]
     {:db (assoc db :loading-friend-requests? true)
      :http-xhrio {:method          :get
                   :uri             (str "http://localhost:3000/api/friendship/users/" uid "/requests")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:friend-requests-loaded]
                   :on-failure      [:friend-requests-loaded-failed]}})))

(rf/reg-event-db
 :friend-requests-loaded
 (fn [db [_ response]]
   (assoc db :friend-requests (or (get response :requests) [])
          :loading-friend-requests? false)))

(rf/reg-event-db
 :friend-requests-loaded-failed
 (fn [db _]
   (assoc db :friend-requests [] :loading-friend-requests? false)))

;; Loads the friends (contacts) of the current user
(rf/reg-event-fx
 :load-friends
 (fn [{:keys [db]} [_ user-id]]
   (let [uid (or user-id (get-in db [:current-user :id]))]
     {:db (assoc db :loading-friends? true)
      :http-xhrio {:method          :get
                   :uri             (str "http://localhost:3000/api/friendship/users/" uid "/friends")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:friends-loaded]
                   :on-failure      [:friends-loaded-failed]}})))

(rf/reg-event-db
 :friends-loaded
 (fn [db [_ response]]
   (assoc db :friends (or (get response :friends) [])
          :loading-friends? false)))

(rf/reg-event-db
 :friends-loaded-failed
 (fn [db _]
   (assoc db :friends [] :loading-friends? false)))

;; Responds to a friend request (accept or reject)
(rf/reg-event-fx
 :respond-friend-request
 (fn [{:keys [db]} [_ user-id from-id accept]]
   {:db (assoc db :sending-friend-request? true)
    :http-xhrio {:method          :post
                 :uri             "http://localhost:3000/api/friendship/respond"
                 :params          {:user-id user-id :from-id from-id :accept accept}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:friend-request-responded]
                 :on-failure      [:friend-request-responded-failed]}}))

(rf/reg-event-fx
 :friend-request-responded
 (fn [{:keys [db]} _]
   (let [user-id (get-in db [:current-user :id])]
     {:db (assoc db :sending-friend-request? false)
      :dispatch-n [[:load-friend-requests user-id]
                   [:load-friends user-id]]})))

(rf/reg-event-db
 :friend-request-responded-failed
 (fn [db _]
   (assoc db :sending-friend-request? false)))