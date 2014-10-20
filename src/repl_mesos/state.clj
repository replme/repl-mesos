(ns repl-mesos.state
  (:require [com.stuartsierra.component :as component])
  (:import [org.apache.mesos.state Variable State InMemoryState]))

(defn create-state
  [type & [config]]
  (condp = type
    :memory (InMemoryState.)))

(defn fetch
  [^State state name]
  (.fetch state name))

(defn write
  [^State state ^Variable var]
  (.store state var))

(defn expunge
  [^State state ^Variable var]
  (.expunge state var))

(defn names
  [^State state]
  (.names state))

(defn value
  [^Variable var]
  (.value var))

(defn mutate!
  [^Variable var #^bytes data])

(defn get-all
  [state]
  (map fetch (names state)))

(defn get-one
  [state name]
  (fetch state name))

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
