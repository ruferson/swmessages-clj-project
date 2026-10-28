(ns app.views.chat
  (:require [app.views.components :refer [avatar-badge]]
            [clojure.string :as str]
            [re-frame.core :as rf]))

(defn chat-component []
  (let [chat @(rf/subscribe [:active-chat])
        messages @(rf/subscribe [:active-messages])
        text @(rf/subscribe [:input-text])]
    [:div {:style {:flex "1"
                   :min-width "0"
                   :display "flex"
                   :flex-direction "column"
                   :background "linear-gradient(180deg, #fdfbf7 0%, #f3ebd8 100%)"}}
     ;; Terminal header
     [:div {:style {:padding "12px clamp(14px, 2vw, 24px)"
                    :background "linear-gradient(180deg, #ffffff 0%, #f6ecd5 100%)"
                    :border-bottom "2px solid #dcc498"
                    :display "flex"
                    :align-items "center"
                    :justify-content "space-between"
                    :box-shadow "0 2px 10px rgba(180, 150, 90, 0.12)"}}
      [:div {:style {:display "flex" :align-items "center" :gap "12px"}}
       [avatar-badge {:text (or (:initials chat) "CP") :active? true}]
       [:div
        [:div {:style {:font-weight "bold"
                       :color "#5d4037"
                       :font-size "clamp(15px, 1.4vw, 18px)"
                       :font-family "Georgia, serif"
                       :letter-spacing "0.5px"}}
         (:contact chat)]
        [:div {:style {:font-size "10px"
                       :color "#8d6e63"
                       :font-weight "bold"
                       :letter-spacing "1px"}}
         (str "CANAL ENLACE // " (str/upper-case (:status chat)))]]]

      [:div {:style {:font-size "10px"
                     :font-family "monospace"
                     :padding "4px 8px"
                     :background "rgba(255, 213, 79, 0.3)"
                     :border "1px solid #ffe082"
                     :border-radius "6px"
                     :color "#5d4037"}}
       "ENCRIPTACIÓN: CÓDIGO-3"]]

     ;; Holographic viewer panel
     [:div {:style {:flex "1"
                    :padding "clamp(12px, 2vw, 24px)"
                    :overflow-y "auto"
                    :display "flex"
                    :flex-direction "column"
                    :gap "12px"
                    :background "radial-gradient(circle at center, #fffefb 0%, #f7f1e1 100%)"
                    :box-shadow "inset 0 0 25px rgba(212, 175, 55, 0.1)"}}
      (for [message messages]
        (let [mine? (= (:sender message) "Tú")]
          ^{:key (:id message)}
          [:div {:style {:align-self (if mine? "flex-end" "flex-start")
                         :max-width "75%"
                         :padding "10px 16px"
                         :border-radius (if mine? "20px 20px 4px 20px" "20px 20px 20px 4px")
                         :background (if mine?
                                       "linear-gradient(135deg, #fff176 0%, #ffd54f 100%)"
                                       "linear-gradient(135deg, #ffffff 0%, #f5efe1 100%)")
                         :color (if mine? "#3e2723" "#4e342e")
                         :border (if mine? "1px solid #ffe082" "1px solid #dfd3bd")
                         :box-shadow (if mine?
                                       "0 4px 14px rgba(255, 213, 79, 0.35)"
                                       "0 2px 8px rgba(0, 0, 0, 0.04)")
                         :font-family "'Trebuchet MS', sans-serif"}}
           [:div {:style {:font-size "10px"
                          :font-weight "bold"
                          :margin-bottom "3px"
                          :opacity "0.7"
                          :letter-spacing "0.5px"}}
            (str "TRANSMISIÓN DE " (str/upper-case (:sender message)) " >>")]
           [:div {:style {:font-size "clamp(13px, 1.1vw, 15px)" :line-height "1.4"}} (:text message)]]))]

     ;; Bottom command console
     [:div {:style {:padding "12px clamp(12px, 2vw, 20px)"
                    :background "linear-gradient(180deg, #f6ecd5 0%, #e8d9bd 100%)"
                    :border-top "2px solid #dcc498"
                    :display "flex"
                    :gap "10px"
                    :align-items "center"}}
      [:input {:type "text"
               :placeholder "Redactar holomensaje..."
               :value text
               :on-change #(rf/dispatch [:update-input (.. % -target -value)])
               :on-key-down #(when (= (.-key %) "Enter") (rf/dispatch [:send-message]))
               :style {:flex "1"
                       :min-width "0"
                       :padding "10px 18px"
                       :background "#ffffff"
                       :border "2px solid #ffe082"
                       :border-radius "30px"
                       :color "#3e2723"
                       :font-size "clamp(12px, 1vw, 14px)"
                       :font-family "'Trebuchet MS', sans-serif"
                       :outline "none"
                       :box-shadow "inset 0 2px 4px rgba(0,0,0,0.03), 0 0 8px rgba(255, 224, 130, 0.5)"}}]
      [:button {:on-click #(rf/dispatch [:send-message])
                :style {:padding "10px clamp(14px, 2vw, 24px)"
                        :background "linear-gradient(180deg, #ffe082 0%, #ffb703 100%)"
                        :color "#3e2723"
                        :border "2px solid #ffca28"
                        :border-radius "30px"
                        :font-size "clamp(12px, 1vw, 14px)"
                        :font-family "Georgia, serif"
                        :font-weight "bold"
                        :letter-spacing "1px"
                        :cursor "pointer"
                        :white-space "nowrap"
                        :box-shadow "0 4px 12px rgba(255, 183, 3, 0.45)"}}
       "TRANSMITIR"]]]))
