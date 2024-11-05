(ns spec-faker.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.generators :as tcgen]
            [cheshire.core :as json]
            [ring.util.response :refer [response]])
  (:gen-class))

;; Custom generators
(def name-generator
  (tcgen/elements ["John" "Jane" "Bob" "Alice" "Mike" "Sarah" "Tom" "Emily"]))

(def email-generator
  (tcgen/fmap
   (fn [[name domain tld]]
     (str name "@" domain "." tld))
   (tcgen/tuple
    (tcgen/elements ["john" "jane" "bob" "alice" "mike" "sarah" "tom" "emily"])
    (tcgen/elements ["gmail" "yahoo" "hotmail" "example"])
    (tcgen/elements ["com" "org" "net"]))))

;; Define specs with custom generators
(s/def ::name (s/spec string? :gen (fn [] name-generator)))
(s/def ::age (s/int-in 18 100))
(s/def ::email (s/spec string? :gen (fn [] email-generator)))
(s/def ::person (s/keys :req-un [::name ::age ::email]))

;; Generate sample data based on specs
(defn generate-random-person []
  (gen/generate (s/gen ::person)))

;; Generate data based on provided sample
(defn generate-from-sample [sample]
  (let [generators {:name name-generator
                    :age (tcgen/choose 18 100)
                    :email email-generator}]
    (into {}
          (for [[k _] sample]
            [k (gen/generate (get generators k (tcgen/return "default")))]))))

;; Routes handlers
(defn get-random-data [_]
  (response (generate-random-person)))

(defn post-sample-data [request]
  (let [sample (get-in request [:body])]
    (response (generate-from-sample sample))))

;; Swagger documentation
(def swagger-docs
  {:swagger "2.0"
   :info {:title "Spec Faker API"
          :description "API for generating fake data using clojure.spec"
          :version "1.0.0"}
   :paths {"/api/random" {:get {:summary "Get random data"
                                :produces ["application/json"]
                                :responses {200 {:description "Random person data"
                                                 :schema {:type "object"
                                                          :properties {:name {:type "string"}
                                                                       :age {:type "integer"
                                                                             :minimum 18
                                                                             :maximum 100}
                                                                       :email {:type "string"}}
                                                          :required ["name" "age" "email"]}}}}}
           "/api/generate" {:post {:summary "Generate data based on sample"
                                   :produces ["application/json"]
                                   :consumes ["application/json"]
                                   :parameters [{:in "body"
                                                 :name "sample"
                                                 :description "Sample data structure"
                                                 :required true
                                                 :schema {:type "object"
                                                          :properties {:name {:type "string"}
                                                                       :age {:type "integer"}
                                                                       :email {:type "string"}}}}]
                                   :responses {200 {:description "Generated data based on sample"}}}}}})

(defroutes app-routes
  (GET "/api/random" [] get-random-data)
  (POST "/api/generate" request post-sample-data)
  (GET "/api/docs" [] (response swagger-docs))
  (route/not-found (response {:error "Not Found"})))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getProperty "port")
                                   (System/getenv "PORT")
                                   "8001"))]
    (println (str "Starting server on port " port))
    (run-jetty app {:port port :join? false})))