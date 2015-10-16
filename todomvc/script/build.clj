(require '[cljs.build.api :as b])

(b/build "src/cljs"
  {:output-to     "resources/public/js/app.js"
   :output-dir    "resources/public/js"
   :optimizations :advanced
   :externs       ["src/js/externs.js"]
   :pretty-print  true
   :pseudo-names  true
   :verbose       true})

(System/exit 0)