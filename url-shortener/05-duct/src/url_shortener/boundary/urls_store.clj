(ns url-shortener.boundary.urls-store
  (:require [duct.database.sql]
            [honey.sql :as sql]
            [next.jdbc.sql :as jdbc.sql]
            [next.jdbc.result-set :refer [as-unqualified-lower-maps]])
  (:import [org.h2.jdbc JdbcSQLIntegrityConstraintViolationException]))


(defprotocol URLs
  (list-all         [this])
  (find-by-id       [this id])
  (save             [this id url])
  (next-counter-val [this]))


(def ^:private all-urls-sql
  (sql/format {:select [:id :url]
               :from :urls}))

(def ^:private next-val-sql
  ["select nextval('counter')"])

(extend-protocol URLs
  duct.database.sql.Boundary
  
  (next-counter-val [{db :spec}]
    (-> (jdbc.sql/query db next-val-sql)
        (first)
        :nextval))
  
  (list-all [{db :spec}]
    (jdbc.sql/query db all-urls-sql {:builder-fn as-unqualified-lower-maps}))
  
  (find-by-id [{db :spec} id]
    (-> (jdbc.sql/get-by-id db :urls id)
        :urls/url))
  
  (save [{db :spec} id url]
    (try
      (-> (jdbc.sql/insert! db :urls {:id id :url url})
          :urls/id)
      (catch JdbcSQLIntegrityConstraintViolationException _
        nil))))
