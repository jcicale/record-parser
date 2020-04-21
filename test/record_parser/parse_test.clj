(ns record-parser.parse-test
  (:require [clojure.test :refer :all]
            [record-parser.parse :refer :all])
  (:import (clojure.lang ExceptionInfo)))

(defn clear-records [tests]
  (reset! records []) (tests))

(use-fixtures :each clear-records)

(def csv-record
  "Lovegood, Luna, F, Periwinkle, 1981-02-13")

(def psv-record
  "Cicale | Julia | F | Green | 1990-05-24")

(def ssv-record
  "Dwyer Andy M Blue 1979-06-21")

(def record-missing-columns
  "Cicale | Julia | F ")

(def record-invalid-date
  "Cicale | Julia | F | Green | 5/24/1990")

(deftest parse-record-test
 (is (= {:lastName "Lovegood" :firstName "Luna" :gender "F" :favoriteColor "Periwinkle" :birthDate "2/13/1981"} (parse-record csv-record)))
  (is (= {:lastName "Cicale" :firstName "Julia" :gender "F" :favoriteColor "Green" :birthDate "5/24/1990"} (parse-record psv-record)))
  (is (= {:lastName "Dwyer" :firstName "Andy" :gender "M" :favoriteColor "Blue" :birthDate "6/21/1979"} (parse-record ssv-record))))

(deftest records-with-less-than-five-columns-fail
  (is (thrown-with-msg? ExceptionInfo #"Incorrect number of columns submitted." (parse-record record-missing-columns))))

(deftest records-with-invalid-date-formats-fail
  (is (thrown-with-msg? ExceptionInfo #"Conversion failed" (parse-record record-invalid-date))))

(defn populate-atom []
  (doseq [record [csv-record psv-record ssv-record]]
    (parse-record record)))

(deftest sort-output-by-gender-and-last-name-ascending-test
  (populate-atom)
  (is (= '({:lastName "Cicale", :firstName "Julia", :gender "F", :favoriteColor "Green", :birthDate "5/24/1990"}
          {:lastName "Lovegood", :firstName "Luna", :gender "F", :favoriteColor "Periwinkle", :birthDate "2/13/1981"}
          {:lastName "Dwyer", :firstName "Andy", :gender "M", :favoriteColor "Blue", :birthDate "6/21/1979"})
         (sort-output compare :gender :lastName))))

(deftest sort-output-by-birth-date-ascending-test
  (populate-atom)
  (is (= '({:lastName "Dwyer", :firstName "Andy", :gender "M", :favoriteColor "Blue", :birthDate "6/21/1979"}
           {:lastName "Lovegood", :firstName "Luna", :gender "F", :favoriteColor "Periwinkle", :birthDate "2/13/1981"}
           {:lastName "Cicale", :firstName "Julia", :gender "F", :favoriteColor "Green", :birthDate "5/24/1990"})
         (sort-output compare :birthDate))))

(deftest sort-output-by-last-name-descending-test
  (populate-atom)
  (is (= '({:lastName "Lovegood", :firstName "Luna", :gender "F", :favoriteColor "Periwinkle", :birthDate "2/13/1981"}
           {:lastName "Dwyer", :firstName "Andy", :gender "M", :favoriteColor "Blue", :birthDate "6/21/1979"}
           {:lastName "Cicale", :firstName "Julia", :gender "F", :favoriteColor "Green", :birthDate "5/24/1990"})
         (sort-output #(compare %2 %1) :lastName))))