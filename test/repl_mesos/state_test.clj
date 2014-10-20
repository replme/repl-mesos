(ns repl-mesos.state-test
  (:require [clojure.test :refer :all]
            [repl-mesos.state :refer :all]))

(deftest update-one-test
  (let [test-state (create-state :memory)]
    (testing "Writes to MesosState with the given key"
      (update-one test-state "asdfasdf" {:asdfasdf :aardvark})
      (is (= {:asdfasdf :aardvark} (get-one test-state "asdfasdf"))))))

(deftest get-all-test
  (let [test-state (create-state :memory)]
    (doseq [n (range 0 10)]
      (update-one test-state (str "test-" n) {:test "Test" :test1 n}))
    (testing "Returns all 10 state values"
      (is (= 10 (count (get-all test-state)))))
    
    (testing "Converts msgpack into clojure data"
      (is (= "Test" (:test (first (get-all test-state))))))))

(deftest delete-one-test
  (let [test-state (create-state :memory)]
    (update-one test-state "asdfasdf" "asdfasdf")
    (delete-one test-state "asdfasdf")
    (is (= nil (get-one test-state "asdfasdf")))))
