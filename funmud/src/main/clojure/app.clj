(ns app
  (:require [netty :as n])
  (:gen-class))

(defn -main [& args]
  (println "This is the main function for app.")
  (n/start 8080 (n/make-handler)))
