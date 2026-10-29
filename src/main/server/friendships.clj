(ns server.friendships
  (:require [server.auth :as auth]
            [clojure.string :as str])
  (:import [java.sql DriverManager PreparedStatement ResultSet]))

(def db-url "jdbc:sqlite:holonet_users.db")

(defonce friendships-db (atom nil))

(defn- db []
  (or @friendships-db
      (let [conn (DriverManager/getConnection db-url "sa" "")]
        (with-open [stmt1 (.prepareStatement conn "CREATE TABLE IF NOT EXISTS friendships (user_a TEXT NOT NULL, user_b TEXT NOT NULL, PRIMARY KEY (user_a, user_b))")
                    stmt2 (.prepareStatement conn "CREATE TABLE IF NOT EXISTS friend_requests (from_id TEXT NOT NULL, to_id TEXT NOT NULL, PRIMARY KEY (from_id, to_id))")]
          (.executeUpdate stmt1)
          (.executeUpdate stmt2))
        (reset! friendships-db conn)
        conn)))

(def valid-id-pattern #"^op-\d+$")

(defn- valid-id? [id]
  (and (string? id)
       (not (str/blank? id))
       (re-matches valid-id-pattern id)))

(defn- require-valid-ids [from-id to-id]
  (when-not (and (valid-id? from-id) (valid-id? to-id))
    {:status 400
     :body {:success false
            :message "Invalid operator ID"}}))

(defn- clean-user [user]
  (-> user
      (dissoc :password)
      (dissoc :email)))

(defn are-friends? [user-a-id user-b-id]
  (with-open [stmt (.prepareStatement (db) "SELECT 1 FROM friendships WHERE user_a = ? AND user_b = ?")]
    (.setString stmt 1 user-a-id)
    (.setString stmt 2 user-b-id)
    (let [rs (.executeQuery stmt)]
      (.next rs))))

(defn has-pending-request? [from-id to-id]
  (with-open [stmt (.prepareStatement (db) "SELECT 1 FROM friend_requests WHERE from_id = ? AND to_id = ?")]
    (.setString stmt 1 from-id)
    (.setString stmt 2 to-id)
    (let [rs (.executeQuery stmt)]
      (.next rs))))

(defn send-friend-request [{:keys [from-id to-id]}]
  (let [validation (require-valid-ids from-id to-id)]
    (if validation
      validation
      (let [from-user (auth/fetch-user-by-id from-id)
            to-user (auth/fetch-user-by-id to-id)]
        (cond
          (nil? from-user)
          {:status 404 :body {:success false :message "Sender operator does not exist"}}

          (nil? to-user)
          {:status 404 :body {:success false :message "Recipient operator does not exist"}}

          (= from-id to-id)
          {:status 400 :body {:success false :message "You cannot request a link with yourself"}}

          (are-friends? from-id to-id)
          {:status 409 :body {:success false :message "Friendship link is already established"}}

          (has-pending-request? from-id to-id)
          {:status 409 :body {:success false :message "A link request is already pending for this operator"}}

          :else
          (do
            (with-open [stmt (.prepareStatement (db) "INSERT INTO friend_requests (from_id, to_id) VALUES (?, ?)")]
              (.setString stmt 1 from-id)
              (.setString stmt 2 to-id)
              (.executeUpdate stmt))
            {:status 200
             :body {:success true
                    :message "Link request sent successfully"}}))))))

;; Incoming requests (where to_id = user-id)
(defn get-friend-requests [user-id]
  (if (and (valid-id? user-id) (auth/fetch-user-by-id user-id))
    (with-open [stmt (.prepareStatement (db) "SELECT from_id FROM friend_requests WHERE to_id = ?")]
      (.setString stmt 1 user-id)
      (let [rs (.executeQuery stmt)]
        (loop [sender-ids []]
          (if (.next rs)
            (recur (conj sender-ids (.getString rs "from_id")))
            (let [sender-users (->> sender-ids
                                    (map auth/fetch-user-by-id)
                                    (filter some?)
                                    (map clean-user)
                                    vec)]
              {:status 200
               :body {:success true
                      :requests sender-users}})))))
    {:status 404
     :body {:success false
            :message "Operator not found"}}))

;; Outgoing requests (where from_id = user-id)
(defn get-sent-friend-requests [user-id]
  (if (and (valid-id? user-id) (auth/fetch-user-by-id user-id))
    (with-open [stmt (.prepareStatement (db) "SELECT to_id FROM friend_requests WHERE from_id = ?")]
      (.setString stmt 1 user-id)
      (let [rs (.executeQuery stmt)]
        (loop [recipient-ids []]
          (if (.next rs)
            (recur (conj recipient-ids (.getString rs "to_id")))
            (let [recipient-users (->> recipient-ids
                                       (map auth/fetch-user-by-id)
                                       (filter some?)
                                       (map clean-user)
                                       vec)]
              {:status 200
               :body {:success true
                      :sent-requests recipient-users}})))))
    {:status 404
     :body {:success false
            :message "Operator not found"}}))

(defn get-friends [user-id]
  (if (and (valid-id? user-id) (auth/fetch-user-by-id user-id))
    (with-open [stmt (.prepareStatement (db) "SELECT user_b FROM friendships WHERE user_a = ?")]
      (.setString stmt 1 user-id)
      (let [rs (.executeQuery stmt)]
        (loop [friend-ids []]
          (if (.next rs)
            (recur (conj friend-ids (.getString rs "user_b")))
            (let [friend-users (->> friend-ids
                                    (map auth/fetch-user-by-id)
                                    (filter some?)
                                    (map clean-user)
                                    vec)]
              {:status 200
               :body {:success true
                      :friends friend-users}})))))
    {:status 404
     :body {:success false
            :message "Operator not found"}}))

(defn respond-friend-request [{:keys [user-id from-id accept]}]
  (let [validation (require-valid-ids user-id from-id)
        user (auth/fetch-user-by-id user-id)
        sender (auth/fetch-user-by-id from-id)]
    (if validation
      validation
      (cond
        (or (nil? user) (nil? sender))
        {:status 404 :body {:success false :message "Operator not found"}}

        (not (has-pending-request? from-id user-id))
        {:status 404 :body {:success false :message "No pending link request exists from this operator"}}

        accept
        (do
          (with-open [stmt-del (.prepareStatement (db) "DELETE FROM friend_requests WHERE from_id = ? AND to_id = ?")
                      stmt-ins1 (.prepareStatement (db) "INSERT OR IGNORE INTO friendships (user_a, user_b) VALUES (?, ?)")
                      stmt-ins2 (.prepareStatement (db) "INSERT OR IGNORE INTO friendships (user_a, user_b) VALUES (?, ?)")]
            (.setString stmt-del 1 from-id)
            (.setString stmt-del 2 user-id)
            (.executeUpdate stmt-del)

            (.setString stmt-ins1 1 user-id)
            (.setString stmt-ins1 2 from-id)
            (.executeUpdate stmt-ins1)

            (.setString stmt-ins2 1 from-id)
            (.setString stmt-ins2 2 user-id)
            (.executeUpdate stmt-ins2))
          {:status 200 :body {:success true :message "Friendship link established successfully"}})

        :else
        (do
          (with-open [stmt-del (.prepareStatement (db) "DELETE FROM friend_requests WHERE from_id = ? AND to_id = ?")]
            (.setString stmt-del 1 from-id)
            (.setString stmt-del 2 user-id)
            (.executeUpdate stmt-del))
          {:status 200 :body {:success true :message "Link request rejected"}})))))