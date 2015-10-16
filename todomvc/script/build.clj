(require '[cljs.build.api :as b])

(b/build "src/cljs"
  {:output-to     "resources/public/js/app.js"
   :optimizations :advanced
   :pretty-print  true
   :pseudo-names  true
   :output-dir    "resources/public/js"
   :verbose       true})

(System/exit 0)