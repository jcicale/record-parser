(defproject record-parser "0.1.0-SNAPSHOT"
  :description "Record Parser and API"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.10.0"]]

  :source-paths
  ["src/common"]

  :profiles
  {:cli {:dependencies [[org.clojure/tools.cli "1.0.194"]]
         :source-paths ["src/cli"]
         :main         record-parser.cli
         :uberjar-name "record-parser-cli.jar"}

   :api {:dependencies [[compojure "1.6.1"]
                        [ring "1.8.0"]
                        [ring/ring-json "0.5.0"]]
         :source-paths ["src/api"]
         :main         record-parser.api
         :uberjar-name "record-parser-api.jar"}

   :dev [:cli :api {:repl-options {:init-ns record-parser.parse}}]

   :uberjar {:aot :all}

   :test [:cli :api]}

  :plugins
  [[lein-cloverage "1.1.2"]])