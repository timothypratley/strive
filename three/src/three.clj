(ns three
  (:require [clojure.contrib.logging :as log])
  (:import [com.sun.j3d.utils.applet MainFrame]
     [java.awt BorderLayout Font]
     [java.applet Applet])
  (:use [java3d]))


;; make a window
(def frame (doto (MainFrame. (doto (Applet.)
                               (.setLayout (BorderLayout.))
                               (.add "Center" canvas)
                               (.add "South"
                                     (panel
                                       (label world [:view :eye] "Eye: %s")
                                       (label world [:view :look] "Look: %s")
                                       (label world [:view :up] "Up: %s")
                                       (label world [:info :fps] "FPS %f"))))
                             1024 512)
             (.setTitle "three")))
(.show frame)

(def world (ref {:view {:eye  [0 0 -10]
                        :look [0 0 0]
                        :up   [0 1 0]}
                 :content {"Dantra" {:model "Wight"
                                     :eye  [0 5 0]
                                     :look [0 0 0]
                                     :up   [0 0 1]}
                           "Minion" {:model "Bat"
                                     :eye  [5 0 0]
                                     :look [0 0 0]
                                     :up   [0 0 1]}
                           "Linux" {:model "Penguin"
                                    :eye  [0 0 0]
                                    :look [0 1 0]
                                    :up   [0 0 1]}}}))

; TODO: use a watcher instead
(render-world @world)

(while true
  (Thread/sleep 1000)
  (dosync (alter world update-in [:content "Dantra" :eye 0] inc))
  (render-world @world))

