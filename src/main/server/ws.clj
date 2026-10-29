(ns server.ws
  (:require [clojure.data.json :as json]
            [org.httpkit.server :as httpkit])
  (:import [java.sql DriverManager ResultSet]))

(def db-url "jdbc:sqlite:holonet_users.db")
(defonce ws-db (atom nil))

;; Initializes or retrieves the SQLite database connection for persistent messages
(defn- db []
  (or @ws-db
      (let [conn (DriverManager/getConnection db-url "sa" "")]
        (with-open [stmt (.prepareStatement conn "CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender_id TEXT NOT NULL, recipient_id TEXT NOT NULL, text TEXT NOT NULL, timestamp INTEGER NOT NULL)")]
          (.executeUpdate stmt))
        (reset! ws-db conn)
        conn)))

;; Maps WebSocket channel instances to User IDs and vice versa
(defonce channel->user (atom {}))
(defonce user->channels (atom {}))

;; Inserts a new chat message record into SQLite database
(defn save-message! [sender-id recipient-id text timestamp]
  (with-open [stmt (.prepareStatement (db) "INSERT INTO messages (sender_id, recipient_id, text, timestamp) VALUES (?, ?, ?, ?)")]
    (.setString stmt 1 (str sender-id))
    (.setString stmt 2 (str recipient-id))
    (.setString stmt 3 (str text))
    (.setLong stmt 4 timestamp)
    (.executeUpdate stmt)))

;; Queries SQLite to retrieve all messages exchanged between two operators ordered by timestamp
(defn fetch-chat-history [user-a user-b]
  (with-open [stmt (.prepareStatement (db) "SELECT * FROM messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) ORDER BY timestamp ASC")]
    (.setString stmt 1 (str user-a))
    (.setString stmt 2 (str user-b))
    (.setString stmt 3 (str user-b))
    (.setString stmt 4 (str user-a))
    (let [rs (.executeQuery stmt)]
      (loop [msgs []]
        (if (.next rs)
          (recur (conj msgs {:id (.getLong rs "id")
                             :sender-id (.getString rs "sender_id")
                             :recipient-id (.getString rs "recipient_id")
                             :text (.getString rs "text")
                             :timestamp (.getLong rs "timestamp")}))
          msgs)))))

;; HTTP handler returning chat history JSON response
(defn get-history-handler [user-a user-b]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:success true
                          :messages (fetch-chat-history user-a user-b)})})

;; WebSocket handler managing channel connections, operator registration, and live message routing
(defn websocket-handler [request]
  (httpkit/as-channel request
                      {:on-open (fn [ch]
                                  (println "New terminal connected to HoloNet frequency"))

                       :on-close (fn [ch status]
                                   (when-let [uid (get @channel->user ch)]
                                     (swap! user->channels update uid disj ch)
                                     (swap! channel->user dissoc ch)
                                     (println "Terminal disconnected:" uid)))

                       :on-receive (fn [ch raw-message]
                                     (try
                                       (let [payload (json/read-str raw-message :key-fn keyword)
                                             {:keys [type user-id sender-id recipient-id text timestamp]} payload]
                                         (case type
                                           ;; Socket registration event following user authentication
                                           "register"
                                           (let [clean-uid (str user-id)]
                                             (swap! channel->user assoc ch clean-uid)
                                             (swap! user->channels update clean-uid (fnil conj #{}) ch)
                                             (println "Channel registered for operator:" clean-uid))

                                           ;; Direct message dispatching event
                                           "message"
                                           (let [ts (or timestamp (System/currentTimeMillis))
                                                 clean-sender (str sender-id)
                                                 clean-recipient (str recipient-id)]
                                             ;; 1. Save to SQLite
                                             (save-message! clean-sender clean-recipient text ts)

                                             (let [msg-payload (json/write-str {:type "message"
                                                                                :sender-id clean-sender
                                                                                :recipient-id clean-recipient
                                                                                :text text
                                                                                :timestamp ts})]
                                               ;; 2. Broadcast to all active sockets of the recipient
                                               (doseq [target-ch (get @user->channels clean-recipient)]
                                                 (httpkit/send! target-ch msg-payload))

                                               ;; 3. Echo confirmation to the sender
                                               (httpkit/send! ch msg-payload)))))
                                       (catch Exception e
                                         (println "Error processing WebSocket HoloMessage:" (.getMessage e)))))}))