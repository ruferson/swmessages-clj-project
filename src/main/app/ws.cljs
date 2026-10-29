(ns app.ws
  (:require [re-frame.core :as rf]))

(defonce socket (atom nil))

;; Explicitly closes the existing WebSocket connection
(defn disconnect! []
  (when-let [ws @socket]
    (.close ws)
    (reset! socket nil)
    (println "Closed existing HoloNet WebSocket connection")))

;; Connects to the HoloNet WebSocket server and registers the operator ID
(defn connect! [user-id]
  ;; Disconnect any active channel first to prevent duplicate listeners
  (disconnect!)
  (let [ws (js/WebSocket. "ws://localhost:3000/api/ws")]
    (set! (.-onopen ws)
          (fn []
            (println "Connected to HoloNet WebSocket channel")
            (.send ws (js/JSON.stringify (clj->js {:type "register" :user-id user-id})))))

    (set! (.-onmessage ws)
          (fn [event]
            (let [data (js->clj (js/JSON.parse (.-data event)) :keywordize-keys true)]
              (when (= (:type data) "message")
                (rf/dispatch [:receive-ws-message data])))))

    (set! (.-onclose ws)
          (fn []
            (println "Disconnected from HoloNet frequency")
            (when (= @socket ws)
              (reset! socket nil))))

    (reset! socket ws)))

;; Sends a direct message payload through the open WebSocket connection
(defn send-ws-message! [sender-id recipient-id text]
  (when-let [ws @socket]
    (when (= (.-readyState ws) js/WebSocket.OPEN)
      (.send ws (js/JSON.stringify (clj->js {:type "message"
                                             :sender-id sender-id
                                             :recipient-id recipient-id
                                             :text text}))))))