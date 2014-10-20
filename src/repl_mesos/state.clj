(ns repl-mesos.state
  (:require [com.stuartsierra.component :as component]
            [cognitect.transit :as transit])
  (:import [org.apache.mesos.state Variable State InMemoryState]
           [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn create-state
  [type & [config]]
  (condp = type
    :memory (InMemoryState.)))

(defn- fetch
  [^State state name]
  (.fetch state name))

(defn- write
  [^State state ^Variable var]
  (.store state var))

(defn- expunge
  [^State state ^Variable var]
  (.expunge state var))

(defn- names
  [^State state]
  (.names state))

(defn- value
  [^Variable var]
  (.value var))

(defn- mutate!
  [^Variable var #^bytes data]
  (.mutate var data))

(defn- var->clj
  [^Variable var]
  (-> (value var)
      (ByteArrayInputStream.)
      (transit/read :msgpack)))

(defn get-all
  [state]
  (->> (names state)
       (map fetch)
       (map var->clj)))

(defn get-one
  [state name]
  (var->clj (fetch state name)))

(defn update-var
  [state name data]
  (let [var (fetch state name)
        out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :msgpack)]
    (->> (.toByteArray (transit/write writer data))
         (mutate! var)
         (write state))))

(defn delete
  [state name]
  (expunge (fetch state name)))

(defrecord MesosState [type config store]
  component/Lifecycle
  (start [component]
    (if store
      component
      (assoc component :store (create-state type config))))
  (stop [component]
    (if-not store
      component
      (dissoc component :store))))

(defn new-state
  [state-type state-config]
  (map->MesosState {:type state-type :config state-config}))
