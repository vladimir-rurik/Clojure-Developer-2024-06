(ns url-shortener.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as jdbc.rs]
            [next.jdbc.sql :as jdbc.sql]
            [honey.sql :as sql])
  (:import [org.h2.jdbc JdbcSQLIntegrityConstraintViolationException]))


(def db-url "jdbc:h2:./database;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")

(def ds (jdbc/get-datasource db-url))

(def ^:private table-urls-ddl
  (sql/format {:create-table [:urls :if-not-exists]
               :with-columns
               [[:id  [:varchar 12]   :primary-key]
                [:url [:varchar 4096] :not-null]]}))

(def ^:private seq-counter-ddl
  ["create sequence if not exists counter start 1"])

(def ^:private next-val-sql
  ["select nextval('counter')"])

(def ^:private all-urls-sql
  (sql/format {:select [:id :url]
               :from :urls}))

(defn init-db []
  (jdbc/with-transaction [tx ds]
    (jdbc/execute-one! tx table-urls-ddl)
    (jdbc/execute-one! tx seq-counter-ddl)))

;; url API
(defn next-counter-val []
  (-> (jdbc/execute-one! ds next-val-sql)
      :nextval))

(defn save [id url]
  (try
    (-> (jdbc.sql/insert! ds :urls {:id id :url url})
        :urls/id)
    (catch JdbcSQLIntegrityConstraintViolationException _
      nil)))

(defn find-by-id [id]
  (-> (jdbc.sql/get-by-id ds :urls id)
      :urls/url))

(defn list-all []
  (jdbc/execute! ds all-urls-sql
                 {:builder-fn jdbc.rs/as-unqualified-lower-maps}))

(comment
  (init-db)
  (next-counter-val)
  (save "a" "ya.ru")
  (save "b" "bayan.ru")
  (find-by-id "a")
  (list-all))

