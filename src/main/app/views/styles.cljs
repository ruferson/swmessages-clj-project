(ns app.views.styles)

;; --- COLOR PALETTE ---
(def colors
  {:bg-main         "radial-gradient(circle at center, #fffdf8 0%, #f2ebd9 100%)"
   :bg-card         "linear-gradient(180deg, #ffffff 0%, #f6ecd5 100%)"
   :bg-header       "linear-gradient(135deg, #fffdf7 0%, #f3e5c8 100%)"
   :bg-input        "#ffffff"
   :bg-panel        "#fdf6e3"
   :border-gold     "#dcc498"
   :border-light    "#e2d1b3"
   :border-focus    "#ffe082"
   :text-primary    "#3e2723"
   :text-secondary  "#5d4037"
   :text-muted      "#8d6e63"
   :text-gold       "#705315"
   :btn-primary-bg  "linear-gradient(180deg, #ffe082 0%, #ffb703 100%)"
   :btn-primary-bdr "#ffca28"
   :btn-accept      "#4caf50"
   :btn-reject      "#d32f2f"
   :font-serif      "Georgia, serif"
   :font-sans       "'Trebuchet MS', sans-serif"
   :font-mono       "monospace"
   :btn-accept-bg  "linear-gradient(180deg, #d4edda 0%, #c3e6cb 100%)"
   :btn-accept-bdr "#a1d9b2"
   :btn-accept-txt "#155724"

   :btn-reject-bg  "linear-gradient(180deg, #f8d7da 0%, #f5c6cb 100%)"
   :btn-reject-bdr "#f1b0b7"
   :btn-reject-txt "#721c24"})

;; --- SHARED COMPONENTS ---
(def input-field
  {:padding "10px 14px"
   :background (:bg-input colors)
   :border (str "2px solid " (:border-focus colors))
   :border-radius "16px"
   :color (:text-primary colors)
   :font-size "13px"
   :outline "none"})

(def label-title
  {:font-size "10px"
   :font-weight "bold"
   :color (:text-gold colors)})

(def primary-button
  {:padding "12px"
   :margin-top "6px"
   :background (:btn-primary-bg colors)
   :color (:text-primary colors)
   :border (str "2px solid " (:btn-primary-bdr colors))
   :border-radius "20px"
   :font-family (:font-serif colors)
   :font-weight "bold"
   :font-size "13px"
   :letter-spacing "1.5px"
   :box-shadow "0 4px 12px rgba(255, 183, 3, 0.45)"})

(def action-btn
  {:padding "5px 10px"
   :color "white"
   :border "none"
   :border-radius "10px"
   :font-size "9px"
   :font-weight "bold"
   :letter-spacing "0.5px"
   :cursor "pointer"})

(defn badge-status [active?]
  {:font-size "10px"
   :font-weight "600"
   :color (if active? "#855900" "#9e9d98")})