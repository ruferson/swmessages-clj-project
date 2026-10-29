(ns app.views.auth
  (:require [app.views.components :refer [avatar-badge]]
            [app.views.styles :as st]
            [re-frame.core :as rf]))

;; Reads the image file chosen by the user and converts it into a data URL
(defn process-image-file [e]
  (let [file (.. e -target -files (item 0))]
    (when file
      (let [reader (js/FileReader.)]
        (set! (.-onloadreader reader)
              (fn [event]
                (let [data-url (.. event -target -result)]
                  (rf/dispatch [:save-profile-photo data-url]))))
        (.readAsDataURL reader file)))))

(defn auth-component []
  (let [form @(rf/subscribe [:auth-form])
        error @(rf/subscribe [:auth-error])
        loading? @(rf/subscribe [:loading-auth?])
        {:keys [mode name email password password-confirmation rank photo-url]} form
        is-registration? (= mode :register)]
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :align-items "center"
                   :justify-content "center"
                   :width "100%"
                   :height "100%"
                   :background (:bg-main st/colors)
                   :padding "20px"
                   :box-sizing "border-box"
                   :overflow-y "auto"}}

     ;; Access console
     [:div {:style {:width "100%"
                    :max-width "460px"
                    :background (:bg-card st/colors)
                    :border (str "3px solid " (:border-gold st/colors))
                    :border-radius "28px"
                    :padding "28px 24px"
                    :box-shadow "0 15px 35px rgba(180, 150, 90, 0.25)"
                    :display "flex"
                    :flex-direction "column"
                    :gap "16px"
                    :box-sizing "border-box"}}

      ;; Console header
      [:div {:style {:text-align "center"}}
       [:div {:style {:font-family (:font-serif st/colors)
                      :font-size "10px"
                      :font-weight "bold"
                      :color (:text-muted st/colors)
                      :letter-spacing "3px"
                      :margin-bottom "4px"}}
        "// HOLONET TERMINAL //"]
       [:div {:style {:font-family (:font-serif st/colors)
                      :font-size "22px"
                      :font-weight "bold"
                      :color (:text-secondary st/colors)
                      :letter-spacing "1px"}}
        "SISTEMA DE IDENTIFICACIÓN"]
       [:div {:style {:font-size "11px"
                      :color (:text-muted st/colors)
                      :font-weight "600"
                      :letter-spacing "1px"
                      :margin-top "4px"}}
        (if is-registration? "REGISTRO OFICIAL DE OPERADOR" "INGRESO A LA FRECUENCIA")]]

      ;; Server error message
      (when error
        [:div {:style {:padding "10px 14px"
                       :background "#ffebee"
                       :border "1px solid #ef5350"
                       :border-radius "12px"
                       :color "#c62828"
                       :font-size "12px"
                       :font-weight "bold"
                       :text-align "center"}}
         error])

      ;; Load profile photo (Registration only)
      (when is-registration?
        [:div {:style {:display "flex"
                       :flex-direction "column"
                       :align-items "center"
                       :gap "8px"
                       :padding "10px"
                       :background "#FAF5EA"
                       :border (str "1px dashed " (:border-gold st/colors))
                       :border-radius "16px"}}
         [avatar-badge {:photo-url photo-url
                        :text (if (seq name) (subs name 0 1) "OP")
                        :active? true
                        :size "64px"}]
         [:label {:style {:padding "6px 14px"
                          :background (:border-focus st/colors)
                          :border (str "1px solid " (:btn-primary-bdr st/colors))
                          :border-radius "16px"
                          :font-size "11px"
                          :font-weight "bold"
                          :color (:text-primary st/colors)
                          :cursor "pointer"}}
          "SUBIR HOLO-IMAGEN"
          [:input {:type "file"
                   :accept "image/*"
                   :on-change process-image-file
                   :style {:display "none"}}]]])

      ;; Operator identifier field (Registration only)
      (when is-registration?
        [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
         [:label {:style st/label-title}
          "IDENTIFICADOR DE OPERADOR:"]
         [:input {:type "text"
                  :placeholder "Ej. Ruben"
                  :value name
                  :on-change #(rf/dispatch [:update-auth-field :name (.. % -target -value)])
                  :style st/input-field}]])

      ;; Email field
      [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
       [:label {:style st/label-title}
        "FRECUENCIA / CORREO GALÁCTICO:"]
       [:input {:type "email"
                :placeholder "ruben@holonet.gal"
                :value email
                :on-change #(rf/dispatch [:update-auth-field :email (.. % -target -value)])
                :style st/input-field}]]

      ;; Rank selector (Registration only)
      (when is-registration?
        [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
         [:label {:style st/label-title}
          "RANGO EN LA REPÚBLICA:"]
         [:select {:value rank
                   :on-change #(rf/dispatch [:update-auth-field :rank (.. % -target -value)])
                   :style st/input-field}
          [:option {:value "Piloto de Caza"} "Piloto de Caza"]
          [:option {:value "Oficial de Enlace"} "Oficial de Enlace"]
          [:option {:value "Comandante de Escuadrón"} "Comandante de Escuadrón"]
          [:option {:value "Ingeniero Mecánico"} "Ingeniero Mecánico"]]])

      ;; Encryption code field
      [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
       [:label {:style st/label-title}
        "CÓDIGO DE ENCRIPTACIÓN:"]
       [:input {:type "password"
                :placeholder "••••••••"
                :value password
                :on-change #(rf/dispatch [:update-auth-field :password (.. % -target -value)])
                :style st/input-field}]]

      ;; Confirm encryption code field (Registration only)
      (when is-registration?
        [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
         [:label {:style st/label-title}
          "CONFIRMAR CÓDIGO:"]
         [:input {:type "password"
                  :placeholder "••••••••"
                  :value password-confirmation
                  :on-change #(rf/dispatch [:update-auth-field :password-confirmation (.. % -target -value)])
                  :style st/input-field}]])

      ;; Main button
      [:button {:on-click #(rf/dispatch [:login])
                :disabled loading?
                :style (assoc st/primary-button
                              :cursor (if loading? "wait" "pointer")
                              :opacity (if loading? "0.6" "1"))}
       (if loading?
         "CONECTANDO..."
         (if is-registration? "REGISTRAR OPERADOR" "ESTABLECER ENLACE"))]

      ;; Mode switch
      [:div {:style {:text-align "center" :margin-top "2px"}}
       [:span {:on-click #(rf/dispatch [:change-auth-mode (if is-registration? :login :register)])
               :style {:font-size "12px"
                       :color (:text-muted st/colors)
                       :text-decoration "underline"
                       :cursor "pointer"
                       :font-weight "600"}}
        (if is-registration?
          "¿Ya posees credencial? Iniciar enlace"
          "¿Sin registrar? Alta de nuevo operador")]]]]))