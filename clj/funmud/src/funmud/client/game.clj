
(set! *warn-on-reflection* true)
(ns funmud.client.game)

(def +size+ 64)
(def icons {:warrior (javax.swing.ImageIcon. "../media/warrior.jpg")
            :hunter (javax.swing.ImageIcon. "../media/hunter.jpg")
            :warlock (javax.swing.ImageIcon. "../media/warlock.jpg")
            :monster (javax.swing.ImageIcon. "../media/monster.jpg")
            :enemy (javax.swing.ImageIcon. "../media/enemy.jpg")
            :princess (javax.swing.ImageIcon. "../media/princess.jpg")
            :powerup (javax.swing.ImageIcon. "../media/clojure-icon.gif")
            :loot (javax.swing.ImageIcon. "../media/loot.jpg")
            :background (javax.swing.ImageIcon. "../media/play.png")})

(defn play-screen
  "This is where all the action happens!"
  [#^java.awt.Window window, character]
  (stop-midi)
  (let [width (.getWidth window)
        height (.getHeight window)
        start-x (int (/ (- width +size+) 2))
        start-y (int (/ (- height +size+) 2))
        rand-x (fn []
                 (let [r (rand-int (- width +size+))]
                   (if (< (* +size+ 3) (java.lang.Math/abs (- r start-x)))
                     r
                     (recur))))
        rand-y (fn []
                 (let [r (rand-int (- height +size+))]
                   (if (< (* +size+ 3) (java.lang.Math/abs (- r start-y)))
                     r
                     (recur))))
        world (ref {:player {:name character
                              :type :warrior
                              :x start-x
                              :y start-y
                              :rich false
                              :powered false}
                     :loot {:name "Wow, Gold!"
                            :type :loot
                            :x (rand-x)
                            :y (rand-y)}
                     :powerup {:name "Primordial Growth"
                               :type :powerup
                               :x (rand-x)
                               :y (rand-y)}
                     :princess {:name "Leia"
                                :type :princess
                                :x (rand-x)
                                :y (rand-y)}
                     :monster {:name "Cyclops"
                               :type :monster
                               :x (rand-x)
                               :y (rand-y)}
                     :enemy {:name "Machiavelli"
                             :type :enemy
                             :x (rand-x)
                             :y (rand-y)}})
        overlap (fn [e1 e2]
                  (and (@world e1) (@world e2)
                       (> +size+ (java.lang.Math/abs
                                   (- (-> @world e1 :x) (-> @world e2 :x))))
                       (> +size+ (java.lang.Math/abs
                                   (- (-> @world e1 :y) (-> @world e2 :y))))))
        check-world (fn []
                      (when (overlap :player :loot)
                        (alter world assoc-in [:player :rich] true)
                        (println "You are rich from"
                                 (-> @world :loot :name))
                        (alter world dissoc :loot))
                      (when (overlap :player :powerup)
                        (alter world assoc-in [:player :powered] true)
                        (println "You are empowered with"
                                 (-> @world :powerup :name))
                        (alter world dissoc :powerup))
                      (when (and (-> @world :player :powered)
                                 (overlap :player :monster))
                        (println "You killed "
                                 (-> @world :monster :name))
                        (alter world dissoc :monster))
                      (when (and (-> @world :player :rich)
                                 (overlap :player :princess))
                        (println "You married the princess"
                                 (-> @world :princess :name))
                        (alter world dissoc :princess))
                      (.repaint window))
        move (fn [entity dx dy]
               (dosync
                 (when (@world entity)
                   (alter world #(assoc-in %1 [entity :x]
                                        (inside (get-in %1 [entity :x])
                                                0 (- (dec width) +size+)
                                                + (* dx +size+))))
                   (alter world #(assoc-in %1 [entity :y]
                                        (inside (get-in %1 [entity :y])
                                                0 (- (dec height) +size+)
                                                + (* dy +size+))))
                   (check-world))))
        timer (doto (java.util.Timer.)
                (.schedule
                  (proxy [java.util.TimerTask] []
                    (run []
                      (move :enemy (dec (rand-int 3)) (dec (rand-int 3)))
                      (move :monster (dec (rand-int 3)) (dec (rand-int 3)))
                      (move :princess (dec (rand-int 3)) (dec (rand-int 3)))))
                  (long 1000) (long 1000)))
        north-action (create-action
                       "N" (fn [_] (move :player 0 -1)) "UP" "Move North")
        south-action (create-action
                       "S" (fn [_] (move :player 0 1)) "DOWN" "Move South")
        east-action (create-action
                      "E" (fn [_] (move :player 1 0)) "RIGHT" "Move East")
        west-action (create-action
                      "W" (fn [_] (move :player -1 0)) "LEFT" "Move West")]
    (doto window
      (.setContentPane
        (doto (proxy [javax.swing.JPanel] []
                (paintComponent [#^java.awt.Graphics g]
                  (.drawImage g (.getImage (icons :background)) 0 0
                              ; why does JPanel require type hint?
                              (.getWidth #^javax.swing.JPanel this)
                              (.getHeight #^javax.swing.JPanel this) nil)
                  (doseq [[k e] @world]
                    (.drawImage g (.getImage (icons (:type e)))
                              (e :x) (e :y) +size+ +size+ nil))
                  (proxy-super paintComponent g)))
          (.setOpaque false)))
      (.setLayout (java.awt.GridBagLayout.)))
  (play-midi "../media/HUMAN1.MID")
  (add-global-hotkey north-action)
  (add-global-hotkey south-action)
  (add-global-hotkey east-action)
  (add-global-hotkey west-action)
  (screen window blocker
          [[(javax.swing.JButton. north-action)
            :gridx 2 :gridy 1]
           [(javax.swing.JButton. south-action)
            :gridx 2 :gridy 3]
           [(javax.swing.JButton. east-action)
            :gridx 3 :gridy 2]
           [(javax.swing.JButton. west-action)
            :gridx 1 :gridy 2]
           [(button "Logout" evt
                    (stop-midi)
                    (set-background window "../media/splash.jpg")
                    (play-midi "../media/TITLE.MID")
                    (.cancel timer)
                    (.put blocker true))
            :anchor java.awt.GridBagConstraints/SOUTHEAST
            :gridx 4 :gridy 4 :weightx 1 :weighty 1]]
          [true client.main/login-screen])))

