

(set! *warn-on-reflection* true)
(ns timothypratley.swingcells
  (:import
     (clojure.lang Agent Atom Ref)
     (java.awt Component Window GridBagLayout GridBagConstraints
               GraphicsEnvironment)
     (java.awt.event ActionListener)
     (javax.swing JTextField JLabel JPanel JPasswordField JButton JFrame
                  SwingUtilities UIManager ImageIcon)
     (javax.swing.event DocumentListener)
     (javax.sound.midi MidiSystem)
     (javax.sound.sampled AudioSystem DataLine$Info Clip AudioInputStream)
     (java.util.concurrent LinkedBlockingQueue)
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


; Swing helpers

(defmacro later
  "Invokes body on the swing event thread"
  [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))

(defmacro add-action-listener
  "Attaches an ActionListener to a Component"
  [#^Component obj, evt & body]
  `(.addActionListener
     ~obj (proxy [ActionListener] []
            (actionPerformed [~evt] ~@body))))

(defmacro add-text-listener
  "Attaches a TextListener to a Component"
  [#^Component obj, evt & body]
  `(.addDocumentListener
     (.getDocument ~obj)
     (proxy [DocumentListener] []
       (changedUpdate [~evt] ~@body)
       (insertUpdate [~evt] ~@body)
       (removeUpdate [~evt] ~@body))))

(defmacro button
  "Create a JButton with an ActionListener"
  [#^String text, evt & body]
  `(doto (JButton. ~text) (add-action-listener ~evt ~@body)))

(defmacro frame
  "Construct a frame"
  [#^String title & body]
  `(let [f# (JFrame. ~title)]
     (later 
       (UIManager/setLookAndFeel
         (UIManager/getSystemLookAndFeelClassName))
       (doto f#
         (.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
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
  [#^Window window]
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
  [#^Window window, blocker elements transitions]
  `(let [~blocker (LinkedBlockingQueue.)]
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
  [#^Window window, next]
  (if-let [f (first next)]
    (recur window (f window (second next)))
    (.dispose window)))

(defn set-background
  "Sets an image as the background of a JFrame"
  [#^Window window, #^String filename]
  (let [image (.getImage (ImageIcon. filename))]
    (doto window
      (.setContentPane
        (doto (proxy [JPanel] []
                (paintComponent [g]
                  (.drawImage g image 0 0
                              (.getWidth this) (.getHeight this) nil)
                  (proxy-super paintComponent g)))
          (.setOpaque false)))
       (.setLayout (GridBagLayout.)))))

; MVC helpers

(defn link
  "Hooks a GUI item to a data item"
  [#^Component view, setter getter reference]
  (when setter
    (setter view @reference)
    (add-watch reference view
               (fn [me r old new]
                 (if (not= old new)
                   (later (setter me @r))))))
  (if getter
    (add-text-listener view evt
                       (let [v (getter view)]
                         (if (not= v @reference)
                           (reference-reset! reference v))))))

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


; Audo helpers

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


; Examples

(defn simple-demo []
  ; Define the data model
  (let [temp (atom 25.0)]
    ; Setup the view
    (frame "Motor monitor"
           (.add (label temp "%f degrees C"))
           (.add (text-field temp str (memfn Double/parseDouble))))
    (loop []
      ; Data model changes...
      ; We don't need to update the view explicitly. It updates itself.
      (Thread/sleep 2000)
      (swap! temp #(+ %1 -1 (rand 2.5)))
      ; Meanwhile the textbox can also update the temperature.
      ; You have to be quick type a new number and press enter.
      (recur))))

;(defn dynamic-demo []
  ;(frame "Database Fun"
         ;(button "Add entry" (add-entry))
         ;(button "View table" (view-table))))


; Game demo

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
  [#^Window window, character]
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
  [#^Window window, _]
  (let [character-name (atom "")]
    (screen window blocker
            [[(text-field character-name "Enter your chacter's name:")]
             [(button "Confirm" evt (if (pos? (count @character-name))
                                      (.put blocker @character-name)))
              :gridx 1]
             [(button "Cancel" evt (.put blocker false))
              :gridx 2]]
            [false login-screen play-screen])))

(let [icons {:warrior (ImageIcon. "warrior.jpg")
             :hunter (ImageIcon. "hunter.jpg")
             :warlock (ImageIcon. "warlock.jpg")}]
  (defn character-select-screen
    [#^Window window, user]
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
  [#^Window window, _]
  (let [username (atom "")
        password (atom "")]
    (screen window blocker
            [[(text-field username "Username:")
              :gridx 1, :gridy 1]
             [(password-field password "Password:")
              :gridx 1, :gridy 2]
             [(JLabel. "Hint: Conan/Barbarian")
              :gridx 2, :gridy 2]
             [(button "Login" evt (.put blocker (login @username @password)))
              :gridx 1, :gridy 4]
             [(button "Quit" evt (.put blocker :quit))
              :gridx 2, :gridy 5]]
            [:quit nil false login-screen character-select-screen])))

(defn splash-screen
  [#^Window window, _]
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

