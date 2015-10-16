(require '[cljs.build.api :as b])

(b/build "src/cljs"
  {:output-to          "resources/public/js/app.js"
   :output-dir         "resources/public/js"
   :optimizations      :simple
   :static-fns         true
   :optimize-constants true
   :externs            ["src/js/externs.js"]
   :closure-defines    '{goog.DEBUG false}
   :verbose            true})

(System/exit 0)