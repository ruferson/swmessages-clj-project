(ns app.core
  (:require [app.events]
            [app.subs]
            [app.views.auth :refer [auth-component]]
            [app.views.chat :refer [chat-component]]
            [app.views.sidebar :refer [sidebar-component]]
            [re-frame.core :as rf]
            [reagent.dom.client :as rdom]))

(defonce root (atom nil))

;; Main application layout
(defn app-component []
  (let [authenticated? @(rf/subscribe [:authenticated?])]
    [:div {:style {:display "flex"
                   :flex-direction "row"
                   :height "calc(100dvh - 24px)"
                   :width "calc(100vw - 24px)"
                   :max-width "1200px"
                   :max-height "900px"
                   :margin "12px auto"
                   :background "#fdfbf7"
                   :border "clamp(3px, 0.8vw, 6px) solid #dcd0bc"
                   :border-radius "clamp(16px, 3vw, 32px)"
                   :box-shadow "0 15px 40px rgba(180, 150, 90, 0.25), 0 0 0 2px #f0e6d2"
                   :overflow "hidden"
                   :box-sizing "border-box"}}
      (if authenticated?
        [:<>
         [sidebar-component]
         [chat-component]]
        [auth-component])]))

(defn render! []
  (let [container (.getElementById js/document "app")]
    (when-not @root
      (reset! root (rdom/create-root container)))
    (rdom/render @root [app-component])))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (render!))

(defn reload! []
  (render!))
