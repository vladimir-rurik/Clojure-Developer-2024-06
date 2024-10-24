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
  ([conn url]
   (let [id (iapi/shorten! conn url)]
     (ring.util.response/created id {:id id})))
  ([conn url id]
   (if-let [id (iapi/shorten! conn url id)]
     (ring.util.response/created id {:id id})
     {:status 409 :body {:error (format "Short URL %s is already taken" id)}})))

(defmethod ig/init-key ::handler
  [_ {:keys [db]}]
  (compojure/defroutes router
    (compojure/GET "/" []
      (ring.util.response/resource-response "index.html" {:root "public"}))
    
    (compojure/POST "/" [url]
      (with-open [conn (jdbc/get-connection db)]
        (if (empty? url)
          (ring.util.response/bad-request {:error "No `url` parameter provided."})
          (retain conn url))))
    
    (compojure/PUT "/:id" [id url]
      (with-open [conn (jdbc/get-connection db)]
        (if (empty? url)
          (ring.util.response/bad-request {:error "No `url` parameter provided"})
          (retain conn url id))))
    
    (compojure/GET "/:id" [id]
      (with-open [conn (jdbc/get-connection db)]
        (if-let [url (iapi/url-for conn id)]
          (ring.util.response/redirect url)
          (ring.util.response/not-found {:error "Requested URL not fount."}))))
    
    (compojure/GET "/list/" []
      (with-open [conn (jdbc/get-connection db)]
        (ring.util.response/response {:urls (iapi/list-all conn)}))))
  
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
