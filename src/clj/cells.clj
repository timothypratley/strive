(ns timothypratley.cells)

(defn watching-label
  "Returns a JLabel which watches an IRef r"
  [r format-str]
  (let [label (javax.swing.JLabel. (format format-str @r))]
    (add-watcher r :send (agent label)
                 (fn [me r]
                   (javax.swing.SwingUtilities/invokeLater
                     #(.setText me (format format-str @r)))
                   me))
    label))

; define the data model
(let [temp (atom 25.0)]
  ; setup the view
  (doto (javax.swing.JFrame. "Motor monitor")
    (.add (watching-label temp "%f degrees C"))
    (.pack)
    (.show))
  (loop []
    ; data model changes...
    ; but we don't need to update the view explicitly,
    ; it updates itself.
    (Thread/sleep 100)
    (swap! temp #(+ %1 -1 (rand 2.5)))
    (recur)))


