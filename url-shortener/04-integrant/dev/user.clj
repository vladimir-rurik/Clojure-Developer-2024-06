(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [integrant.core :as ig]
            [integrant.repl :refer [halt go reset]]
            [integrant.repl.state :refer [config system]]
            [url-shortener.db :as db])
  (:import [org.slf4j.simple SimpleLogger]))

(System/setProperty SimpleLogger/DEFAULT_LOG_LEVEL_KEY "warn")

(clojure.tools.namespace.repl/set-refresh-dirs "src")

(defn read-config []
  (ig/read-string (slurp "config.edn")))

(integrant.repl/set-prep! #(ig/prep (read-config)))

(comment
  (refresh-all)
  
  config   ; to check eventual config
  system   ; to check current system
  
  ;; system's lifecycle
  (go)     ; prep and init — start the system
  (reset)  ; halt the system, refresh all changed code (with tools.namespace), start the system again
  (halt)   ; stop the system
  )

(comment ; test web api
  (require '[clojure.java.shell :refer [sh]])

  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/"
      "-d" "{\"url\": \"https://clojurescript.org/\"}")

  (sh "curl" "-X" "PUT"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/clj"
      "-d" "{\"url\": \"https://clojure.org/\"}")

  (sh "curl" "-i" "http://localhost:8000/clj")

  (sh "curl" "http://localhost:8000/list/"))

(comment ;; test handler
  (let [test-config {:url-shortener.db/db {:jdbc-url "jdbc:h2:./database-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"}
                     
                     :url-shortener.web/handler
                     {:db (ig/ref :url-shortener.db/db)}}
        test-sys (-> test-config ig/init)
        db       (:url-shortener.db/db test-sys)
        handler  (:url-shortener.web/handler test-sys)]
    (db/init-db db)
    
    (prn (handler {:uri "/list/"
                   :request-method :get}))
    (ig/halt! test-sys))
  )
