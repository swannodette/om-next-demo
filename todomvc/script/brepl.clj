(require '[cljs.build.api :as b])
(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])

(def shared-opts
  {:asset-path "/js"
   :output-dir "resources/public/js"})

(b/build (b/inputs "src/dev")
  (merge
    {:main 'todomvc.dev
     :output-to "resources/public/js/demo.js"}
    shared-opts))

(repl/repl* (browser/repl-env :host-port 8081) shared-opts)