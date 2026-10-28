(ns app.views.components)

;; Displays the user avatar, falling back to the initials when there is no photo
(defn avatar-badge [{:keys [photo-url text active? size] :or {size "36px"}}]
  (if photo-url
    [:img {:src photo-url
           :alt "Profile"
           :style {:width size
                   :height size
                   :border-radius "50%"
                   :object-fit "cover"
                   :border "2px solid #fffdf7"
                   :box-shadow "0 2px 6px rgba(0,0,0,0.12)"
                   :flex-shrink "0"}}]
    [:div {:style {:width size
                   :height size
                   :border-radius "50%"
                   :background (if active?
                                 "linear-gradient(135deg, #ffe082 0%, #ffb703 100%)"
                                 "linear-gradient(135deg, #e0d1b3 0%, #c4b292 100%)")
                   :border "2px solid #fffdf7"
                   :box-shadow "0 2px 6px rgba(0,0,0,0.1)"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :font-family "Georgia, serif"
                   :font-weight "bold"
                   :font-size "12px"
                   :color "#3e2723"
                   :flex-shrink "0"}}
     text]))
