(ns server.core
  (:require [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [ring.middleware.cors :refer [wrap-cors]]
            [server.auth :as auth]
            [server.friendships :as friendships]
            [server.ws :as ws]))

(def app-router
  (ring/ring-handler
   (ring/router
    [["/api"
      ["/register" {:post (fn [{:keys [body-params]}]
                            (auth/register-user body-params))}]
       ["/login" {:post (fn [{:keys [body-params]}]
                          (auth/login-user body-params))}]
       ["/friendship/send" {:post (fn [{:keys [body-params]}]
                                   (friendships/send-friend-request body-params))}]
       ["/friendship/respond" {:post (fn [{:keys [body-params]}]
                                   (friendships/respond-friend-request body-params))}]
       ["/friendship/users/:user-id/requests" {:get (fn [{:keys [path-params]}]
                                                (friendships/get-friend-requests (get path-params :user-id)))}]
       ["/friendship/users/:user-id/friends" {:get (fn [{:keys [path-params]}]
                                   (friendships/get-friends (get path-params :user-id)))}]
       ["/ws" {:get ws/websocket-handler}]]]
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))

;; CORS wrapper to allow connections from the client dev server only
(def app
  (wrap-cors app-router
             :access-control-allow-origin ["http://localhost:8080"]
             :access-control-allow-methods [:get :post :put :delete :options]
             :access-control-allow-headers ["Content-Type" "Authorization"]))
