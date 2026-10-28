(ns app.views.sidebar
  (:require [app.views.components :refer [avatar-badge]]
            [clojure.string :as str]
            [re-frame.core :as rf]))

(defn request-item [request current-user sending?]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :padding "8px"
                 :margin-bottom "6px"
                 :background "#ffffff"
                 :border "1px solid #e2d1b3"
                 :border-radius "14px"}}
   [avatar-badge {:text (or (:initials request) "OP") :active? false}]
   [:div {:style {:flex "1" :min-width "0"}}
    [:div {:style {:font-size "12px" :font-weight "bold" :color "#3e2723"}} (:name request)]
    [:div {:style {:font-size "9px" :color "#8d6e63" :font-weight "600"}} (:email request)]]
   [:button {:on-click #(rf/dispatch [:respond-friend-request (:id current-user) (:id request) true])
             :disabled sending?
             :style {:padding "5px 10px"
                     :background "#4caf50"
                     :color "white"
                     :border "none"
                     :border-radius "10px"
                     :font-size "9px"
                     :font-weight "bold"
                     :letter-spacing "0.5px"
                     :cursor "pointer"}} "ACEPTAR"]
   [:button {:on-click #(rf/dispatch [:respond-friend-request (:id current-user) (:id request) false])
             :disabled sending?
             :style {:padding "5px 10px"
                     :background "#d32f2f"
                     :color "white"
                     :border "none"
                     :border-radius "10px"
                     :font-size "9px"
                     :font-weight "bold"
                     :letter-spacing "0.5px"
                     :cursor "pointer"}} "RECHAZAR"]])

(defn friend-item [friend current-user]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :padding "8px"
                 :margin-bottom "8px"
                 :background "rgba(255, 255, 255, 0.65)"
                 :border "1px solid #e2d1b3"
                 :border-radius "14px"}}
   [avatar-badge {:text (or (:initials friend) "OP") :active? false}]
   [:div {:style {:flex "1" :min-width "0" :margin-left "10px"}}
    [:div {:style {:font-weight "bold"
                   :color (if (= (:id friend) (:id current-user)) "#3e2723" "#4e342e")
                   :font-size "clamp(13px, 1.1vw, 15px)"
                   :white-space "nowrap"
                   :overflow "hidden"
                   :text-overflow "ellipsis"}}
     (:name friend)]
    [:div {:style {:font-size "10px"
                    :font-weight "600"
                    :color "#9e9d98"}} (str "ID: " (:id friend))]]
   [:button {:on-click #(js/navigator.clipboard.writeText (or (:id friend) ""))
             :style {:padding "4px 8px"
                     :background "#ffe082"
                     :color "#4a3800"
                     :border "1px solid #ffca28"
                     :border-radius "8px"
                     :font-size "8px"
                     :font-weight "bold"
                     :letter-spacing "0.5px"
                     :cursor "pointer"}} "COPIAR"]])

(defn chat-item [chat active-chat-id]
  (let [active-chat? (= (:id chat) active-chat-id)]
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
       (str "• " (if (= (:status chat) "en línea") "EN FRECUENCIA" "DESCONECTADO"))]]]))

