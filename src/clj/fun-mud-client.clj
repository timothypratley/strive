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
            :warlock (javax.swing.ImageIcon. "warlock.jpg")})
(def login-screen)
(def +char-size+ 64)
(defn play-screen
  [#^java.awt.Window window, character]
  (stop-midi)
  (let [width (.getWidth window)
        height (.getHeight window)
        x (atom (/ (- width +char-size+) 2))
        y (atom (/ (- height +char-size+) 2))
        move (fn [dx dy]
               (swap! x inside 0 (- (dec width) +char-size+)
                      + (* dx +char-size+))
               (swap! y inside 0 (- (dec height) +char-size+)
                      + (* dy +char-size+))
               (.repaint window))
        back-image (.getImage (javax.swing.ImageIcon. "play.png"))
        char-image (.getImage (icons :warrior))]
    (doto window
      (.setContentPane
        (doto (proxy [javax.swing.JPanel] []
                (paintComponent [#^java.awt.Graphics g]
                  (.drawImage g back-image 0 0
                              (.getWidth this) (.getHeight this) nil)
                  (.drawImage g char-image @x @y +char-size+ +char-size+ nil)
                  (proxy-super paintComponent g)))
          (.setOpaque false)))
      (.setLayout (java.awt.GridBagLayout.)))
  (play-midi "HUMAN1.MID")
  (screen window blocker
          [[(button "N" evt (move 0 -1))
            :gridx 2 :gridy 1]
           [(button "S" evt (move 0 1))
            :gridx 2 :gridy 3]
           [(button "E" evt (move 1 0))
            :gridx 3 :gridy 2]
           [(button "W" evt (move -1 0))
            :gridx 1 :gridy 2]
           [(button "Logout" evt
                    (stop-midi)
                    (set-background window "splash.jpg")
                    (play-midi "TITLE.MID")
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

