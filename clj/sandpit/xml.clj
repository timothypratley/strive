(use 'clojure.contrib.zip-filter.xml)       
(defn format-name
  [surname given suffix]
  (str surname (if given (str ", " given)) (if suffix (str ", " suffix))))
(defn get-names
  [xz]
  (map (juxt
         #(xml1-> % :surname text)
         #(xml1-> % :given_name text)
         #(xml1-> % :suffix text))
     (xml-> xz :publication :contributors :person_name)))
(let [x (clojure.zip/xml-zip (clojure.xml/parse "foo.xml"))]
  (map (partial apply format-name) (get-names x)))

