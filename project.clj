(defproject record-parser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]]

  :source-paths
  ["src/common"]

  :profiles
  {:cli {:dependencies [[org.clojure/tools.cli "1.0.194"]]
         :source-paths ["src/cli"]
         :main         record-parser.core
         :uberjar-name "record-parser-cli.jar"}

   :api {:dependencies [[compojure "1.6.1"]
                        [ring "1.8.0"]
                        [ring/ring-json "0.5.0"]
                        [ring/ring-jetty-adapter "1.8.0"]]
         :source-paths ["src/api"]
         :main         record-parser.core
         :uberjar-name "record-parser-api.jar"}

   :dev [:cli :api {:repl-options {:init-ns record-parser.parse}}]

   :uberjar {:aot :all}})