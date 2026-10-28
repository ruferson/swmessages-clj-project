(ns app.subs
  (:require [re-frame.core :as rf]))

;; General list of chats for the sidebar
(rf/reg-sub
 :chats
 (fn [db _]
   (vals (:chats db))))

;; Id of the currently active chat
(rf/reg-sub
 :active-chat-id
 (fn [db _]
   (:active-chat-id db)))

;; Data of the active chat
(rf/reg-sub
 :active-chat
 (fn [db _]
   (get-in db [:chats (:active-chat-id db)])))

;; Messages of the active chat
(rf/reg-sub
 :active-messages
 (fn [db _]
   (rf/subscribe [:active-chat]))
 (fn [chat _]
   (:messages chat)))

;; Text of the input field
(rf/reg-sub
 :input-text
 (fn [db _]
   (:input-text db)))

;; Is the user authenticated?
(rf/reg-sub
 :authenticated?
 (fn [db _]
   (:authenticated? db)))

;; Data of the currently logged-in user
(rf/reg-sub
 :current-user
 (fn [db _]
   (:current-user db)))

;; Authentication form
(rf/reg-sub
 :auth-form
 (fn [db _]
   (:auth-form db)))

;; Authentication error
(rf/reg-sub
 :auth-error
 (fn [db _]
   (:auth-error db)))

;; Is the authentication request in progress?
(rf/reg-sub
 :loading-auth?
 (fn [db _]
   (:loading-auth? db)))
