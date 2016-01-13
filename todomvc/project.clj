(defproject om-next-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.8.0-RC5"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.datomic/datomic-free "0.9.5344"]
                 [bidi "1.25.0"]
                 [org.omcljs/om "1.0.0-alpha29-SNAPSHOT"]
                 [ring/ring "1.4.0"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 [com.stuartsierra/component "0.3.1"]

  :clean-targets ^{:protect false} ["resources/public/js"]
  :source-paths ["src/clj" "src/cljs" "src/dev"]
  )
