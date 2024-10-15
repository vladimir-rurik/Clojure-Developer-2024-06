(ns url-shortener.main
  (:require [ring.adapter.jetty :as jetty]
            [url-shortener.web :as web]
            [url-shortener.db :as db])
  (:import [org.eclipse.jetty.server Server]))

(set! *warn-on-reflection* true)

(defn start-server
  ([] (start-server {}))
  ([opts]
   (let [server (jetty/run-jetty #'web/handler opts)]
     (println "Server started on port:" (:port opts 80))
     server)))

(defn stop-server [server]
  (.stop ^Server server)
  (println "Server stopped"))

(defn -main [& _]
  (db/init-db)
  (let [server (start-server)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. ^Runnable #(stop-server server)))))
