(ns name.choi.joshua.fnparse.json
  (:use name.choi.joshua.fnparse))

(defstruct state-s :remainder :column :line)
(def remainder-a
  (accessor state-s :remainder))

(defn- nb-char [subrule]
  (invisi-conc subrule (update-info :column inc)))
(def nb-char-lit
  (comp nb-char lit)) 
(defn- b-char [subrule]
  (invisi-conc subrule (update-info :line inc)))

(deferror parse-error [] [state message message-args]
  {:msg (str (format "JSON error at line %s, column %s: "
               (:line state) (:column state))
             (apply format message message-args))
   :unhandled (throw-msg Exception)})

(defn- expectation-error-fn [expectation]
  (fn [remainder state]
    (raise parse-error state "%s expected where \"%s\" is"
      [expectation (or (first remainder) "the end of the file")])))


(def string-delimiter (nb-char-lit \"))
(def escape-indicator (nb-char-lit \\))
(def false-lit (constant-semantics (lit-conc-seq "false" nb-char-lit) false))
(def true-lit (constant-semantics (lit-conc-seq "true" nb-char-lit) true))
(def nil-lit (constant-semantics (lit-conc-seq "nil" nb-char-lit) nil))
(def keyword-lit (alt false-lit true-lit null-lit))
(def space (nb-char-lit \space \,))
(def tab (nb-char-lit \tab))
(def newline-lit (lit \newline))
(def return-lit (lit \return))
(def line-break (b-char (rep+ (alt newline-lit return-lit))))
(def json-char (alt line-break (nb-char anything)))
(def ws (constant-semantics (rep* (alt space tab line-break)) :ws))
(def begin-list
  (constant-semantics (conc ws (nb-char-lit \() ws) :begin-list))
(def end-list
  (constant-semantics (conc ws (nb-char-lit \)) ws) :end-list))
(def begin-vector
  (constant-semantics (conc ws (nb-char-lit \[) ws) :begin-vector))
(def end-vector
  (constant-semantics (conc ws (nb-char-lit \]) ws) :end-vector))
(def begin-hashmap
  (constant-semantics (conc ws (nb-char-lit \{) ws) :begin-hashmap))
(def end-hashmap
  (constant-semantics (conc ws (nb-char-lit \}) ws) :end-hashmap))
(def keyword-separator
  (constant-semantics (conc ws (nb-char-lit \:) ws) :keyword-separator))
(def minus-sign (nb-char-lit \-))
(def plus-sign (nb-char-lit \+))
(def decimal-point (nb-char-lit \.))
(def exponential-sign (lit-alt-seq "eE" nb-char-lit))
(def zero-digit (nb-char-lit \0))
(def nonzero-decimal-digit (lit-alt-seq "123456789" nb-char-lit))
(def decimal-digit (alt zero-digit nonzero-decimal-digit))
(def fractional-part (conc decimal-point (rep* decimal-digit)))
(def exponential-part
  (conc exponential-sign (opt (alt plus-sign minus-sign))
        (failpoint (rep+ decimal-digit)
          (expectation-error-fn
            (str "in number literal, after an exponent sign, decimal"
                 "digit")))))
(def number-lit
  (complex [minus (opt minus-sign)
            above-one (alt zero-digit (rep+ nonzero-decimal-digit))
            below-one (opt fractional-part)
            power (opt exponential-part)]
    (-> [minus above-one below-one power] flatten apply-str
      Double/parseDouble
      ((if (or below-one power) identity int))
      make-scalar-node)))
(def hexadecimal-digit
  (alt decimal-digit (lit-alt-seq "ABCDEF" nb-char-lit)))
(def unescaped-char
  (except json-char (alt escape-indicator string-delimiter)))
(def unicode-char-sequence
  (complex [_ (nb-char-lit \u)
              digits (factor= 4
                       (failpoint hexadecimal-digit
                         (expectation-error-fn "hexadecimal digit")))]
    (-> digits apply-str (Integer/parseInt 16) char)))
(def escaped-characters
  {\\ \\, \/ \/, \b \backspace, \f \formfeed, \n \newline, \r \return,
   \t \tab})

(def normal-escape-sequence
  (semantics (lit-alt-seq (keys escaped-characters) nb-char-lit)
    escaped-characters))

(def escape-sequence
  (complex [_ escape-indicator
            character (alt unicode-char-sequence
                           normal-escape-sequence)]
    character))

(def string-char (alt escape-sequence unescaped-char))

(def string-lit
  (complex [_ string-delimiter
            contents (rep* string-char)
            _ string-delimiter]
    (-> contents apply-str make-scalar-node)))

(declare array)

(declare object)

(def value (alt string-lit number-lit keyword-lit array object))

(def additional-value
  (complex [_ value-separator, content value] content))

(def array-contents
  (complex [first-value value, rest-values (rep* additional-value)]
    (cons first-value rest-values)))

(def array
  (complex [_ begin-array
            contents (opt array-contents)
            _ (failpoint end-array
                (expectation-error-fn "an array is unclosed; \"]\""))]
    (-> contents vec make-array-node)))

(def entry
  (complex [entry-key string-lit, _ name-separator, entry-val value]
    [entry-key entry-val]))

(def additional-entry
  (complex [_ value-separator, content entry]
    content))

(def object-contents
  (complex [first-entry entry, rest-entries (rep* additional-entry)]
    (cons first-entry rest-entries)))

(def object
  (complex [_ begin-object
            contents object-contents
            _ (failpoint end-object
                (expectation-error-fn
                  (str "either \"}\" or another object entry (which "
                       "always starts with a string)")))]
    (struct node-s :object (into {} contents))))

(def text (alt object array)) ; The root rule

;; The functions below uses the rules to parse strings.

(defn parse [tokens]
  (binding [*remainder-accessor* remainder-a]
    (rule-match text
      #(raise parse-error "invalid document \"%s\""
         (apply-str (remainder-a %)))
      #(raise parse-error "leftover data after a valid node \"%s\""
         (apply-str (remainder-a %2)))
      (struct state-s tokens 0 0))))

(defmulti represent :kind)
(defmethod represent :object [node]
  (into {}
    (map #(vector (represent (key %)) (represent (val %)))
      (:content node))))
(defmethod represent :array [node]
  (vec (map #(represent %) (:content node))))
(defmethod represent :scalar [node]
  (:content node))

(def load-stream (comp represent parse))

