(import '(java.awt.event ActionListener)
        '(javax.swing JButton SwingUtilities JFrame)
        '(java.util.concurrent LinkedBlockingQueue)
        '(clojure.lang IDeref IFn))


(defn fn->al [fn]
  (proxy [ActionListener] [] (actionPerformed [event] (future (fn event)))))

(defn actionqueue [obj fn]
  (.addActionListener obj (fn->al fn)))

(defn j-button
  ([text] (doto (JButton.) (.setText text)))
  ([text fn] (doto (j-button text) (.addActionListener (fn->al fn)))))

(defmacro EDT [& code]
  `(SwingUtilities/invokeLater
     (fn [] ~@code)))

(defn hydra
  "returns a BlockingQueue, will return a new infinite lazy-seq wrapped in a delay
  evertime it is deref'ed. all items put in the LinkedBlockingQueue will be added to
  all the lazy-seqs producded by deref'ing"
  []
  (let [consumers (atom nil)
        producer (proxy [LinkedBlockingQueue IDeref IFn] []
                   (invoke [& x]
                           (doseq [y x] (.put this y)))
                   (deref []
                          (let [x (LinkedBlockingQueue.)]
                            (swap! consumers conj x)
                            (delay (repeatedly #(.take x))))))]
    (future
      (while true
        (let [x (.take producer)]
          (doseq [y @consumers]
            (.put y x)))))
    producer))

(def queue (hydra)) ;events go here

(def window (JFrame.))

(def exit (j-button "Exit" queue)) ;the lbq from hydra is a function of one arg that enqueues it's arg

(EDT
  (doto window
    (.setTitle "Window")
    (.add exit)
    .pack
    (.setVisible true)))

(doseq [i (force @queue)] ;derefing the lbq from hydra returns a delay wrapped lazy-seq
  (prn (bean i)))