(ns record-parser.parse
  (:require [clojure.string :as string]
            [java-time :refer [format local-date]])
  (:refer-clojure :exclude [format])
  (:import (java.time.format DateTimeParseException)))

(def records (atom []))

(defn parse-record [record]
  (let [[lname fname gender fcolor dob :as data] (string/split record #"[\s|,|\|]+")]
    (if (> 5 (count data))
      (throw (ex-info "Incorrect number of columns submitted." {:error :bad-input :status 400 :message "Incorrect number of columns submitted."}))
      (try
        (let [record {:lastName lname :firstName fname :gender gender :favoriteColor fcolor :birthDate (local-date dob)}]
          (swap! records #(conj % record))
          (assoc record :birthDate (format "M/d/YYYY" (:birthDate record))))
        (catch DateTimeParseException e
          (throw (ex-info (ex-message e) {:error :bad-input :status 400 :message "Date should be an ISO string."})))))))

(defn sort-output [comparator & ks]
  (->> @records
       (sort-by (apply juxt ks) comparator)
       (map (fn [x] (assoc x :birthDate (format "M/d/YYYY" (:birthDate x)))))))