(defn sidebar-component []
  (let [chats @(rf/subscribe [:chats])
        active-chat-id @(rf/subscribe [:active-chat-id])
        current-user @(rf/subscribe [:current-user])
        friends @(rf/subscribe [:friends])
        friend-requests @(rf/subscribe [:friend-requests])
        friend-id-input @(rf/subscribe [:friend-id-input])
        sending-friend-request? @(rf/subscribe [:sending-friend-request?])
        settings-open? @(rf/subscribe [:settings-open?])]
    [:div {:style {:width "clamp(200px, 30vw, 290px)"
                   :min-width "180px"
                   :position "relative"
                   :background "linear-gradient(180deg, #fbf9f3 0%, #f1e7d0 100%)"
                   :border-right "2px solid #dfcfb2"
                   :box-shadow "inset -4px 0 12px rgba(212, 175, 55, 0.15)"
                   :display "flex"
                   :flex-direction "column"}}

      ;; Header
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

      ;; Friend requests section
      (when (seq friend-requests)
        [:div {:style {:padding "10px 12px"
                       :background "#fdf6e3"
                       :border-bottom "1px solid #dfcfb2"}}
         [:div {:style {:font-size "10px"
                        :font-weight "bold"
                        :color "#705315"
                        :letter-spacing "1.5px"
                        :margin-bottom "8px"}} "SOLICITUDES DE AMISTAD"]
         (for [request friend-requests]
           (request-item request current-user sending-friend-request?))])

      ;; Friends section
      (when (seq friends)
        [:div {:style {:flex "1" :overflow-y "auto" :padding "8px"}}
         [:div {:style {:font-size "10px"
                        :font-weight "bold"
                        :color "#705315"
                        :letter-spacing "1.5px"
                        :margin-bottom "8px"}} "CONTACTOS"]
         (for [friend friends]
           (friend-item friend current-user))
         [:div {:style {:display "flex"
                        :gap "6px"
                        :margin-top "6px"
                        :padding "8px"
                        :background "#fdf6e3"
                        :border "1px dashed #dcc498"
                        :border-radius "14px"}}
          [:input {:type "text"
                   :placeholder "ID DEL OPERADOR (Ej. op-123456)"
                   :value friend-id-input
                   :on-change #(rf/dispatch [:update-friend-id (.. % -target -value)])
                   :style {:flex "1"
                           :min-width "0"
                           :padding "6px 10px"
                           :background "#ffffff"
                           :border "1px solid #ffe082"
                           :border-radius "10px"
                           :color "#3e2723"
                           :font-size "10px"
                           :outline "none"}}]
          [:button {:on-click #(rf/dispatch [:send-friend-request])
                    :disabled (or sending-friend-request? (str/blank? friend-id-input))
                    :style {:padding "6px 12px"
                            :background "#ffe082"
                            :color "#4a3800"
                            :border "1px solid #ffca28"
                            :border-radius "12px"
                            :font-size "9px"
                            :font-weight "bold"
                            :letter-spacing "0.5px"
                            :cursor (if sending-friend-request? "wait" "pointer")}} "ENVIAR SOLICITUD"]]])

      ;; Frequencies list
      [:div {:style {:flex "1" :overflow-y "auto" :padding "8px"}}
       (for [chat chats]
         (chat-item chat active-chat-id))]

      ;; Settings modal
      (when settings-open?
        [:div {:style {:position "absolute"
                       :top "0"
                       :right "0"
                       :bottom "0"
                       :left "0"
                       :background "rgba(40, 28, 12, 0.45)"
                       :z-index "20"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"}}
         [:div {:style {:width "min(320px, 85%)"
                        :background "linear-gradient(180deg, #ffffff 0%, #f6ecd5 100%)"
                        :border "2px solid #dcc498"
                        :border-radius "20px"
                        :padding "20px"
                        :display "flex"
                        :flex-direction "column"
                        :gap "12px"}}
          [:div {:style {:font-family "Georgia, serif"
                         :font-size "16px"
                         :font-weight "bold"
                         :color "#5d4037"
                         :textAlign "center"}} "CONFIGURACIÓN DE OPERADOR"]
          [:div {:style {:font-size "11px"
                         :font-weight "bold"
                         :color "#705315"
                         :textAlign "center"
                         :letter-spacing "0.5px"}} (str "ID DE OPERADOR: " (or (:id current-user) ""))]
          [:div {:style {:display "flex"
                         :gap "8px"
                         :justify-content "center"}}
           [:button {:on-click #(js/navigator.clipboard.writeText (or (:id current-user) ""))
                     :style {:padding "8px 14px"
                             :background "#ffe082"
                             :color "#4a3800"
                             :border "1px solid #ffca28"
                             :border-radius "12px"
                             :font-size "10px"
                             :font-weight "bold"
                             :letter-spacing "0.5px"
                             :cursor "pointer"}} "COPIAR ID"]
           [:button {:on-click #(rf/dispatch [:toggle-settings])
                     :style {:padding "8px 14px"
                             :background "#f6ecd5"
                             :color "#705315"
                             :border "1px solid #dcc498"
                             :border-radius "12px"
                             :font-size "10px"
                             :font-weight "bold"
                             :letter-spacing "0.5px"
                             :cursor "pointer"}} "CERRAR"]]]])

      ;; Footer
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
       [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
        [:button {:on-click #(rf/dispatch [:toggle-settings])
                  :style {:padding "6px 10px"
                          :background "#f6ecd5"
                          :color "#705315"
                          :border "1px solid #dcc498"
                          :border-radius "12px"
                          :font-size "10px"
                          :font-weight "bold"
                          :letter-spacing "0.5px"
                          :cursor "pointer"}}
         "CONFIG"]
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
          "SALIR"]]]]))
