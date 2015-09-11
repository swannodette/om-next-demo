(require '[cljs.build.api :as b])
(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])

(def shared-opts
  {:asset-path "/js"
   :output-dir "resources/public/js"
   :verbose    true})

(b/build "src/dev"
  (merge
    {:main      'todomvc.dev
     :output-to "resources/public/js/app.js"}
    shared-opts))

(repl/repl* (browser/repl-env :host-port 8082) shared-opts)
