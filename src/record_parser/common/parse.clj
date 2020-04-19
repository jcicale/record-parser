(ns record-parser.common.parse
  (:require [clojure.string :as string])
  (:refer-clojure :exclude [format contains? iterate range min max zero?])
  (:use java-time))

(def records (atom []))

;; input parsing
(defn input-type [string]
  (cond
    (string/includes? string "|") :psv
    (string/includes? string ",") :csv
    :otherwise :ssv))

(defn parse [record delimiter]
  (let [[lname fname gender fcolor dob] (string/split record delimiter)]
    (swap! records #(conj % {:lname lname :fname fname :gender gender :fcolor fcolor :dob (local-date dob)}))))

(defmulti parse-record input-type)

(defmethod parse-record :psv [record]
  (parse record #" \| "))

(defmethod parse-record :csv [record]
  (parse record #", "))

(defmethod parse-record :ssv [record]
  (parse record #" "))

;;output sorting/display
(defmulti produce-output identity)

(defmethod produce-output :gender-lname-asc [_]
  (->> @records
       (sort-by (juxt :gender :lname))
       (map (fn [x] (assoc x :dob (format "M/d/YYYY" (:dob x)))))))

(defmethod produce-output :dob-asc [_]
  (->> @records
       (sort-by :dob)
       (map (fn [x] (assoc x :dob (format "M/d/YYYY" (:dob x)))))))

(defmethod produce-output :lname-des [_]
  (->> @records
       (sort-by :lname #(compare %2 %1))
       (map (fn [x] (assoc x :dob (format "M/d/YYYY" (:dob x)))))))