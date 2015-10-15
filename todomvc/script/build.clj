(require '[cljs.build.api :as b])

(b/build "src/cljs"
  {:output-to     "resources/public/js/app.js"
   :optimizations :advanced
   :output-dir    "resources/public/js"
   :verbose       true})

(System/exit 0)