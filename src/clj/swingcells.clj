(set! *warn-on-reflection* true)
(ns timothypratley.swingcells)


; General swing helpers

(defmacro later
  "Invokes body on the swing event thread"
  [& body]
  `(javax.swing.SwingUtilities/invokeLater (fn [] ~@body)))

(defmacro add-listener
  "Attaches an ActionListener to a Component"
  [#^java.awt.Component obj evt# & body]
  `(.addActionListener
     ~obj (proxy [java.awt.event.ActionListener] []
            (actionPerformed [evt#] ~@body))))

(defmacro add-button
  "Create a JButton with an ActionListener"
  [#^java.awt.Component parent #^String text & body]
  `(.add parent (doto (JButton. text) (action-listener ~@body))))

;in progress
(defmacro frame
  "Construct a frame"
  [#^String title & body]
  `(later 
    (javax.swing.UIManager/setLookAndFeel
      (javax.swing.UIManager/getSystemLookAndFeelClassName))
    (doto (javax.swing.JFrame. ~title)
      (.setDefaultCloseOperation (javax.swing.JFrame/EXIT_ON_CLOSE))
      (.setLayout (java.awt.GridLayout.))
      ~@body
      (.pack)
      (.show))))


; MVC helpers

(defn set-mut
  "Sets the value of any mutable"
  [#^clojure.IDeref mutable value]
  (cond
    (instance? clojure.lang.Agent mutable) (send mutable (fn [x y] y) value)
    (instance? clojure.lang.Atom mutable) (reset! mutable value)
    (instance? clojure.lang.Ref mutable) (dosync (ref-set mutable value))))

(defn link
  "Hooks a GUI item to a data item"
  [view setter getter mutable]
  (when setter
    (add-watcher mutable :send (agent view)
                 (fn [me dr]
                   (later
                     (setter me @dr))
                   me))
    (setter view @mutable))
  (if getter
    (add-listener view evt (let [v (getter view)]
                             (if (not= v @mutable)
                               (set-mut mutable v))))))

(defn linked-label
  "Returns a JLabel which watches a mutable"
  ([mutable]
   (doto (javax.swing.JLabel.)
     (link #(.setText %1 %2)
           nil
           mutable)))
  ([mutable format-str]
   (doto (javax.swing.JLabel.)
     (link #(.setText %1 (format format-str %2))
           nil
           mutable))))

(defn linked-text
  "Returns a JTextField which watches and updates a mutable"
  ([mutable]
   (doto (javax.swing.JTextField.)
     ; todo: this is double specific!!! no good
     (link #(.setText %1 (str %2))
           #(Double/parseDouble (.getText %1))
           mutable)))
  ([mutable format-str]
   (doto (javax.swing.JTextField.)
     (link #(.setText %1 (format format-str %2))
           #(.getText %1)
           mutable))))

; in progress
(comment
(defn table-model
  [rows col-names value-at] 
  (proxy [AbstractTableModel] [] 
    (getRowCount []    (count rows)) 
    (getColumnCount [] (count col-names)) 
    (getColumnName [c] (nth col-names c)) 
    (getValueAt [r c]  (value-at r c)) 
    (isCellEditable [r c] false))) 
)


; Demo app

; define the data model
(let [temp (atom 25.0)]
  ; setup the view
  (frame "Motor monitor"
    (.add (linked-label temp "%f degrees C"))
    (.add (linked-text temp)))
  (loop []
    ; data model changes...
    ; but we don't need to update the view explicitly,
    ; it updates itself.
    (Thread/sleep 2000)
    (swap! temp #(+ %1 -1 (rand 2.5)))
    (recur)))
; meanwhile the textbox can also update the temperature
; you have to be quick type a new number and press enter

