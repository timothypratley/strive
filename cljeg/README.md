CLJEG is intended to provide examples for [Clojure](http://clojure.org),
and a framework for user examples.

CLJEG contains
* Macros and functions for creating and using examples
* Reference examples from the wikibook
* A simple web application to allow people to submit examples


Goals
-----

* Define examples in one place once.
* Automatically part of test suite.
* Automatically included in documentation.
* Define examples in the code.

* Extension of clojure.test to identify examles for docs and save as meta data.
* Need to keep the original form intact for documentation purposes.


TODO
----

* Docstring at front might work better? Note currently accepts pred,
  or pred and value which does not work with optional at end as per deftest.
* Integrate with autodoc to include example(s) as part of output.
* Some forms does not seem to work.
* Copied all from wiki, needs some improvement still.
* New features examples todo.
* Coverage tests to check what is missing.


Building
--------

To build the sources
	mvn clojure:compile

To run the tests
	mvn clojure:test

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

And you can view the appliation at
[http://localhost:8080/cljeg](http://localhost:8080/cljeg)

Or if you want to deploy it, you can create a WAR like so:
	mvn install
Then look in the target directory for cljeg-1.0-SNAPSHOT.war

