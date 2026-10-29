(ns app.views.sidebar
  (:require [app.views.components :refer [avatar-badge]]
            [app.views.styles :as st]
            [clojure.string :as str]
            [re-frame.core :as rf]))

;; Incoming request item
(defn request-item [request current-user sending?]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :padding "10px 10px"
                 :margin-bottom "8px"
                 :background "rgba(255, 255, 255, 0.75)"
                 :border (str "1px solid " (:border-gold st/colors))
                 :border-radius "14px"
                 :box-shadow "0 2px 6px rgba(180, 150, 90, 0.12)"}}

   [avatar-badge {:text (or (:initials request) "OP") :active? false :size "32px"}]

   [:div {:style {:flex "1"
                  :min-width "0"
                  :display "flex"
                  :flex-direction "column"}}
    [:div {:style {:font-size "12px"
                   :font-weight "bold"
                   :color (:text-primary st/colors)
                   :white-space "nowrap"
                   :overflow "hidden"
                   :text-overflow "ellipsis"}}
     (:name request)]
    [:div {:style {:font-size "9px"
                   :color (:text-muted st/colors)
                   :font-family (:font-mono st/colors)
                   :white-space "nowrap"
                   :overflow "hidden"
                   :text-overflow "ellipsis"}}
     (str "ID: " (:id request))]]

   [:div {:style {:display "flex" :gap "4px" :flex-shrink "0"}}
    [:button {:on-click #(rf/dispatch [:respond-friend-request (:id current-user) (:id request) true])
              :disabled sending?
              :style {:padding "5px 8px"
                      :background (:btn-accept-bg st/colors)
                      :color (:btn-accept-txt st/colors)
                      :border (str "1px solid " (:btn-accept-bdr st/colors))
                      :border-radius "8px"
                      :font-family (:font-serif st/colors)
                      :font-size "9px"
                      :font-weight "bold"
                      :cursor "pointer"}}
     "ACEPTAR"]
    [:button {:on-click #(rf/dispatch [:respond-friend-request (:id current-user) (:id request) false])
              :disabled sending?
              :style {:padding "5px 8px"
                      :background (:btn-reject-bg st/colors)
                      :color (:btn-reject-txt st/colors)
                      :border (str "1px solid " (:btn-reject-bdr st/colors))
                      :border-radius "8px"
                      :font-family (:font-serif st/colors)
                      :font-size "9px"
                      :font-weight "bold"
                      :cursor "pointer"}}
     "RECHAZAR"]]])

;; Sent request item (Outbound / Pending)
(defn sent-request-item [request]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :padding "8px"
                 :margin-bottom "6px"
                 :background "rgba(255, 255, 255, 0.5)"
                 :border (str "1px dashed " (:border-gold st/colors))
                 :border-radius "14px"}}
   [avatar-badge {:text (or (:initials request) "OP") :active? false}]
   [:div {:style {:flex "1" :min-width "0"}}
    [:div {:style {:font-size "11px" :font-weight "bold" :color (:text-secondary st/colors)}} (:name request)]
    [:div {:style {:font-size "9px" :color (:text-muted st/colors) :font-weight "600"}} (str "ID: " (:id request))]]
   [:span {:style {:font-size "8px"
                   :font-weight "bold"
                   :padding "3px 6px"
                   :background "#fff3cd"
                   :color "#856404"
                   :border "1px solid #ffeeba"
                   :border-radius "8px"
                   :font-family (:font-mono st/colors)}}
    "PENDIENTE"]])

;; Confirmed contact item (Clickable to open chat)
(defn friend-item [friend active-chat-id]
  (let [active? (= (:id friend) active-chat-id)]
    [:div {:on-click #(rf/dispatch [:select-chat (:id friend)])
           :style {:display "flex"
                   :align-items "center"
                   :gap "8px"
                   :padding "10px"
                   :margin-bottom "8px"
                   :cursor "pointer"
                   :border-radius "16px"
                   :background (if active?
                                 "linear-gradient(135deg, #fff59d 0%, #ffe082 100%)"
                                 "rgba(255, 255, 255, 0.65)")
                   :border (if active?
                             (str "2px solid " (:btn-primary-bdr st/colors))
                             (str "1px solid " (:border-light st/colors)))
                   :box-shadow (if active?
                                 "0 4px 14px rgba(255, 193, 7, 0.4)"
                                 "none")
                   :transition "all 0.2s ease"}}
     [avatar-badge {:text (or (:initials friend) "OP") :active? active?}]
     [:div {:style {:flex "1" :min-width "0" :margin-left "4px"}}
      [:div {:style {:font-weight "bold"
                     :color (:text-primary st/colors)
                     :font-size "13px"
                     :white-space "nowrap"
                     :overflow "hidden"
                     :text-overflow "ellipsis"}}
       (:name friend)]
      [:div {:style {:font-size "9px"
                     :color (:text-muted st/colors)
                     :font-family (:font-mono st/colors)}}
       (str "ID: " (:id friend))]]]))

