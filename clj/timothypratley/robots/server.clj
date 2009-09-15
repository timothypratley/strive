(ns timothypratley.robots.server
  (:require [timothypratley.state-server :as protocol]))

(protocol/run-state-server 8888)

