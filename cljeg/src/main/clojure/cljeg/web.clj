(ns cljeg.web
  (:use compojure)
  (:use clojure.contrib.pprint)
  (:use clojure.contrib.duck-streams)
  (:use cljeg.example)
  (:use cljeg.util)
  (:gen-class :extends javax.servlet.http.HttpServlet))

(defn html-doc
  [title & body]
  (html (doctype :html4)
        [:html
         [:head
          [:title (str title " - Clojure Examples")]]
         [:body
          [:div
           [:h2
            [:a {:href "/cljeg"} "Home"]]]
          body]]))

(def submitted-examples (ref {}))
(try
  (let [es (load-examples "submitted_examples.clj")]
    (dosync (ref-set submitted-examples es)))
  (catch Exception e
    (println "Could not load submitted_examples.clj" e)))

(defn home
  []
  (html-doc "Home"
            [:a {:href "/cljeg/add"} "Add an example"] [:br]
            @submitted-examples [:br]
            [:a {:href "/cljeg/add"} "Add an example"] [:br]))

(defn add-example-form
  []
  (html-doc "Add"
            (form-to [:post "/cljeg/add"]
                     "For:" (text-field {:size 10} :for) [:br]
                     "Example:" (text-field {:size 50} :example) [:br]
                     "Result:" (text-field {:size 50} :result) [:br]
                     (submit-button "submit"))))

(defn add-submitted-example
  [for example result]
  (let [f (read-string for)
        e (read-string example)
        r (read-string result)]
    (dosync (alter submitted-examples #(assoc %1 f (conj (%1 f []) %2))
                   {:example e, :result r, :vote-ups 0, :vote-downs 0}))
    (with-open [w (writer "submitted_examples.clj")]
      (pprint @submitted-examples w))
    (save-examples-from-map @submitted-examples))
  (html-doc "Thanks" "Thanks, your example was added."))
#_(add-submitted-example "defn" "(defn foo [] 1)" "1")

(defroutes cljeg-web
           (GET "/cljeg/" (home))
           (GET "/cljeg/add" (add-example-form))
           (POST "/cljeg/add" (add-submitted-example
                          (:for params)
                          (:example params)
                          (:result params)))
           (ANY "*" (page-not-found)))

(defservice cljeg-web)

#_(run-server {:port 8080}
            "/*" (servlet webservice))

