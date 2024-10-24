(ns url-shortener.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as jdbc.rs]
            [next.jdbc.sql :as jdbc.sql]
            [honey.sql :as sql]
            [integrant.core :as ig])
  (:import [org.h2.jdbc JdbcSQLIntegrityConstraintViolationException]))

(defmethod ig/init-key ::db
  [_ {:keys [jdbc-url]}]
  (jdbc/get-datasource jdbc-url))

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

(defn init-db [conn]
  (jdbc/with-transaction [tx conn]
    (jdbc/execute-one! tx table-urls-ddl)
    (jdbc/execute-one! tx seq-counter-ddl)))

;; url API
(defn next-counter-val [conn]
  (-> (jdbc/execute-one! conn next-val-sql)
      :nextval))

(defn save [conn id url]
  (try
    (-> (jdbc.sql/insert! conn :urls {:id id :url url})
        :urls/id)
    (catch JdbcSQLIntegrityConstraintViolationException _
      nil)))

(defn find-by-id [conn id]
  (-> (jdbc.sql/get-by-id conn :urls id)
      :urls/url))

(defn list-all [conn]
  (jdbc/execute! conn all-urls-sql
                 {:builder-fn jdbc.rs/as-unqualified-lower-maps}))
