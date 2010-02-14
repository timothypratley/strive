(ns timothypratley.logging
  (:use [clojure.contrib.str-utils :only (re-sub)] timothypratley.extensions)
  (:import (java.util.logging Logger Level Formatter ConsoleHandler)
     (java.text SimpleDateFormat)
     (java.util Date)
     (java.io ByteArrayOutputStream PrintWriter PrintStream
              OutputStreamWriter OutputStream)))

; Stephen C. Gilardi   	
(defn unmangle
  "Given the name of a class that implements a Clojure function,  
  returns the function's name in Clojure.
  Note: If the true Clojure function name contains any underscores
  (a rare occurrence), the unmangled name will contain hyphens
  at those locations instead."
  [class-name]
  (.replace
    (re-sub #"^(.+)\$(.+)__\d+$" "$1/$2" class-name)
    \_ \-))

; Stephen C. Gilardi   	
(defmacro current-function-name
  "Returns a string, the name of the current Clojure function."
  []
  `(-> (Throwable.) .getStackTrace first .getClassName unmangle))

;; note line and filename
;; (println "file:" *file* "  line:" @(clojure.lang.Compiler/LINE))

(let [logger (Logger/getLogger "timothypratley.extensions.log")
      a (agent nil)
      m {:off Level/OFF
         :severe Level/SEVERE
         :warning Level/WARNING
         :info Level/INFO
         :config Level/CONFIG
         :fine Level/FINE
         :finer Level/FINER
         :finest Level/FINEST}]
  (.addHandler logger (ConsoleHandler.))
  (.setUseParentHandlers logger false)

  (defn log*
    {:doc (str "Log a message. level should be one of:
  " (keys m))}
    [level thread fun & text]
    (send-off a (fn [_]
                  (.logp logger (m level) thread fun (apply str text)))))

  (defmacro log
    {:doc (str "Log a message.
  Buffers messages sequentially so multiple thread logs will not overlap.
  level should be one of:
  " (keys m))}
    [level & text]
    `(log* ~level
           (.getName (Thread/currentThread))
           (current-function-name)
           ~@text))

  (defn log-level
    {:doc (str "Sets the current log level to one of:
  " (keys m))}
    [level]
    (.setLevel logger (m level))
    (doseq [h (.getHandlers logger)]
      (.setLevel h (m level))))

  (defn log-format
    "Sets the log formatter."
    ([]
     (log-format
          (let [sdf (SimpleDateFormat. "yyyy/MM/dd HH:mm:ss")]
            (proxy [Formatter] []
              (format [r]
                      (str "[" (.format sdf (Date. (.getMillis r)))
                           " " (.getLevel r)
                           "]: " (.getMessage r)
                           " [" (.getSourceClassName r)
                           " " (.getSourceMethodName r)
                           "]" \newline))))))
    ([formatter] 
     (doseq [h (.getHandlers logger)]
       (.setFormatter h formatter))))
  
  (defn log-wait
    "Waits for logging to finish."
    []
    (await a)))

(defmacro debug
  "Debugging shorthand.
  Will evaluate and log debug info about expr.
  You must call (log-level :fine) to see the output."
  [expr]
  `(let [a# ~expr] (log :fine "DEBUG " '~expr "=" a#) a#))

; http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
(defn logging-output-stream
  "Creates a logging output stream useful for capturing stdout and stderr."
  [level]
  (PrintStream.
    (proxy [ByteArrayOutputStream] []
      (flush []
        (proxy-super flush)
        (let [s (.trim (.toString this))]
          (proxy-super reset)
          (if (> (.length s) 0)
            (log level s)))))
    true))

(defmacro log-capture
  "Captures stderr and stdout for logging.
  Multiple thread access can still mix up output,
  and the thread/function will not be correct.
  Prefer to still use log instead of print,
  this is just to catch unexpected output."
  [& body]
  `(let [new-out# (logging-output-stream :info)
         new-err# (logging-output-stream :warning)
         old-out# (System/out)
         old-err# (System/err)]
     (System/setOut new-out#)
     (System/setErr new-err#)
     (binding [*out* (PrintWriter. new-out# true)
               *err* (PrintWriter. new-err# true)]
       (let [result# (do ~@body)]
         (log-wait)
         (System/setOut old-out#)
         (System/setErr old-err#)
         result#))))

(defmacro logged-future
  "Capture exception of a long running future for logging."
  [& body]
  `(future (try ~@body (catch Exception e# (log :severe e#)))))
