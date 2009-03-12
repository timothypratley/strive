; Copyright (c) Timothy Pratley. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(set! *warn-on-reflection* true)
(ns fun-mud-client
  (:use swingcells))

(defn login
  "Pseudo login check, returns user details"
  [#^String username, #^String password]
  (if true 
    ;(and (zero? (.compareToIgnoreCase username "Conan"))
    ;     (= password "Barbarian"))
    {:username username,
     :characters [{:name "Maldy", :class :warrior}
                  {:name "Than", :class :warlock}
                  {:name "Alsvid" :class :hunter}]}
    false))

(def icons {:warrior (javax.swing.ImageIcon. "warrior.jpg")
            :hunter (javax.swing.ImageIcon. "hunter.jpg")
            :warlock (javax.swing.ImageIcon. "warlock.jpg")
            :monster (javax.swing.ImageIcon. "monster.jpg")
            :enemy (javax.swing.ImageIcon. "enemy.jpg")
            :princess (javax.swing.ImageIcon. "princess.jpg")
            :loot (javax.swing.ImageIcon. "loot.jpg")})
(def login-screen)
(def +size+ 64)
(defn play-screen
  [#^java.awt.Window window, character]
  (stop-midi)
  (let [width (.getWidth window)
        height (.getHeight window)
        world (atom {:player-x (/ (- width +size+) 2)
                     :player-y (/ (- height +size+) 2)
                     :loot-x (rand (- width +size+))
                     :loot-y (rand (- height +size+))
                     :princess-x (rand (- width +size+))
                     :princess-y (rand (- height +size+))
                     :monster-x (rand (- width +size+))
                     :monster-y (rand (- height +size+))
                     :enemy-x (rand (- width +size+))
                     :enemy-y (rand (- height +size+))})
        move (fn [entity-x entity-y dx dy]
               (swap! world #(assoc %1 entity-x
                                    (inside (%1 entity-x)
                                            0 (- (dec width) +size+)
                                            + (* dx +size+))))
               (swap! world #(assoc %1 entity-y
                                    (inside (%1 entity-y)
                                            0 (- (dec height) +size+)
                                            + (* dy +size+))))
               (.repaint window))
        back-image (.getImage (javax.swing.ImageIcon. "play.png"))
        char-image (.getImage (icons :warrior))
        enemy-image (.getImage (icons :enemy))
        monster-image (.getImage (icons :monster))
        loot-image (.getImage (icons :loot))
        princess-image (.getImage (icons :princess))
        timer (doto (java.util.Timer.)
                (.schedule (proxy [java.util.TimerTask] []
                             (run []
                                  (move :enemy-x :enemy-y
                                        (dec (int (rand 3)))
                                        (dec (int (rand 3))))
                                  (move :monster-x :monster-y
                                        (dec (int (rand 3)))
                                        (dec (int (rand 3))))
                                  (move :princess-x :princess-y
                                        (dec (int (rand 3)))
                                        (dec (int (rand 3))))))
                           (long 1000)
                           (long 1000)))]
    (doto window
      (.setContentPane
        (doto (proxy [javax.swing.JPanel] []
                (paintComponent [#^java.awt.Graphics g]
                  (.drawImage g back-image 0 0
                              (.getWidth #^javax.swing.JPanel this)
                              (.getHeight #^javax.swing.JPanel this) nil)
                  (.drawImage g loot-image
                              (@world :loot-x) (@world :loot-y)
                              +size+ +size+ nil)
                  (.drawImage g monster-image
                              (@world :monster-x) (@world :monster-y)
                              +size+ +size+ nil)
                  (.drawImage g enemy-image
                              (@world :enemy-x) (@world :enemy-y)
                              +size+ +size+ nil)
                  (.drawImage g princess-image
                              (@world :princess-x) (@world :princess-y)
                              +size+ +size+ nil)
                  (.drawImage g char-image
                              (@world :player-x) (@world :player-y)
                              +size+ +size+ nil)
                  (proxy-super paintComponent g)))
          (.setOpaque false)))
      (.setLayout (java.awt.GridBagLayout.)))
  (play-midi "HUMAN1.MID")
  (screen window blocker
          [[(button "N" evt (move :player-x :player-y 0 -1))
            :gridx 2 :gridy 1]
           [(button "S" evt (move :player-x :player-y 0 1))
            :gridx 2 :gridy 3]
           [(button "E" evt (move :player-x :player-y 1 0))
            :gridx 3 :gridy 2]
           [(button "W" evt (move :player-x :player-y -1 0))
            :gridx 1 :gridy 2]
           [(button "Logout" evt
                    (stop-midi)
                    (set-background window "splash.jpg")
                    (play-midi "TITLE.MID")
                    (.cancel timer)
                    (.put blocker true))
            :anchor java.awt.GridBagConstraints/SOUTHEAST
            :gridx 4 :gridy 4 :weightx 1 :weighty 1]]
          [true login-screen])))

(defn character-create-screen
  [#^java.awt.Window window, _]
  (let [character-name (atom "")]
    (screen window blocker
            [[(text-field character-name "Enter your chacter's name:")]
             [(button "Confirm" evt (if (pos? (count @character-name))
                                      (.put blocker @character-name)))
              :gridx 1]
             [(button "Cancel" evt (.put blocker false))
              :gridx 2]]
            [false login-screen play-screen])))

(defn character-select-screen
  [#^java.awt.Window window, user]
  (screen window blocker
          (conj 
            (vec (map #(list
; oh my I'm out of whitespace
(doto (button (str (:name %1) " - " (name (:class %1)))
            evt (.put blocker (:name %1)))
(.setIcon (icons (:class %1))))
                         :gridx 1)
                      (user :characters)))
            [(button "Create New" evt (.put blocker :create-new))
             :gridx 1])
          [:create-new character-create-screen
           false login-screen play-screen]))

(defn login-screen
  [#^java.awt.Window window, _]
  (let [username (atom "")
        password (atom "")]
    (screen window blocker
            [[(text-field username "Username:")
              :gridx 1, :gridy 1]
             [(password-field password "Password:")
              :gridx 1, :gridy 2]
             [(javax.swing.JLabel. "Hint: Conan/Barbarian")
              :gridx 2, :gridy 2]
             [(button "Login" evt (.put blocker (login @username @password)))
              :gridx 1, :gridy 4]
             [(button "Quit" evt (.put blocker :quit))
              :gridx 2, :gridy 5]]
            [:quit nil false login-screen character-select-screen])))

(defn splash-screen
  [#^java.awt.Window window, _]
  (set-background window "splash.jpg")
  (play-midi "TITLE.MID")
  (screen window blocker
          [[(button "FunMud" evt (.put blocker true))]]
          [login-screen]))


(defn game-demo []
  (navigate
    (doto
      (frame "FunMud")
      (full-screen)
      (.setUndecorated true))
    [splash-screen nil])
  (stop-midi))

(game-demo)

