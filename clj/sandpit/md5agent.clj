(ns agentmd5 
  (:refer-clojure) 
  (:import 
     (java.security 
       NoSuchAlgorithmException 
       MessageDigest) 
     (java.math BigInteger))) 

; computes an MD5 sum of a string 
; (http://www.holygoat.co.uk/blog/entry/2009-03-26-1)
(defn md5-sum 
  "Compute the hex MD5 sum of a string." 
  [#^String str] 
  (let [alg (doto (MessageDigest/getInstance "MD5") 
              (.reset) 
              (.update (.getBytes str)))] 
    (try 
      (.toString (new BigInteger 1 (.digest alg)) 16) 
      (catch NoSuchAlgorithmException e 
        (throw (new RuntimeException e)))))) 

; memoized version of MD5
(def md5-memo (memoize md5-sum))

; returns the next iteration of a given character sequence
(defn next-string [#^"[C" chars start-char end-char]
  (loop [i (int (- (alength chars) 1))]
    (if (>= i 0)
      (let [last-char (= (aget #^"[C" chars i) (char end-char))
            recur-val (int (if last-char (- i 1) -1))]
        (if last-char
          (aset chars i (char start-char))
          (aset chars i (char (inc (int (aget chars i))))))
        (recur recur-val))))
  (new String chars))

; returns a sequence of strings given a length, range, and result size
; length=4 start-char=a end-char=z result-size=5 would yield
; ["aaaa" "aaab" "aaac" "aaad" "aaae"]
(defn alpha-gen [length start-char end-char result-size]
  (let [#^"[C" chars (make-array (Character/TYPE) length)]
    ; initialize the array
    (loop [i 0]
      (if (< i length)
        (do (aset chars (int i) (char start-char))
          (recur (inc i)))))
    (for [i (range result-size)]
      (next-string chars start-char end-char))))

; creates an initial cached copy of all character combinations
; subsequent calls return the cached copy rather than calculating
(def string-perms
  (let [xs (ref [])]
    (fn []
      (if (empty? @xs)
        (dosync (ref-set xs (doall (alpha-gen 4 \a \z (Math/pow 26 4)))))
        @xs))))

; cycles through character combinations for a given md5 string 
; until the matching result is found 
(defn decode-md5 [md5] 
  (first (filter #(= md5 (md5-memo %1)) (string-perms))))

; decodes a bucket 
(defn decode [bucket] 
  (doall (map decode-md5 bucket)))

; returns a collection of agents for a given set of work buckets 
(defn spawn-agents [agents buckets] 
  (if (empty? buckets) 
    agents 
    (recur (conj agents (agent (first buckets))) 
           (rest buckets))))

; initialize string permutations
(string-perms)

; number of tasks to doll out to the agents 
(def num-work-units 60) 

; generate requested number of work units 
(def work-units (map md5-sum (for [x (range num-work-units)] "cloj")))

; number of agents to spawn 
(def num-agents 4) 

; divide the units into buckets for each agent 
(def work-buckets (partition (int (/ (count work-units) 
                                     num-agents)) work-units)) 

; create an agent for each bucket 
(def agents (spawn-agents [] work-buckets)) 

; send each agent a job to do 
(doseq [agent agents] 
  (send agent decode)) 

; ensure successful dispatch 
(apply await agents) 

; view the results 
(doseq [agent agents] 
  (doseq [result @agent] (println result)))

; clean up 
(shutdown-agents) 
