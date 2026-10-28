(ns app.db)

(def default-db
  {:authenticated? false
   :current-user nil
   :active-chat-id :c1
   :chats {:c1 {:id :c1
                :contact "Capitán Panaka"
                :initials "CP"
                :photo-url nil
                :status "en línea"
                :messages [{:id 1 :sender "Capitán Panaka" :text "Rubén, los escudos del Nubian están listos. ¿Iniciamos la transmisión?"}
                            {:id 2 :sender "Tú" :text "Enlace HoloNet established. La consola responde perfectamente."}]}
             :c2 {:id :c2
                  :contact "Unidad R2"
                  :initials "R2"
                  :photo-url nil
                  :status "en línea"
                  :messages [{:id 1 :sender "Unidad R2" :text "[Frecuencia segura de enlace configurada]"}]}}
   :auth-form {:mode :login ;; :login or :register
               :name ""
               :email ""
               :password ""
               :password-confirmation ""
               :rank "Piloto de Caza"
               :photo-url nil}
   :input-text ""
   :friends []
   :friend-requests []
   :friend-id-input ""
   :sending-friend-request? false
   :loading-friend-requests? false
   :loading-friends? false
   :settings-open? false})
