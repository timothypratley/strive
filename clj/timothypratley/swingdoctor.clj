; Copyright (c) Timothy Pratley. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(set! *warn-on-reflection* true)
(ns timothypratley.swingdoctor
  (:import
     (clojure.lang Agent Atom Ref)
     (java.awt Component GridBagLayout GridBagConstraints
               GraphicsEnvironment Graphics)
     (java.awt.event ActionListener)
     (javax.swing JTextField JLabel JPanel JPasswordField JFrame
                  ImageIcon)
     (javax.sound.midi MidiSystem)
     (javax.sound.sampled AudioSystem DataLine$Info Clip AudioInputStream)
     (java.io File)))


; General helpers

(defn reference-reset!
  "Resets the value of any reference"
  [#^IDeref reference, value]
  (cond
    (instance? Agent reference) (send reference (fn [x y] y) value)
    (instance? Atom reference) (reset! reference value)
    (instance? Ref reference) (dosync (ref-set reference value))))

(defn reference-swap!
  "Updates the value of any reference by applying func"
  [#^IDeref reference, func & args]
  (cond
    (instance? Agent reference) (apply send reference func args)
    (instance? Atom reference) (apply swap! reference func args)
    (instance? Ref reference) (dosync (apply commute reference func args))))

(defn inside
  "Restricts the result of a function"
  [value lower upper func & args]
  (let [result (apply func value args)]
    (cond
      (> lower result) lower
      (< upper result) upper
      :else result)))


; Swing helpers

(defmacro later
  "Invokes body on the swing event thread"
  [& body]
  `(javax.swing.SwingUtilities/invokeLater (fn [] ~@body)))

(defmacro add-action-listener
  "Attaches an ActionListener to a Component"
  [#^java.awt.Component obj, evt & body]
  `(.addActionListener
     ~obj (proxy [java.awt.event.ActionListener] []
            (actionPerformed [~evt] ~@body))))

(defmacro doc-listener
  "Creates a DocumentListener"
  [evt & body]
  `(proxy [javax.swing.event.DocumentListener] []
     (changedUpdate [~evt] ~@body)
     (insertUpdate [~evt] ~@body)
     (removeUpdate [~evt] ~@body)))

(defmacro add-text-listener
  "Attaches a TextListener to a Component"
  [#^java.awt.Component obj, evt & body]
  `(.addDocumentListener
     (.getDocument ~obj)
     (doc-listener ~evt ~@body)))

(defmacro button
  "Create a JButton with an ActionListener"
  [#^String text, evt & body]
  `(doto (javax.swing.JButton. ~text) (add-action-listener ~evt ~@body)))

(defmacro frame
  "Construct a frame"
  [#^String title & body]
  `(let [f# (javax.swing.JFrame. ~title)]
     (later 
       (javax.swing.UIManager/setLookAndFeel
         (javax.swing.UIManager/getSystemLookAndFeelClassName))
       (doto f#
         (.setDefaultCloseOperation (javax.swing.JFrame/EXIT_ON_CLOSE))
         (.setLayout (GridBagLayout.))
         ~@body
         (.pack)
         (.show)))
     f#))

(defn add
  "Adds a component to another component"
  [#^JFrame parent, #^Component child & constraints]
  (let [gbc (GridBagConstraints.)]
    (doseq [c (partition 2 constraints)]
      (clojure.lang.Reflector/setInstanceField gbc (name (first c)) (second c)))
    (.add (.getContentPane parent) child gbc)))

(defn full-screen
  "Enables full-screen mode"
  [#^java.awt.Window window]
  (later
    (->
      (GraphicsEnvironment/getLocalGraphicsEnvironment)
      .getDefaultScreenDevice
      (.setFullScreenWindow window))))

(defmacro screen
  "Adds elements with layout constraints to window.
  blocker is a symbol for use in creating a navigation event.
  Put the next transition onto blocker to move to a new screen.
  Transitions match value taken from blocker, last odd is default."
  [#^java.awt.Window window, blocker elements transitions]
  `(let [~blocker (java.util.concurrent.LinkedBlockingQueue.)]
     (later (doseq [e# ~elements]
              (apply add ~window (first e#) (rest e#)))
            (.validate ~window)
            (.repaint ~window))
     (let [result# (.take ~blocker)]
       (later (doseq [e# ~elements]
                (.removeAll (.getContentPane ~window))))
       [(condp = result# ~@transitions) result#])))

(defn navigate
  "Moves through predefined screens."
  [#^java.awt.Window window, next]
  (if-let [f (first next)]
    (recur window (f window (second next)))
    (.dispose window)))

(defn set-background
  "Sets an image as the background of a JFrame"
  [#^java.awt.Window window, #^String filename]
  (let [image (.getImage (ImageIcon. filename))]
    (doto window
      (.setContentPane
        (doto (proxy [JPanel] []
                (paintComponent [#^Graphics g]
                  (.drawImage g image 0 0
                              (.getWidth #^JPanel this)
                              (.getHeight #^JPanel this) nil)
                  (proxy-super paintComponent g)))
          (.setOpaque false)))
       (.setLayout (GridBagLayout.)))))

(defn input-map
  "Map a key to a function"
  [#^java.awt.Window window name key-event func & args]
  (-> window .getRootPane .getActionMap
    (.put name (proxy [javax.swing.AbstractAction] []
                 (actionPerformed [#^java.awt.event.ActionEvent a] 
                                  (println "ACTION!")
                                  (apply func args)))))
  (-> window .getRootPane
    (.getInputMap javax.swing.JComponent/WHEN_IN_FOCUSED_WINDOW)
    (.put (javax.swing.KeyStroke/getKeyStroke key-event 0) name)))


; MVC helpers

(defn link
  "Hooks a GUI item to a data item"
  [#^Component view, setter getter reference]
  (let [dl (doc-listener evt
                         (try
                           (let [v (getter view)]
                             (if (not= v @reference)
                               (reference-reset! reference v)))
                           (catch RuntimeException e)))]
    (when setter
      (setter view @reference)
      (add-watch reference view
                 (fn [me r old new]
                   (if (not= old new)
                     (later 
                       (if getter
                         (.removeDocumentListener (.getDocument view) dl))
                       (setter me @r)
                       (if getter
                         (.addDocumentListener (.getDocument view) dl)))))))
    (if getter
      (.addDocumentListener (.getDocument view) dl))))

(defn link-in
  "Hooks a GUI item to a data item in a map found by ks a seq of keys"
  [#^Component view, setter getter reference ks]
  (when setter
    (setter view (get-in @reference ks))
    (add-watch reference view
               (fn [me r old new]
                 (if (not= (get-in old ks) (get-in new ks))
                   (later (setter me (get-in @r ks)))))))
  (if getter
    (add-text-listener view evt
                       (let [v (getter view)]
                         (if (not= v (get-in @reference ks))
                           (reference-swap! reference assoc-in ks v))))))

(defn unlink
  "Unlinks a GUI item from a data item"
  [#^Deref reference, #^Component view]
  (remove-watch reference view))

(defn label
  "Returns a JLabel which watches a reference"
  ([reference]
   (doto (JLabel.)
     (link (fn [#^JLabel x, #^String y]
             (if (not= y (.getText x))
               (.setText x y)))
           nil
           reference)))
  ([reference format-str]
   (doto (JLabel.)
     (link (fn [#^JLabel x, y]
             (if (not= (format format-str y) (.getText x))
               (.setText x (format format-str y))))
           nil
           reference))))

(defn text-field
  "Returns a JTextField which watches and updates a reference"
  ([reference]
   (doto (JTextField. 20)
     (link (fn [#^JTextField x, #^String y]
             (if (not= y (.getText x))
               (.setText x y)))
           (fn [#^JTextField x] (.getText x))
           reference)))
  ([reference, #^String label]
   (doto (JPanel.)
     (.add (JLabel. label))
     (.add (text-field reference))))
  ([reference to-string from-string]
   (doto (JTextField. 20)
     (link (fn [#^JTextField x, y]
             (if (not= (to-string y) (.getText x))
               (.setText x (to-string y))))
           (fn [#^JTextField x] (from-string (.getText x)))
           reference))))

(defn password-field
  "Returns a JPasswordField which watches and updates a reference"
  ([reference]
   (doto (JPasswordField. 20)
     (link (fn [#^JPasswordField x, #^String y]
             (if (not= y (.getText x))
               (.setText x y)))
           (fn [#^JPasswordField x] (.getText x))
           reference)))
  ([reference, #^String label]
   (doto (JPanel.)
     (.add (JLabel. label))
     (.add (password-field reference)))))

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


; Audio helpers

(defn play-clip
  [#^String filename]
  (doto (AudioSystem/getLine (DataLine$Info. (.class Clip) format))
    (.open (AudioInputStream. (File. filename)))
    (.start)))

(let [midi-sequencer (MidiSystem/getSequencer)]
  (defn play-midi
    "Play a midi file"
    [#^String filename]
    (doto midi-sequencer
      (.open)
      (.stop)
      (.setSequence (MidiSystem/getSequence (File. filename)))
      (.start)))
  (defn stop-midi
    "Stop midi playing"
    []
    (doto midi-sequencer
      (.stop)
      (.close))))


; Example

(defn simple-demo []
  ; Define the data model
  (let [temp (atom 25.0)]
    ; Setup the view
    (frame "Motor monitor"
           (.add (label temp "%f degrees C"))
           (.add (text-field temp (comp str int)
                             #(Double/parseDouble %1))))
    (loop []
      ; Data model changes...
      ; We don't need to update the view explicitly. It updates itself.
      (Thread/sleep 2000)
      (swap! temp #(+ %1 -1 (rand 2.5)))
      ; Meanwhile the textbox can also update the temperature.
      (recur))))

;(defn dynamic-demo []
  ;(frame "Database Fun"
         ;(button "Add entry" (add-entry))
         ;(button "View table" (view-table))))

;(simple-demo)