(defn sidebar-component []
  (let [chats @(rf/subscribe [:chats])
        active-chat-id @(rf/subscribe [:active-chat-id])
        current-user @(rf/subscribe [:current-user])
        friends @(rf/subscribe [:friends])
        friend-requests @(rf/subscribe [:friend-requests])
        sent-friend-requests @(rf/subscribe [:sent-friend-requests])
        friend-id-input @(rf/subscribe [:friend-id-input])
        sending-friend-request? @(rf/subscribe [:sending-friend-request?])
        settings-open? @(rf/subscribe [:settings-open?])]
    [:div {:style {:width "clamp(200px, 30vw, 290px)"
                   :min-width "180px"
                   :position "relative"
                   :background "linear-gradient(180deg, #fbf9f3 0%, #f1e7d0 100%)"
                   :border-right (str "2px solid " (:border-gold st/colors))
                   :box-shadow "inset -4px 0 12px rgba(212, 175, 55, 0.15)"
                   :display "flex"
                   :flex-direction "column"}}

     ;; Panel Header
     [:div {:style {:padding "clamp(12px, 2vw, 20px) 14px"
                    :background (:bg-header st/colors)
                    :border-bottom (str "2px solid " (:border-gold st/colors))
                    :color (:text-gold st/colors)
                    :font-family (:font-serif st/colors)
                    :font-weight "bold"
                    :letter-spacing "1px"
                    :display "flex"
                    :align-items "center"
                    :justify-content "space-between"}}
      [:div {:style {:display "flex" :align-items "center" :gap "6px"}}
       [:span {:style {:font-size "11px" :letter-spacing "2px" :font-family (:font-mono st/colors)}} "HOLONET // CHANNELS"]]
      [:span {:style {:font-size "9px"
                      :padding "2px 6px"
                      :background (:border-focus st/colors)
                      :border-radius "8px"
                      :color "#4a3800"
                      :font-family (:font-mono st/colors)
                      :font-weight "bold"}} "SEC-9"]]

     ;; Send Request Input Section
     [:div {:style {:padding "10px 12px"
                    :background (:bg-panel st/colors)
                    :border-bottom (str "1px solid " (:border-light st/colors))}}
      [:div {:style st/label-title} "AÑADIR OPERADOR"]
      [:div {:style {:display "flex" :gap "6px" :margin-top "6px"}}
       [:input {:type "text"
                :placeholder "ID (Ej. op-171829)"
                :value friend-id-input
                :on-change #(rf/dispatch [:update-friend-id (.. % -target -value)])
                :style (assoc st/input-field :padding "6px 10px" :font-size "10px" :flex "1" :min-width "0")}]
       [:button {:on-click #(rf/dispatch [:send-friend-request])
                 :disabled (or sending-friend-request? (str/blank? friend-id-input))
                 :style (assoc st/action-btn :background (:border-focus st/colors) :color (:text-gold st/colors))}
        (if sending-friend-request? "..." "+")]]]

     ;; Pending Incoming Requests Section
     (when (seq friend-requests)
       [:div {:style {:padding "10px 12px"
                      :background (:bg-panel st/colors)
                      :border-bottom (str "1px solid " (:border-light st/colors))}}
        [:div {:style (assoc st/label-title :margin-bottom "8px")} "SOLICITUDES RECIBIDAS"]
        (for [request friend-requests]
          ^{:key (:id request)}
          [request-item request current-user sending-friend-request?])])

     ;; Pending Outgoing Requests Section
     (when (seq sent-friend-requests)
       [:div {:style {:padding "10px 12px"
                      :background "linear-gradient(180deg, #fdf6e3 0%, #f7ebd0 100%)"
                      :border-bottom (str "1px solid " (:border-light st/colors))}}
        [:div {:style (assoc st/label-title :margin-bottom "8px")} "SOLICITUDES ENVIADAS"]
        (for [sent-req sent-friend-requests]
          ^{:key (:id sent-req)}
          [sent-request-item sent-req])])

     ;; Confirmed Friends & Channels Section
     [:div {:style {:flex "1" :overflow-y "auto" :padding "8px"}}
      [:div {:style (assoc st/label-title :margin-bottom "8px")} "CONTACTOS EN FRECUENCIA"]
      (if (seq friends)
        (for [friend friends]
          ^{:key (:id friend)}
          [friend-item friend active-chat-id])
        [:div {:style {:font-size "11px" :color (:text-muted st/colors) :padding "8px"}} "Sin contactos vinculados."])

      (when (seq chats)
        [:div {:style {:margin-top "12px"}}
         [:div {:style (assoc st/label-title :margin-bottom "8px")} "CANALES DE PRUEBA"]
         (for [chat chats]
           ^{:key (:id chat)}
           [friend-item {:id (:id chat)
                         :name (:contact chat)
                         :initials (:initials chat)} active-chat-id])])]

     ;; Settings Modal
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
                       :background (:bg-card st/colors)
                       :border (str "2px solid " (:border-gold st/colors))
                       :border-radius "20px"
                       :padding "20px"
                       :display "flex"
                       :flex-direction "column"
                       :gap "12px"}}
         [:div {:style {:font-family (:font-serif st/colors)
                        :font-size "16px"
                        :font-weight "bold"
                        :color (:text-secondary st/colors)
                        :text-align "center"}} "CONFIGURACIÓN DE OPERADOR"]
         [:div {:style {:font-size "11px"
                        :font-weight "bold"
                        :color (:text-gold st/colors)
                        :text-align "center"
                        :letter-spacing "0.5px"}} (str "ID DE OPERADOR: " (or (:id current-user) ""))]
         [:div {:style {:display "flex"
                        :gap "8px"
                        :justify-content "center"}}
          [:button {:on-click #(js/navigator.clipboard.writeText (or (:id current-user) ""))
                    :style {:padding "8px 14px"
                            :background (:border-focus st/colors)
                            :color "#4a3800"
                            :border (str "1px solid " (:btn-primary-bdr st/colors))
                            :border-radius "12px"
                            :font-size "10px"
                            :font-weight "bold"
                            :letter-spacing "0.5px"
                            :cursor "pointer"}} "COPIAR ID"]
          [:button {:on-click #(rf/dispatch [:toggle-settings])
                    :style {:padding "8px 14px"
                            :background "#f6ecd5"
                            :color (:text-gold st/colors)
                            :border (str "1px solid " (:border-gold st/colors))
                            :border-radius "12px"
                            :font-size "10px"
                            :font-weight "bold"
                            :letter-spacing "0.5px"
                            :cursor "pointer"}} "CERRAR"]]]])

     ;; Sidebar Footer
     [:div {:style {:padding "12px"
                    :background "#e8d9bd"
                    :border-top (str "2px solid " (:border-gold st/colors))
                    :display "flex"
                    :align-items "center"
                    :justify-content "space-between"}}
      [:div {:style {:display "flex" :align-items "center" :gap "10px"}}
       [avatar-badge {:photo-url (:photo-url current-user)
                      :text (:initials current-user)
                      :active? true
                      :size "36px"}]
       [:div {:style {:display "flex" :flex-direction "column"}}
        [:div {:style {:font-size "12px" :font-weight "bold" :color (:text-primary st/colors)}} (:name current-user)]
        [:div {:style {:font-size "9px" :color (:text-gold st/colors) :font-weight "600"}} (:rank current-user)]]]
      [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
       [:button {:on-click #(rf/dispatch [:toggle-settings])
                 :style {:padding "6px 10px"
                         :background "#f6ecd5"
                         :color (:text-gold st/colors)
                         :border (str "1px solid " (:border-gold st/colors))
                         :border-radius "12px"
                         :font-size "10px"
                         :font-weight "bold"
                         :letter-spacing "0.5px"
                         :cursor "pointer"}}
        "CONFIG"]
       [:button {:on-click #(rf/dispatch [:logout])
                 :style {:padding "6px 10px"
                         :background (:btn-reject st/colors)
                         :color "white"
                         :border "none"
                         :border-radius "12px"
                         :font-size "10px"
                         :font-weight "bold"
                         :letter-spacing "0.5px"
                         :cursor "pointer"}}
        "SALIR"]]]]))