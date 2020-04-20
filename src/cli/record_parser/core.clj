(ns record-parser.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [record-parser.parse :as parse])
  (:gen-class))

(def cli-options
  [["-f" "--file <filepath1,filepath2,...>" "Comma separated list of paths to the input files"
    :parse-fn (fn [s] (set (string/split s #",")))
    :validate [(fn [ps] (not-any? false? (map #(.exists (io/file %)) ps))) "No file exists at the supplied path"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  parse-gender      Parse the input record(s). Returns a view sorted by gender then last name ascending."
        "  parse-birthdate   Parse the input record(s). Returns a view sorted by birth date ascending."
        "  parse-last-name   Parse the input record(s). Returns a view sorted by last name descending."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn log [& strings]
  (do (apply print strings) (flush)))

(defn exit
  ([] (exit ""))
  ([msg] (exit msg 0))
  ([msg status]
   (do (log msg \newline) (flush) (System/exit status))))

(defn print-table [rows]
  (let [header    ["Last Name" "First Name" "Gender" "Favorite Color" "Birthday"]
        formatter "%-15s%-15s%-15s%-20s%-15s"]
    (log (apply (partial format formatter) header) \newline)
    (doseq [row rows]
      (log (apply (partial format formatter) (vals row)) \newline))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (exit (usage summary))
      (not= (count arguments) 1)
      (do (println "Arguments received were: " (pr-str arguments))
          (exit (usage summary) 1))
      errors
      (exit (error-msg errors) 1))

    (try
      (let [files (:file options)]
        (doseq [file files]
          (with-open [rdr (io/reader file)]
            (doseq [line (line-seq rdr)]
              (parse/parse-record line))))
        (case (first arguments)
          "parse-gender"
          (let [rows (parse/sort-output compare :gender :lastName)]
            (print-table rows))
          "parse-birthdate"
          (let [rows (parse/sort-output compare :birthDate)]
            (print-table rows))
          "parse-last-name"
          (let [rows (parse/sort-output #(compare %2 %1) :lastName)]
            (print-table rows))
          :otherwise (exit usage summary)))

      (catch Exception e
        (if (:message (ex-data e))
          (exit (:message (ex-data e)) 1)
          (exit (str "An error occurred: " (.getMessage ^Throwable e)) 1))))))