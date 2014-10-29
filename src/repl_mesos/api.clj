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
  (if-let [content (::content ctx)]
    {key (-> (get-in ctx [:request :body])
             (transit/reader content)
             (transit/read))}
    true))

(defn check-content
  [ctx key]
  (if (#{:post :put} (get-in ctx [:request :request-method]))
    (let [content (get-in ctx [:request :headers "content-type"])]
      (condp = content
        "application/transit+json" [true {key :json}]
        "application/transit+msgpack" [true {key :msgpack}]
        :else [false {:message "Unspported Content-Type"}]))
    true))

(def resource-defaults
  {:available-media-types ["application/transit+msgpack"
                           "application/transit+json"]
   :known-content-type? (fn [ctx] (check-content ctx ::content))
   :processable? (fn [ctx] (parse-body ctx ::data))})

(defn repls
  [state]
  (resource resource-defaults
            :allowed-methods [:get :post]
            :post! (fn [ctx]
                     (let [id (java.util.UUID/randomUUID)
                           data (assoc (::data ctx) :id id :status "creating")]
                       (when (update-one state (str id) data)
                         {::entry data})))
            :handle-created ::entry
            :handle-ok (fn [ctx] (get-all state))))

(defn repl
  [state id]
  (resource resource-defaults
            :allowed-method [:get :put :delete]
            :available-media-types ["application/transit+msgpack"
                                    "application/transit+json"]
            :exists? (fn [_]
                       (if-let [entry (get-one state id)]
                         {::entry entry}))
            :put! (fn [ctx]
                    (let [data (select-keys (::data ctx) [:repo])
                          entry (merge (get-one state id) data)]
                      (when (update-one state id entry)
                        {::entry entry})))
            :can-put-to-missing? false
            :delete! (fn [ctx] (delete-one state id))
            :handle-no-content ""
            :handle-ok ""))

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
