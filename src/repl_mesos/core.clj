(ns repl-mesos.core
  (:require [com.stuartsierra.component :as component]
            [repl-mesos.state :refer [new-state]]
            [repl-mesos.api :refer [new-api]]))

(defn system
  ""
  [{:keys [state state-config port]
    :or [:memory state {} state-config 8081 port]}]
  (component/system-map
   :state (new-state state state-config)
   :api (component/using
         (new-api port)
         [:state])))

(defn -main
  [& config]
  (component/start (system)))
