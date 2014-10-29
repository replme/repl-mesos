(ns repl-mesos.api-test
  (:require [repl-mesos.api :refer :all]
            [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [cognitect.transit :as transit]
            [repl-mesos.state :refer [create-state]])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn test-router
  []
  (router (create-state :memory)))

(defn make-transit
  ([msg]
     (make-transit msg :format :msgpack))
  ([msg & {:keys [format]}]
     (let [out (ByteArrayOutputStream. 4096)
           writer (transit/writer out format)]
       (transit/write writer msg)
       (.toByteArray out))))

(defn get-transit
  ([msg]
     (get-transit msg :format :msgpack))
  ([msg & {:keys [format]}]
     (transit/read (transit/reader msg format))))

(deftest post-test
  (let [handler (test-router)
        response (handler (-> (request :post "/repls")
                              (body (make-transit {:repo ""}))
                              (content-type "application/transit+msgpack")))
        response-body (get-transit (:body response))]
    (testing "response status is created"
      (is (= 201 (:status response))))
    (testing "response body contains a uuid id"
      (is (= java.util.UUID (type (:id response-body)))))
    (testing "response body should have creating status"
      (is (= "creating" (:status response-body))))))

(deftest get-col-test
  (let [handler (test-router)
        response (handler (request :get "/repls"))
        response-body (get-transit (:body response))]
    (testing "response status is ok"
      (is (= 200 (:status response))))
    (testing "response body contains a uuid id for each entry"
      (doseq [resource response-body]
        (is (= java.util.UUID (type (:id resource))))))
    (testing "response body is a vector"
      (is (vector? response-body)))))
