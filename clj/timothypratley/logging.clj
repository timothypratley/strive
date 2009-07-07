(ns timothypratley.logging
  (:use [clojure.contrib.str-utils :only (re-sub)])
  (:import (java.util.logging Logger Level Formatter ConsoleHandler)
     (java.text SimpleDateFormat)
     (java.util Date)))

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
  "Returns a string, the name of the current Clojure function"
  []
  `(-> (Throwable.) .getStackTrace first .getClassName unmangle))

(defmacro defn-doc
  "TODO: fix me"
  [fun doc-str & body]
  `(defn ~(str doc-str) ~fun ~@body))

(defmacro str-now
  [s]
  `~(str s))

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
    "Log a message."
    [level c f & text]
    (send-off a (fn [_]
                  (.logp logger (m level) c f (apply str text)))))

  (defmacro log
    "Log a message.
    Buffers messages sequentially so multiple thread logs will not overlap.
    level should be one of:
    (keys m)"
    [level & text]
    `(log* ~level
           (.getName (Thread/currentThread))
           (current-function-name)
           ~@text))

  (defn log-level
    "Sets the current log level to one of:
    (keys m)"
    [level]
    (.setLevel logger (m level))
    (doseq [h (.getHandlers logger)]
      (.setLevel h (m level))))

  (defn log-format
    "Sets the log formatter"
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
       (.setFormatter h formatter)))))

(defmacro debug
  "Debugging shorthand.
  Will evaluate and log debug info about expr.
  NB: you must (log-level :fine) to see the output."
  [expr]
  `(let [a# ~expr] (log :fine "DEBUG:" '~expr "=" a#) a#))

