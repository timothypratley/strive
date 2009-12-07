CLJEG is intended to provide examples for [Clojure](http://clojure.org).
It contains a simple web application to allow people to submit examples,
and some utilities to make use of the data.

To run the web application, you need
[Compojure](http://github.com/weavjester/compojure)
installed to your local Maven repository:
	git clone git://github.com/weavejester/compojure.git
	cd compojure
	ant deps
	ant
	mvn install:install-file \
		-DgroupId=org.clojure -DartifactId=compojure \
		-Dversion=1.0-SNAPSHOT -Dfile=compojure.jar -Dpackaging=jar

Then execute
	mvn jetty:run

And you can view the appliation at http://localhost:8080/cljeg

