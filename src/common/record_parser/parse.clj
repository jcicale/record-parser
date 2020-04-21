(ns record-parser.parse
  (:require [clojure.string :as string])
  (:import (java.time.format DateTimeParseException DateTimeFormatter)
           (java.time LocalDate)))

(def records (atom []))

(defn parse-record [record]
  (let [[lname fname gender fcolor dob :as data] (string/split record #"[\s|,|\|]+")]
    (if (> 5 (count data))
      (throw (ex-info "Incorrect number of columns submitted." {:error :bad-input :status 400 :message "Incorrect number of columns submitted."}))
      (try
        (let [record {:lastName lname :firstName fname :gender gender :favoriteColor fcolor :birthDate (LocalDate/parse dob)}]
          (swap! records #(conj % record))
          (assoc record :birthDate (.format (:birthDate record) (DateTimeFormatter/ofPattern "M/d/YYYY"))))
        (catch DateTimeParseException e
          (throw (ex-info "Conversion failed" {:error :bad-input :status 400 :message "Date should be an ISO string."})))))))

(defn sort-output [comparator & ks]
  (->> @records
       (sort-by (apply juxt ks) comparator)
       (map (fn [x] (assoc x :birthDate (.format (:birthDate x) (DateTimeFormatter/ofPattern "M/d/YYYY")))))))