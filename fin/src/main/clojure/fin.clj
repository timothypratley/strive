(ns fin
  (:gen-class)
  (:require [clojure.contrib.duck-streams :as ds]))

(declare history current)

(defn -main [& args]
  (println "Loading fin...")
  (println (history "RIO.AX"))
  (println (current "RIO.AX")))

(defn history [stock]
  (ds/read-lines
    (java.net.URL. (str "http://ichart.finance.yahoo.com/table.csv?s="
                        stock "&a=0&b=1&c=2003&ignore=.csv"))))

; full list at http://www.gummy-stuff.org/Yahoo-data.htm
(defn current [stock]
  (ds/read-lines
      (java.net.URL. (str "http://finance.yahoo.com/d/quotes.csv?s="
                          stock "&f=snd1l1yrr2r5r6r7"))))

