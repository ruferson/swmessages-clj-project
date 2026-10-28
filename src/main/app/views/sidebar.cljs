(ns app.views.sidebar
  (:require [app.views.components :refer [avatar-badge]]
            [re-frame.core :as rf]))

(defn sidebar-component []
  (let [chats @(rf/subscribe [:chats])
        active-chat-id @(rf/subscribe [:active-chat-id])
        current-user @(rf/subscribe [:current-user])]
    [:div {:style {:width "clamp(200px, 30vw, 290px)"
                   :min-width "180px"
                   :background "linear-gradient(180deg, #fbf9f3 0%, #f1e7d0 100%)"
                   :border-right "2px solid #dfcfb2"
                   :box-shadow "inset -4px 0 12px rgba(212, 175, 55, 0.15)"
                   :display "flex"
                   :flex-direction "column"}}
     ;; HoloNet header
     [:div {:style {:padding "clamp(12px, 2vw, 20px) 14px"
                    :background "linear-gradient(135deg, #fffdf7 0%, #f3e5c8 100%)"
                    :border-bottom "2px solid #dcc498"
                    :color "#705315"
                    :font-family "Georgia, serif"
                    :font-weight "bold"
                    :letter-spacing "1px"
                    :display "flex"
                    :align-items "center"
                    :justify-content "space-between"}}
      [:div {:style {:display "flex" :align-items "center" :gap "6px"}}
       [:span {:style {:font-size "11px" :letter-spacing "2px" :font-family "monospace"}} "HOLONET // CHANNELS"]]
      [:span {:style {:font-size "9px"
                      :padding "2px 6px"
                      :background "#ffe082"
                      :border-radius "8px"
                      :color "#4a3800"
                      :font-family "monospace"
                      :font-weight "bold"}} "SEC-9"]]

     ;; Frequencies list
     [:div {:style {:flex "1" :overflow-y "auto" :padding "8px"}}
      (for [chat chats]
        (let [active-chat? (= (:id chat) active-chat-id)]
          ^{:key (:id chat)}
          [:div {:on-click #(rf/dispatch [:select-chat (:id chat)])
                 :style {:display "flex"
                         :align-items "center"
                         :padding "10px"
                         :margin-bottom "8px"
                         :cursor "pointer"
                         :border-radius "16px"
                         :background (if active-chat?
                                       "linear-gradient(135deg, #fff59d 0%, #ffe082 100%)"
                                       "rgba(255, 255, 255, 0.65)")
                         :border (if active-chat?
                                   "2px solid #ffca28"
                                   "1px solid #e2d1b3")
                         :box-shadow (if active-chat?
                                       "0 4px 14px rgba(255, 193, 7, 0.4)"
                                       "none")
                         :transition "all 0.2s ease"}}
           [avatar-badge {:text (or (:initials chat) "CP") :active? active-chat?}]
           [:div {:style {:flex "1" :min-width "0" :margin-left "10px"}}
            [:div {:style {:font-weight "bold"
                           :color (if active-chat? "#3e2723" "#4e342e")
                           :font-size "clamp(13px, 1.1vw, 15px)"
                           :white-space "nowrap"
                           :overflow "hidden"
                           :text-overflow "ellipsis"}}
             (:contact chat)]
            [:div {:style {:font-size "10px"
                           :font-weight "600"
                           :color (if (= (:status chat) "en línea") "#855900" "#9e9d98")}}
             (str "• " (if (= (:status chat) "en línea") "EN FRECUENCIA" "DESCONECTADO"))]]]))]

     ;; Footer: current operator and disconnect button
     [:div {:style {:padding "12px"
                    :background "#e8d9bd"
                    :border-top "2px solid #dcc498"
                    :display "flex"
                    :align-items "center"
                    :justify-content "space-between"}}
      [:div {:style {:display "flex" :align-items "center" :gap "10px"}}
       [avatar-badge {:photo-url (:photo-url current-user)
                      :text (:initials current-user)
                      :active? true
                      :size "36px"}]
       [:div {:style {:display "flex" :flex-direction "column"}}
        [:div {:style {:font-size "12px" :font-weight "bold" :color "#3e2723"}} (:name current-user)]
        [:div {:style {:font-size "9px" :color "#705315" :font-weight "600"}} (:rank current-user)]]]
      [:button {:on-click #(rf/dispatch [:logout])
                :style {:padding "6px 10px"
                        :background "#d32f2f"
                        :color "white"
                        :border "none"
                        :border-radius "12px"
                        :font-size "10px"
                        :font-weight "bold"
                        :letter-spacing "0.5px"
                        :cursor "pointer"}}
       "SALIR"]]]))
