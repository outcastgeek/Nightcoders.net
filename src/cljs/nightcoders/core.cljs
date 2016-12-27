(ns nightcoders.core
  (:require [reagent.core :as r]
            [cljs-react-material-ui.core :refer [get-mui-theme]]
            [cljs-react-material-ui.reagent :as ui]
            [nightcoders.auth :as auth])
  (:import goog.net.XhrIo))

(defonce state (r/atom {}))

(auth/set-sign-in #(swap! state assoc :signed-in? %))
(auth/load (fn [_]))

(defn new-project [project-name]
  (let [template (:new-project-template @state)]
    (swap! state dissoc :new-project-template)
    (.send XhrIo
      "/new-project"
      (fn [e]
        (when (.isSuccess (.-target e))
          (set! (.-location js/window) (.. e -target getResponseText))))
      "POST"
      (pr-str {:project-type template
               :project-name project-name}))))

(defn new-project-dialog []
  (let [project-name (atom "")]
    [ui/dialog {:modal true
                :open (some? (:new-project-template @state))
                :actions
                [(r/as-element
                   [ui/flat-button {:on-click #(swap! state dissoc :new-project-template)
                                    :style {:margin "10px"}}
                    "Cancel"])
                 (r/as-element
                   [ui/flat-button {:on-click #(new-project @project-name)
                                    :style {:margin "10px"}}
                    "Create Project"])]}
     [ui/text-field
      {:hint-text "Choose a name for your project"
       :full-width true
       :on-change #(reset! project-name (.-value (.-target %)))}]]))

(defn app []
  [ui/mui-theme-provider
   {:mui-theme (get-mui-theme
                 (doto (aget js/MaterialUIStyles "DarkRawTheme")
                   (aset "palette" "accent1Color" "darkgray")
                   (aset "palette" "accent2Color" "darkgray")
                   (aset "palette" "accent3Color" "darkgray")))}
   [:span
    [:div {:style {:margin "10px"
                   :display "inline-block"}}
     [:div {:class "g-signin2"
            :data-onsuccess "signIn"
            :style {:display (if (:signed-in? @state) "none" "block")}}]
     [ui/raised-button {:on-click (fn []
                                    (auth/sign-out #(swap! state assoc :signed-in? false)))
                        :style {:display (if (:signed-in? @state) "block" "none")}}
      "Sign Out"]]
    [new-project-dialog]
    [ui/card
     [ui/card-text
      (if (:signed-in? @state)
        [:span
         [:p "Create a new project:"]
         [:a {:href "#"
              :on-click #(swap! state assoc :new-project-template :basic-web)}
          "Basic Web App"]]
        [:span
         [:p "Build web apps and games with ClojureScript, a simple and powerful programming language."]
         [:p "Sign in with your Google account and start coding for free."]])]]]])

(r/render-component [app] (.querySelector js/document "#app"))

