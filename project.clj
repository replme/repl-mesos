(defproject repl-mesos "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GNU Affero General Public License v3"
            :url "https://www.gnu.org/licenses/agpl-3.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [com.stuartsierra/component "0.2.2"]
                 [clj-mesos "0.20.4"]
                 [http-kit "2.1.18"]
                 [liberator "0.12.0"]
                 [compojure "1.1.3"]
                 [io.clojure/liberator-transit "0.3.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[org.clojure/tools.namespace "0.2.3"]
                        [cider/cider-nrepl "0.8.0-SNAPSHOT"]]}})

