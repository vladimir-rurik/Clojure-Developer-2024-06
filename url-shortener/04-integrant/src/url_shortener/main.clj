(ns url-shortener.main
  (:require [url-shortener.db :as db]
            [url-shortener.web] ; should be here to load ns.
            [integrant.core :as ig]))

(set! *warn-on-reflection* true)

(defn -main [& _]
  (let [config (ig/read-string (slurp "config.edn"))
        system (-> config ig/prep ig/init)]
    (db/init-db (::db/db system))
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. ^Runnable #(ig/halt! system)))))
