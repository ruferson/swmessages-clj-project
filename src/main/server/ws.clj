(ns server.ws
  (:require [org.httpkit.server :as httpkit]))

;; Set of active websocket channels
(defonce active-channels (atom #{}))

(defn websocket-handler [request]
  (httpkit/as-channel request
                      {:on-open (fn [ch]
                                  (println "📡 New terminal connected to the HoloNet channel")
                                  (swap! active-channels conj ch))

                       :on-close (fn [ch status]
                                   (println "🔌 Terminal disconnected:" status)
                                   (swap! active-channels disj ch))

                       :on-receive (fn [ch raw-message]
                                     (println "📩 Message received on frequency:" raw-message)
                                     ;; Re-broadcast the message to all other connected clients
                                     (doseq [channel @active-channels]
                                       (when-not (= channel ch)
                                         (httpkit/send! channel raw-message))))}))
