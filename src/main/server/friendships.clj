(ns server.friendships
  (:require [server.auth :as auth]
            [clojure.string :as str]))

;; Friendship store: user-id -> set of friend ids
(defonce friendships (atom {}))

;; Friend requests store: user-id (recipient) -> set of sender ids
(defonce friend-requests (atom {}))

;; Valid operator id format: "op-" followed by digits
(def valid-id-pattern #"^op-\d+$")

(defn- valid-id? [id]
  (and (string? id)
       (not (str/blank? id))
       (re-matches valid-id-pattern id)))

(defn- require-valid-ids [from-id to-id]
  (when-not (and (valid-id? from-id) (valid-id? to-id))
    {:status 400
     :body {:success false
            :message "ID de operador inválido"}}))

(defn- friends-of [user-id]
  (or (get @friendships user-id) #{}))

(defn- incoming-of [user-id]
  (or (get @friend-requests user-id) #{}))

;; Returns user data without sensitive fields (password, email)
(defn- clean-user [user]
  (-> user
      (dissoc :password)
      (dissoc :email)))

(defn are-friends? [user-a-id user-b-id]
  (contains? (friends-of user-a-id) user-b-id))

(defn send-friend-request [{:keys [from-id to-id]}]
  (let [validation (require-valid-ids from-id to-id)]
    (if validation
      validation
      (let [from-user (auth/fetch-user-by-id from-id)
            to-user (auth/fetch-user-by-id to-id)]
        (cond
          (nil? from-user)
          {:status 404 :body {:success false :message "El operador emisor no existe"}}

          (nil? to-user)
          {:status 404 :body {:success false :message "El operador receptor no existe"}}

          (= from-id to-id)
          {:status 400 :body {:success false :message "No puedes solicitar enlace contigo mismo"}}

          (are-friends? from-id to-id)
          {:status 409 :body {:success false :message "Ya tenéis enlace de amistad establecido"}}

          (contains? (incoming-of to-id) from-id)
          {:status 409 :body {:success false :message "Ya hay una solicitud de enlace pendiente enviada a este operador"}}

          :else
          (do
            (swap! friend-requests update to-id (fnil conj #{}) from-id)
            {:status 200
             :body {:success true
                    :message "Solicitud de enlace enviada correctamente"}}))))))

(defn get-friend-requests [user-id]
  (if (and (valid-id? user-id) (auth/fetch-user-by-id user-id))
    (let [sender-ids (vec (incoming-of user-id))
          sender-users (->> sender-ids
                            (map auth/fetch-user-by-id)
                            (filter some?)
                            (map clean-user)
                            vec)]
      {:status 200
       :body {:success true
              :requests sender-users}})
    {:status 404
     :body {:success false
            :message "Operador no encontrado"}}))

(defn get-friends [user-id]
  (if (and (valid-id? user-id) (auth/fetch-user-by-id user-id))
    (let [friend-ids (vec (friends-of user-id))
          friend-users (->> friend-ids
                            (map auth/fetch-user-by-id)
                            (filter some?)
                            (map clean-user)
                            vec)]
      {:status 200
       :body {:success true
              :friends friend-users}})
    {:status 404
     :body {:success false
            :message "Operador no encontrado"}}))

(defn respond-friend-request [{:keys [user-id from-id accept]}]
  (let [validation (require-valid-ids user-id from-id)
        user (auth/fetch-user-by-id user-id)
        sender (auth/fetch-user-by-id from-id)]
    (if validation
      validation
      (cond
        (or (nil? user) (nil? sender))
        {:status 404 :body {:success false :message "Operador no encontrado"}}

        (not (contains? (incoming-of user-id) from-id))
        {:status 404 :body {:success false :message "No existe solicitud de enlace pendiente de este operador"}}

        accept
        (do
          (swap! friend-requests update user-id (fnil disj #{}) from-id)
          (swap! friendships update user-id (fnil conj #{}) from-id)
          (swap! friendships update from-id (fnil conj #{}) user-id)
          {:status 200 :body {:success true :message "Enlace de amistad establecido correctamente"}})

        :else
        (do
          (swap! friend-requests update user-id (fnil disj #{}) from-id)
          {:status 200 :body {:success true :message "Solicitud de enlace rechazada"}})))))