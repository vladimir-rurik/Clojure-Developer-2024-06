(ns url-shortener.iapi
  (:require [url-shortener.core :as core]
            [url-shortener.boundary.urls-store :as urls-store]))

;; API
(defn shorten!
  ([db url]
   (let [id (core/int->id (urls-store/next-counter-val db))]
     (or (shorten! db url id)
         (recur db url))))
  ([db url id]
   (urls-store/save db id url)))

(defn url-for [db id]
  (prn :db-type (type db))
  (prn :db db)
  (urls-store/find-by-id db id))

(defn list-all [db]
  (prn :db-type (type db))
  (prn :db db)
  (urls-store/list-all db))
