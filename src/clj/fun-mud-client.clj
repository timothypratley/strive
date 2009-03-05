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

(def login-screen)
(defn play-screen
  [#^java.awt.Window window, character]
  (stop-midi)
  (set-background window "play.png")
  (play-midi "HUMAN1.MID")
  (screen window blocker
          [[(button "Bye" evt
                    (stop-midi)
                    (set-background window "splash.jpg")
                    (play-midi "TITLE.MID")
                    (.put blocker true))]]
          [true login-screen]))

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

(let [icons {:warrior (javax.swing.ImageIcon. "warrior.jpg")
             :hunter (javax.swing.ImageIcon. "hunter.jpg")
             :warlock (javax.swing.ImageIcon. "warlock.jpg")}]
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
             false login-screen play-screen])))

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

