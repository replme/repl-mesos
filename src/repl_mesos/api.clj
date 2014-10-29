(ns repl-mesos.api
  (:require [repl-mesos.state :refer [get-all get-one update-one delete-one]]
            [com.stuartsierra.component :as component]
            [liberator.core :refer [resource]]
            [liberator.dev :refer [wrap-trace]]
            [cognitect.transit :as transit]
            [io.clojure.liberator-transit]
            [ring.middleware.transit :refer [wrap-transit-body]]
            [org.httpkit.server :refer [run-server]]
            [compojure.core :refer [routes ANY]]))

(defn parse-body
  [ctx key]
  {key (-> (get-in ctx [:request :body])
           (transit/reader (::content ctx))
           (transit/read))})

(defn check-content
  [ctx key]
  (if (#{:post :put} (get-in ctx [:request :request-method]))
    (let [content (get-in ctx [:request :headers "content-type"])]
      (condp = content
        "application/transit+json" [true {key :json}]
        "application/transit+msgpack" [true {key :msgpack}]
        :else [false {:message "Unspported Content-Type"}]))
    true))

(defn repls
  [state]
  (resource
   :allowed-methods [:get :post]
   :available-media-types ["application/transit+msgpack"
                           "application/transit+json"]
   :known-content-type? (fn [ctx] (check-content ctx ::content))
   :processable? (fn [ctx] (parse-body ctx ::data))
   :post! (fn [ctx]
            (let [id (java.util.UUID/randomUUID)
                  data (assoc (::data ctx) :id id :status "creating")]
              (when (update-one state (str id) data)
                {::entry data})))
   :handle-created ::entry
   :handle-ok (fn [ctx] (get-all state))))

(defn repl
  [state id]
  (resource
   :allowed-method [:get :put :delete]
   :available-media-types ["application/transit+msgpack"
                           "application/transit+json"]
   :handle-ok "Happ BIRTHDAY"))

(defn router
  [state]
  (-> (routes
       (ANY "/repls" [] (repls state))
       (ANY "/repls/:id" [id] (repl state id)))
      (wrap-trace :header :ui)))

(defn start-server
  [port {:keys [store]}]
  (run-server (router store) {:port port}))

(defrecord API [port state server]
  component/Lifecycle
  (start [component]
    (if server
      component
      (assoc component :server (start-server port state))))
  (stop [component]
    (if-not server
      component
      (do (server) (dissoc component :server)))))

(defn new-api
  [port]
  (map->API {:port port}))
