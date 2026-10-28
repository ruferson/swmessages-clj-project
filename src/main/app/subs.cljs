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

;; List of friends (contacts) of the current user
(rf/reg-sub
 :friends
 (fn [db _]
   (:friends db)))

;; Pending incoming friend requests of the current user
(rf/reg-sub
 :friend-requests
 (fn [db _]
   (:friend-requests db)))

;; Id the user wants to add as a friend
(rf/reg-sub
 :friend-id-input
 (fn [db _]
   (:friend-id-input db)))

;; Is a friend request being sent?
(rf/reg-sub
 :sending-friend-request?
 (fn [db _]
   (:sending-friend-request? db)))

;; Are friend requests being loaded?
(rf/reg-sub
 :loading-friend-requests?
 (fn [db _]
   (:loading-friend-requests? db)))

;; Is the settings menu open?
(rf/reg-sub
 :settings-open?
 (fn [db _]
   (:settings-open? db)))
