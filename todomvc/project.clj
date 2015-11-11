(defproject om-next-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [com.datomic/datomic-free "0.9.5206"]
                 [bidi "1.20.3"]
                 [org.omcljs/om "1.0.0-alpha20-SNAPSHOT"]
                 [ring/ring "1.4.0"]
                 [com.cognitect/transit-clj "0.8.281"]
                 [com.cognitect/transit-cljs "0.8.225"]
                 [cljs-http "0.1.30" :exclusions
                  [org.clojure/clojure org.clojure/clojurescript
                   com.cognitect/transit-cljs]]
                 [com.stuartsierra/component "0.2.3"]]

  :source-paths ["src/clj" "src/cljs" "src/dev"]
  )
