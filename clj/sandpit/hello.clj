(defmulti encounter (fn [x y] [(:job x) (:job y)]))
(defmethod encounter [:pirate :ninja] [p n]
  (str (:name p) " tries to convince " (:name n) " to use EMACS"))
(defmethod encounter [:ninja :pirate] [n p]
  (str (:name n) " roofwalks through Lisp in VIM, invisible to " (:name p)))
(defmethod encounter [:ninja :ninja] [n1 n2]
  (str (:name n1) " paints こんにちは世界 before " (:name n2)))
(defmethod encounter [:pirate :pirate] [p1 p2]
  (str (:name p1) " engages " (:name p2) " in a pitched sea battle."))
(dorun
  (map println
       (let [es [{:job :ninja, :name "Chikirin"}
                 {:job :ninja, :name "Fumodo"}
                 {:job :pirate, :name "Jolly Bud Slasher"}
                 {:job :pirate, :name "Cap'n Elmer Burntbeard"}]]
         (filter identity
                 (for [e1 es e2 es]
                   (if (not= e1 e2)
                     (encounter e1 e2)))))))

(comment output is:
Chikirin paints こんにちは世界 before Fumodo
Chikirin roofwalks through Lisp in VIM, invisible to Jolly Bud Slasher
Chikirin roofwalks through Lisp in VIM, invisible to Cap'n Elmer Burntbeard
Fumodo paints こんにちは世界 before Chikirin
Fumodo roofwalks through Lisp in VIM, invisible to Jolly Bud Slasher
Fumodo roofwalks through Lisp in VIM, invisible to Cap'n Elmer Burntbeard
Jolly Bud Slasher tries to convince Chikirin to use EMACS
Jolly Bud Slasher tries to convince Fumodo to use EMACS
Jolly Bud Slasher engages Cap'n Elmer Burntbeard in a pitched sea battle.
Cap'n Elmer Burntbeard tries to convince Chikirin to use EMACS
Cap'n Elmer Burntbeard tries to convince Fumodo to use EMACS
Cap'n Elmer Burntbeard engages Jolly Bud Slasher in a pitched sea battle.)

