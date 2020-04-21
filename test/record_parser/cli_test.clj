(ns record-parser.cli-test
  (:require [clojure.test :refer :all]
            [record-parser.cli :refer :all]
            [record-parser.parse :as parse]
            [clojure.tools.cli :as cli]))

(defn clear-records [tests]
  (reset! parse/records []) (tests))

(use-fixtures :each clear-records)

(defn get-summary [args]
  (cli/parse-opts args cli-options))

(deftest error-msg-test
  (is (= "The following errors occurred while parsing your command:\n\nerror 1\nerror 2" (error-msg ["error 1" "error 2"]))))

(deftest populate-records-test
  (is (empty? @parse/records))
  (populate-records #{"resources/records.csv" "resources/records.psv" "resources/records.ssv"})
  (is (= 12 (count @parse/records))))

(deftest produce-output-test
  (populate-records #{"resources/records.csv" "resources/records.psv" "resources/records.ssv"})
  (let [{:keys [arguments summary]} (get-summary ["-f" "resources/records.csv,resources/records.psv,resources/records.ssv" "parse-last-name"])
        table (with-out-str (produce-output arguments summary))]
    (is (= 13 (count (re-seq #"\n" table))))))

(deftest main-cli-tests
  (are [command-list result] (let [prom (promise)]
                               (with-redefs [exit (fn [_ status] (deliver prom status))]
                                 (apply -main command-list)
                                 (is (= result @prom))))
                             ["-h"] 0 ;cli-exits-with-zero-status-code-on-help-request
                             ["-f" "resources/dne.csv" "parse-gender"] 1 ;cli-exits-with-error-status-code-on-file-that-does-not-exist
                             ["-f" "resources/records.csv"] 1 ;cli-exits-with-error-status-code-with-zero-arguments
                             ["-f" "resources/record-invalid-data.csv" "parse-gender"] 1 ;cli-exits-with-error-status-code-on-bad-data
                             ["-f" "resources/records.csv" "parse"] 1)) ;cli-exits-with-error-status-code-on-incorrect-argument

