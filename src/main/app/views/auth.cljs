(ns app.views.auth
  (:require [app.views.components :refer [avatar-badge]]
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
                   :background "radial-gradient(circle at center, #fffdf8 0%, #f2ebd9 100%)"
                   :padding "20px"
                   :box-sizing "border-box"
                   :overflow-y "auto"}}]

     ;; Access console
     [:div {:style {:width "100%"
                    :max-width "460px"
                    :background "linear-gradient(180deg, #ffffff 0%, #f6ecd5 100%)"
                    :border "3px solid #dcc498"
                    :border-radius "28px"
                    :padding "28px 24px"
                    :box-shadow "0 15px 35px rgba(180, 150, 90, 0.25)"
                    :display "flex"
                    :flex-direction "column"
                    :gap "16px"
                    :box-sizing "border-box"}}

      ;; Console header
      [:div {:style {:text-align "center"}}
       [:div {:style {:font-family "Georgia, serif"
                      :font-size "10px"
                      :font-weight "bold"
                      :color "#8d6e63"
                      :letter-spacing "3px"
                      :margin-bottom "4px"}}
         "// HOLONET TERMINAL //"]
       [:div {:style {:font-family "Georgia, serif"
                      :font-size "22px"
                      :font-weight "bold"
                      :color "#5d4037"
                      :letter-spacing "1px"}}
         "SISTEMA DE IDENTIFICACIÓN"]
       [:div {:style {:font-size "11px"
                      :color "#8d6e63"
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
                       :border "1px dashed #dcc498"
                       :border-radius "16px"}}
         [avatar-badge {:photo-url photo-url
                        :text (if (seq name) (subs name 0 1) "OP")
                        :active? true
                        :size "64px"}]
         [:label {:style {:padding "6px 14px"
                          :background "#ffe082"
                          :border "1px solid #ffca28"
                          :border-radius "16px"
                          :font-size "11px"
                          :font-weight "bold"
                          :color "#3e2723"
                          :cursor "pointer"}}
          "SUBIR HOLO-IMAGEN"
          [:input {:type "file"
                   :accept "image/*"
                   :on-change process-image-file
                   :style {:display "none"}}]]])

      ;; Operator identifier field (Registration only)
      (when is-registration?
        [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
         [:label {:style {:font-size "10px" :font-weight "bold" :color "#705315"}}
          "IDENTIFICADOR DE OPERADOR:"]
         [:input {:type "text"
                  :placeholder "Ej. Ruben"
                  :value name
                  :on-change #(rf/dispatch [:update-auth-field :name (.. % -target -value)])
                  :style {:padding "10px 14px"
                          :background "#ffffff"
                          :border "2px solid #ffe082"
                          :border-radius "16px"
                          :color "#3e2723"
                          :font-size "13px"
                          :outline "none"}}]])

      ;; Email field
      [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
       [:label {:style {:font-size "10px" :font-weight "bold" :color "#705315"}}
        "FRECUENCIA / CORREO GALÁCTICO:"]
       [:input {:type "email"
                :placeholder "ruben@holonet.gal"
                :value email
                :on-change #(rf/dispatch [:update-auth-field :email (.. % -target -value)])
                :style {:padding "10px 14px"
                        :background "#ffffff"
                        :border "2px solid #ffe082"
                        :border-radius "16px"
                        :color "#3e2723"
                        :font-size "13px"
                        :outline "none"}}]]

      ;; Rank selector (Registration only)
      (when is-registration?
        [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
         [:label {:style {:font-size "10px" :font-weight "bold" :color "#705315"}}
          "RANGO EN LA REPÚBLICA:"]
         [:select {:value rank
                   :on-change #(rf/dispatch [:update-auth-field :rank (.. % -target -value)])
                   :style {:padding "10px 14px"
                           :background "#ffffff"
                           :border "2px solid #ffe082"
                           :border-radius "16px"
                           :color "#3e2723"
                           :font-size "13px"
                           :outline "none"}}
          [:option {:value "Piloto de Caza"} "Piloto de Caza"]
          [:option {:value "Oficial de Enlace"} "Oficial de Enlace"]
          [:option {:value "Comandante de Escuadrón"} "Comandante de Escuadrón"]
          [:option {:value "Ingeniero Mecánico"} "Ingeniero Mecánico"]]])

      ;; Encryption code field
      [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
       [:label {:style {:font-size "10px" :font-weight "bold" :color "#705315"}}
        "CÓDIGO DE ENCRIPTACIÓN:"]
       [:input {:type "password"
                :placeholder "••••••••"
                :value password
                :on-change #(rf/dispatch [:update-auth-field :password (.. % -target -value)])
                :style {:padding "10px 14px"
                        :background "#ffffff"
                        :border "2px solid #ffe082"
                        :border-radius "16px"
                        :color "#3e2723"
                        :font-size "13px"
                        :outline "none"}}]]

      ;; Confirm encryption code field (Registration only)
      (when is-registration?
        [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
         [:label {:style {:font-size "10px" :font-weight "bold" :color "#705315"}}
          "CONFIRMAR CÓDIGO:"]
         [:input {:type "password"
                  :placeholder "••••••••"
                  :value password-confirmation
                  :on-change #(rf/dispatch [:update-auth-field :password-confirmation (.. % -target -value)])
                  :style {:padding "10px 14px"
                          :background "#ffffff"
                          :border "2px solid #ffe082"
                          :border-radius "16px"
                          :color "#3e2723"
                          :font-size "13px"
                          :outline "none"}}]])

      ;; Main button
      [:button {:on-click #(rf/dispatch [:login])
                :disabled loading?
                :style {:padding "12px"
                        :margin-top "6px"
                        :background "linear-gradient(180deg, #ffe082 0%, #ffb703 100%)"
                        :color "#3e2723"
                        :border "2px solid #ffca28"
                        :border-radius "20px"
                        :font-family "Georgia, serif"
                        :font-weight "bold"
                        :font-size "13px"
                        :letter-spacing "1.5px"
                        :cursor (if loading? "wait" "pointer")
                        :opacity (if loading? "0.6" "1")
                        :box-shadow "0 4px 12px rgba(255, 183, 3, 0.45)"}}
       (if loading?
         "CONECTANDO..."
         (if is-registration? "REGISTRAR OPERADOR" "ESTABLECER ENLACE"))]

      ;; Mode switch
      [:div {:style {:text-align "center" :margin-top "2px"}}
       [:span {:on-click #(rf/dispatch [:change-auth-mode (if is-registration? :login :register)])
               :style {:font-size "12px"
                       :color "#8d6e63"
                       :text-decoration "underline"
                       :cursor "pointer"
                       :font-weight "600"}}
        (if is-registration?
          "¿Ya posees credencial? Iniciar enlace"
          "¿Sin registrar? Alta de nuevo operador")]]]))
