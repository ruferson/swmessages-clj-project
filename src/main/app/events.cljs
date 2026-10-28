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
           (update-in [:chats chat-id :messages] conj {:id (js/Date.now)
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

;; Event fired when the authentication succeeds
(rf/reg-event-db
 :auth-success
 (fn [db [_ response]]
   (let [user (get response :user)]
     (-> db
         (assoc :loading-auth? false)
         (assoc :authenticated? true)
         (assoc :current-user user)
         (assoc-in [:auth-form :password] "")
         (assoc-in [:auth-form :password-confirmation] "")))))

;; Event fired when the authentication fails
(rf/reg-event-db
 :auth-failure
 (fn [db [_ response]]
   (let [error-message (get-in response [:response :message] "Error communicating with the HoloNet")]
     (-> db
         (assoc :loading-auth? false)
         (assoc :auth-error error-message)))))

(rf/reg-event-db
 :logout
 (fn [db _]
   (-> db
       (assoc :authenticated? false)
       (assoc :current-user nil))))
