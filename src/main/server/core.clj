(ns server.core
  (:require [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [ring.middleware.cors :refer [wrap-cors]]
            [server.auth :as auth]
            [server.ws :as ws]))

(def app-router
  (ring/ring-handler
   (ring/router
    [["/api"
      ["/register" {:post (fn [{:keys [body-params]}]
                            (auth/register-user body-params))}]
       ["/login" {:post (fn [{:keys [body-params]}]
                          (auth/login-user body-params))}]]
      ["/ws" {:get ws/websocket-handler}]]
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))

;; CORS wrapper to allow connections from the client dev server
(def app
  (wrap-cors app-router
             :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :post :put :delete :options]
             :access-control-allow-headers ["Content-Type" "Authorization"]))
