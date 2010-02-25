(ns three
  (:require [clojure.contrib.logging :as log]
     [clojure.contrib.swing-utils :as su]
     [clojure.contrib.miglayout :as ml]
     [tjpext.swingdoctor :as sd]
     [java3d :as j3])
  (:import [com.sun.j3d.utils.applet MainFrame]
     [java.awt BorderLayout Font]
     [java.applet Applet]))


(defn main-
  [& args]
  ;; make a window
  (def frame (doto (MainFrame. (doto (Applet.)
                                 (.setLayout (BorderLayout.))
                                 (.add "Center" j3/canvas)
                                 #_(.add "South"
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

  (sd/add-global-hotkey
    (sd/create-action "forward"
      (fn [_] (dosync (alter world update-in [:view :eye 2] inc)))
      "UP" "move forward"))
  (sd/add-global-hotkey
    (sd/create-action "backward"
      (fn [_] (dosync (alter world update-in [:view :eye 2] dec)))
      "DOWN" "move backward"))
  (sd/add-global-hotkey
    (sd/create-action "left"
      (fn [_] (dosync (alter world update-in [:view :eye 0] inc)))
      "LEFT" "turn left"))
  (sd/add-global-hotkey
    (sd/create-action "right"
      (fn [_] (dosync (alter world update-in [:view :eye 0] dec)))
      "RIGHT" "turn right"))

  ; TODO: use a watcher instead
  (j3/render-world @world)

  (while true
    (Thread/sleep 1000)
    (dosync (alter world update-in [:content "Dantra" :eye 0] inc))
    (j3/render-world @world)))

(main-)
