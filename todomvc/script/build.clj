(require '[cljs.build.api :as b])

(b/build "src/cljs"
  {:output-to          "resources/public/js/app.js"
   :output-dir         "resources/public/js"
   :optimizations      :simple
   :static-fns         true
   :optimize-constants true
   :pretty-print       true
   :externs            ["src/js/externs.js"]
   :closure-defines    '{goog.DEBUG false}
   :verbose            true})

#_(b/build "src/cljs"
 {:output-to       "resources/public/js/app.js"
  :output-dir      "resources/public/js"
  :optimizations   :advanced
  :pretty-print    true
  :pseudo-names    true
  :externs         ["src/js/externs.js"]
  :closure-defines '{goog.DEBUG false}
  :verbose          true})

(System/exit 0)