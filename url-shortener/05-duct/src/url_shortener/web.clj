(ns url-shortener.web
  (:require [ring.middleware.json :as middleware.json]
            [ring.middleware.params :as middleware.params]
            [ring.middleware.resource :as middleware.resource]
            [ring.middleware.keyword-params :as middleware.keyword-params]
            [ring.util.response]
            [compojure.core :as compojure]
            [url-shortener.iapi :as iapi]
            [next.jdbc :as jdbc]
            [integrant.core :as ig]
            [ring.adapter.jetty :as jetty])
  (:import [org.eclipse.jetty.server Server]))

(defn retain
  ([db url]
   (let [id (iapi/shorten! db url)]
     (ring.util.response/created id {:id id})))
  ([db url id]
   (if-let [id (iapi/shorten! db url id)]
     (ring.util.response/created id {:id id})
     {:status 409 :body {:error (format "Short URL %s is already taken" id)}})))

(defmethod ig/init-key ::handler
  [_ {:keys [db]}]
  (compojure/defroutes router
    (compojure/GET "/" []
      (ring.util.response/resource-response "index.html" {:root "public"}))
    
    (compojure/POST "/" [url]
      (if (empty? url)
        (ring.util.response/bad-request {:error "No `url` parameter provided"})
        (retain db url)))
    
    (compojure/PUT "/:id" [id url]
      (if (empty? url)
        (ring.util.response/bad-request {:error "No `url` parameter provided"})
        (retain db url id)))
    
    (compojure/GET "/:id" [id]
      (if-let [url (iapi/url-for db id)]
        (ring.util.response/redirect url)
        (ring.util.response/not-found {:error "Requested URL not fount."})))
    
    (compojure/GET "/list/" []
      (ring.util.response/response {:urls (iapi/list-all db)})))
  
  (-> #'router
      (middleware.resource/wrap-resource "public")
      (middleware.params/wrap-params)
      (middleware.keyword-params/wrap-keyword-params)
      (middleware.json/wrap-json-params)
      (middleware.json/wrap-json-response)))

(defmethod ig/init-key ::server
  [_ {:keys [port handler]}]
  (println "Server started on port:" port)
  (let [server (jetty/run-jetty handler {:port  (or port 3000)
                                         :join? false})]
    server))

(defmethod ig/halt-key! ::server
  [_ server]
  (when server
    (println "Server stopped!")
    (.stop ^Server server)))

(comment
  (require '[clojure.java.shell :refer [sh]])

  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/shorten"
      "-d" "{\"url\": \"https://clojure.org/\"}")

  ;; => {\"url\":\"http://localhost:8000/cWyhdn\"}

  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/shorten"
      "-d" "{\"url\": \"https://bit.ly/\"}")

  ;; => {\"url\":\"http://localhost:8000/d4w07h\"}

  (sh "curl" "http://localhost:8000/cWyhdn")

  (sh "curl" "http://localhost:8000/urls"))
