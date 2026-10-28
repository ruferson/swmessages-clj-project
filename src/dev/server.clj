(ns server
  (:require [org.httpkit.server :as httpkit]
            [server.core :refer [app]]))

(defonce server-instance (atom nil))

(defn start-server! [& {:keys [port] :or {port 3000}}]
  (when-not @server-instance
    (reset! server-instance (httpkit/run-server #'app {:port port}))
    (println (str "[SERVER] Server started listening on port " port))))

(defn stop-server! []
  (when-let [stop-fn @server-instance]
    (stop-fn)
    (reset! server-instance nil)
    (println "[SERVER] Server stopped.")))

(defn -main [& args]
  (start-server!))

(start-server!)
