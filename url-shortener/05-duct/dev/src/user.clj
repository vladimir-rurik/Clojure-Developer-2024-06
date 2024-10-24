(ns user
  (:import [org.slf4j.simple SimpleLogger]))

(System/setProperty SimpleLogger/DEFAULT_LOG_LEVEL_KEY "warn")

(defn dev
  "Load and switch to the 'dev' namespace."
  []
  (require 'dev)
  (in-ns 'dev)
  :loaded)
