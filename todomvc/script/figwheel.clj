(require
 '[figwheel-sidecar.repl-api :as ra]
 '[com.stuartsierra.component :as component]
 '[todomvc.system :as system])

(def figwheel-config
  {:figwheel-options {}
   :build-ids ["dev"]
   :all-builds
   [{:id "dev"
     :figwheel true
     :source-paths ["src/cljs"]
     :compiler {:main 'todomvc.core
                :asset-path "/js"
                :output-to "resources/public/js/app.js"
                :output-dir "resources/public/js"
                :optimizations :none
                :static-fns true
                :optimize-constants true
                :pretty-print true
                :externs ["src/js/externs.js"]
                :closure-defines '{goog.DEBUG true}
                :verbose true}}]})

(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (ra/start-figwheel! config)
    config)
  (stop [config]
    (ra/stop-figwheel!)
    config))

(def sys
  (atom
   (component/system-map
    :figwheel (map->Figwheel figwheel-config)
    :app-server (system/dev-system
                 {:db-uri   "datomic:mem://localhost:4334/todos"
                  :web-port 8081}))))

(defn start []
  (swap! sys component/start))

(defn stop []
  (swap! sys component/stop))

(defn reload []
  (stop)
  (start))

(defn repl []
    (ra/cljs-repl))

;;lein run -m clojure.main --init script/figwheel.clj --repl
