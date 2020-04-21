(ns record-parser.cli
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

(defn exit [msg status]
   (do (println msg) (System/exit status)))

(defn print-table [rows]
  (let [header    ["Last Name" "First Name" "Gender" "Favorite Color" "Birthday"]
        formatter "%-15s%-15s%-15s%-20s%-15s"]
    (println (apply (partial format formatter) header))
    (doseq [row rows]
      (println (apply (partial format formatter) (vals row))))))

(defn populate-records [files]
  (doseq [file files]
    (with-open [rdr (io/reader file)]
      (doseq [line (line-seq rdr)]
        (parse/parse-record line)))))

(def dispatch-map {"parse-gender"    {:sort-keys [:gender :lastName] :comparator compare}
                   "parse-birthdate" {:sort-keys [:birthDate] :comparator compare}
                   "parse-last-name" {:sort-keys [:lastName] :comparator #(compare %2 %1)}})

(defn produce-output [arguments summary]
  (if-some [{:keys [sort-keys comparator]} (dispatch-map (first arguments))]
    (print-table (apply parse/sort-output comparator sort-keys))
    (exit (usage summary) 1)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (exit (usage summary) 0)
      (not= (count arguments) 1)
      (do (println "Arguments received were: " (pr-str arguments))
          (exit (usage summary) 1))
      errors
      (exit (error-msg errors) 1))

    (try
      (let [files (:file options)]
        (populate-records files)
        (produce-output arguments summary))

      (catch Exception e
        (let [message (or (:message (ex-data e))
                          (str "An error occurred: " (.getMessage ^Throwable e)))]
          (exit message 1))))))