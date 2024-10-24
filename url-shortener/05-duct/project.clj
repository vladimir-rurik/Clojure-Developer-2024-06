(defproject url-shortener "0.1.0-SNAPSHOT"
  :description "URL shortener app"

  :dependencies [[org.clojure/clojure "1.11.1"]

                 [dev.weavejester/medley "1.7.0"]
                 [integrant/integrant "0.8.1"]

                 [duct/core "0.8.1"]
                 [duct/module.sql "0.6.1" :exclusions [medley integrant]]
                 [duct/module.logging "0.5.0" :exclusions [integrant com.taoensso/timbre]]

                 [ring/ring-core "1.10.0"]
                 [ring/ring-devel "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-defaults "0.3.4"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.0" :exclusions [medley]]

                 ;; DB
                 [com.h2database/h2 "2.2.224"]
                 [org.postgresql/postgresql "42.2.10"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.github.seancorfield/honeysql "2.4.1045"]

                 ;; Logging
                 [com.taoensso/timbre "6.3.1"]
                 [org.slf4j/slf4j-simple "2.0.10"]]

  :plugins [[duct/lein-duct "0.12.3"]]

  :main ^:skip-aot url-shortener.main

  :uberjar-name "url-shortener.jar"

  :resource-paths ["resources" "target/resources"]
  :middleware     [lein-duct.plugin/middleware]

  :profiles {:dev  {:source-paths   ["dev/src"]
                    :resource-paths ["dev/resources"]
                    :dependencies   [[integrant/repl "0.3.2"]
                                     [hawk "0.2.11"]
                                     [fipp "0.6.26"]]}

             :cljs {:dependencies
                    [[reagent "1.2.0"]
                     [org.clojure/core.async "1.6.681"]
                     [cljs-http "0.1.48" :exclusions [org.clojure/core.async
                                                      com.cognitect/transit-cljs
                                                      com.cognitect/transit-js]]
                     [thheller/shadow-cljs "2.27.2"]]}  ; Keep it synced with npm version!

             :uberjar {:aot :all}

             :repl {:prep-tasks   ^:replace ["javac" "compile"]
                    :repl-options {:init-ns user}}})
