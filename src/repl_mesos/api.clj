(ns repl-mesos.api
  (:require [repl-mesos.state :refer [get-all get-one update-one delete-one]]
            [com.stuartsierra.component :as component]
            [liberator.core :refer [resource]]
            [io.clojure.liberator-transit]
            [ring.middleware.transit :refer [wrap-transit-body]]
            [org.httpkit.server :refer [run-server]]
            [compojure.core :refer [routes ANY]]))

(defn repls
  [state]
  (resource
   :allowed-methods [:get :post]
   :available-media-types ["application/transit+msgpack"
                           "application/transit+json"]
   :post! (fn [ctx]
            (let [id (java.util.UUID/randomUUID)
                  data (assoc (get-in ctx [:request :body]) :id id)]
              (when (update-one state id data)
                {::entry data})))
   :handle-created #(::entry %)
   :handle-ok #(get-all state)))

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
       (ANY "/repls/:id" [id] (repls id)))
      (wrap-transit-body)))

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
