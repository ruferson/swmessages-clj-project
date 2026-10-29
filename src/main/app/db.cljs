(ns app.db)

(def default-db
  {:authenticated? false
   :current-user nil
   :active-chat-id :c1
   :chats {:c1 {:id :c1
                :contact "Captain Panaka"
                :initials "CP"
                :photo-url nil
                :status "online"
                :messages [{:id 1 :sender "Captain Panaka" :text "Rubén, the Nubian shields are operational. Shall we begin transmission?"}
                           {:id 2 :sender "You" :text "HoloNet link established. The console is responding perfectly."}]}
           :c2 {:id :c2
                :contact "R2 Unit"
                :initials "R2"
                :photo-url nil
                :status "online"
                :messages [{:id 1 :sender "R2 Unit" :text "[Secure link frequency configured]"}]}}
   :auth-form {:mode :login
               :name ""
               :email ""
               :password ""
               :password-confirmation ""
               :rank "Starfighter Pilot"
               :photo-url nil}
   :input-text ""
   :friends []
   :friend-requests []
   :sent-friend-requests []
   :friend-id-input ""
   :sending-friend-request? false
   :loading-friend-requests? false
   :loading-friends? false
   :settings-open? false
   :active-chat-messages []})