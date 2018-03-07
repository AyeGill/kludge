(ns {{namespace}}
  (:require [kludge.core :refer :all]
            [kludge.entities :as e]
            [kludge.ui :refer :all]))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage))
    (e/create-entity entities (label "Hello, World!" (color :white))))

  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities)))

(defgame {{game-name}}
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
