(ns cljeg.web
  (:use compojure)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.pprint)
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

(def examples (ref {}))
(try (with-open [r (java.io.PushbackReader. (reader "examples.clj"))]
       (let [e (read r)]
         (dosync (ref-set examples e))))
  (catch Exception e))

(defn home
  []
  (html-doc "Home"
            [:a {:href "/cljeg/add"} "Add an example"] [:br]
            @examples [:br]
            [:a {:href "/cljeg/add"} "Add an example"] [:br]))

(defn add-example-form
  []
  (html-doc "Add"
            (form-to [:post "/cljeg/add"]
                     "For:" (text-field {:size 10} :for) [:br]
                     "Example:" (text-field {:size 50} :example) [:br]
                     "Result:" (text-field {:size 50} :result) [:br]
                     (submit-button "submit"))))

(defn add-example
  [for example result]
  (let [f (read-string for)
        e (read-string example)
        r (read-string result)]
    (dosync (alter examples #(assoc %1 f (conj (%1 f []) %2))
                   {:example e, :result r, :vote-ups 0, :vote-downs 0}))
    (with-open [w (writer "examples.clj")]
      (pprint @examples w)))
  (html-doc "Thanks" "Thanks, your example was added."))
#_(add-example "defn" "(defn foo [] 1)" "1")

(defroutes cljeg-web
           (GET "/cljeg/" (home))
           (GET "/cljeg/add" (add-example-form))
           (POST "/cljeg/add" (add-example
                          (:for params)
                          (:example params)
                          (:result params)))
           (ANY "*" (page-not-found)))

(defservice cljeg-web)

#_(run-server {:port 8080}
            "/*" (servlet webservice))

