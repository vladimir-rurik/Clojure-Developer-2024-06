(ns spec-faker.core-test
  (:require [clojure.test :refer :all]
            [spec-faker.core :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]))

;; Helper function to parse JSON response body
(defn parse-body [response]
  (json/parse-string (:body response) keyword))

(deftest random-data-test
  (testing "GET /api/random returns valid person data"
    (let [response (app (mock/request :get "/api/random"))
          body (parse-body response)]
      (is (= 200 (:status response)))
      (is (string? (:name body)))
      (is (number? (:age body)))
      (is (string? (:email body))))))

(deftest sample-data-test
  (testing "POST /api/generate returns data matching sample structure"
    (let [sample {:name "John" :age 25 :email "john@example.com"}
          response (app (-> (mock/request :post "/api/generate")
                            (mock/json-body sample)))
          body (parse-body response)]
      (is (= 200 (:status response)))
      (is (string? (:name body)))
      (is (number? (:age body)))
      (is (string? (:email body))))))

(deftest swagger-docs-test
  (testing "GET /api/docs returns swagger documentation"
    (let [response (app (mock/request :get "/api/docs"))
          body (parse-body response)]
      (is (= "2.0" (:swagger body)))
      (is (= "Spec Faker API" (get-in body [:info :title]))))))

(deftest not-found-test
  (testing "Non-existent route returns 404"
    (let [response (app (mock/request :get "/non-existent"))]
      (is (= 404 (:status response))))))