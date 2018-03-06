(ns kludge.helloworld
  (:require [kludge.core :refer :all]
            [kludge.entities :as e]
            [kludge.ui :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:use clojure.test))
(def l (e/entity-uid))
(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage))
    (e/create-entity entities l (assoc (label "Hello world!" (color :white)) :x 0 :y 0)))

  :on-render
  (fn [screen entities]
    (clear!)
    (print entities)
    (render! screen entities)
    (update-in entities [l :x] inc)))

(defgame game-test
  :on-create
  (fn [this]
    (set-screen! this main-screen)))

(deftest hello-world
  (LwjglApplication. game-test "Hello World" 800 600)
  (Keyboard/enableRepeatEvents true)
  (read-line))
