(ns url-shortener.iapi
  (:require [url-shortener.core :as core]
            [url-shortener.db :as url-store]))

;; internal API
(defn shorten!
  ([conn url]
   (let [id (core/int->id (url-store/next-counter-val conn))]
     (or (shorten! conn url id)
         (recur conn url))))
  ([conn url id]
   (url-store/save conn id url)))

(defn url-for [conn id]
  (url-store/find-by-id conn id))

(defn list-all [conn]
  (url-store/list-all conn))
