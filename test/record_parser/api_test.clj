(ns record-parser.api-test
  (:require [clojure.test :refer :all])
  (:require [record-parser.api :refer :all]
            [record-parser.parse :as parse]
            [clojure.java.io :as io]
            [cheshire.core :refer [parse-string]])
  (:import (java.io ByteArrayInputStream)))

(defn clear-records [tests]
  (reset! parse/records []) (tests))

(use-fixtures :each clear-records)

(defn populate []
  (with-open [rdr (io/reader "resources/records.csv")]
    (doseq [line (line-seq rdr)]
      (parse/parse-record line))))

(defn json-response? [res]
  (= {"Content-Type" "application/json; charset=utf-8"} res))

(deftest get-gender
  (populate)
  (let [{:keys [status headers body]} (app {:request-method :get :uri "/records/gender"})]
    (is (= 200 status))
    (is (json-response? headers))
    (is (= [{:lastName "Granger" :firstName "Hermione" :gender "F" :favoriteColor "Teal" :birthDate "9/19/1979"}
             {:lastName "Lovegood" :firstName "Luna" :gender "F" :favoriteColor "Periwinkle" :birthDate "2/13/1981"}
             {:lastName "Potter" :firstName "Harry" :gender "M" :favoriteColor "Green" :birthDate "7/31/1980"}
             {:lastName "Weasley" :firstName "Ron" :gender "M" :favoriteColor "Blue" :birthDate "3/1/1980"}]
           (parse-string body true)))))

(deftest get-birth-date
  (populate)
  (let [{:keys [status headers body]} (app {:request-method :get :uri "/records/birthdate"})]
    (is (= 200 status))
    (is (json-response? headers))
    (is (= [{:lastName "Granger" :firstName "Hermione" :gender "F" :favoriteColor "Teal" :birthDate "9/19/1979"}
            {:lastName "Weasley" :firstName "Ron" :gender "M" :favoriteColor "Blue" :birthDate "3/1/1980"}
            {:lastName "Potter" :firstName "Harry" :gender "M" :favoriteColor "Green" :birthDate "7/31/1980"}
            {:lastName "Lovegood" :firstName "Luna" :gender "F" :favoriteColor "Periwinkle" :birthDate "2/13/1981"}]
           (parse-string body true)))))

(deftest get-name
  (populate)
  (let [{:keys [status headers body]} (app {:request-method :get :uri "/records/name"})]
    (is (= 200 status))
    (is (json-response? headers))
    (is (= [{:lastName "Weasley" :firstName "Ron" :gender "M" :favoriteColor "Blue" :birthDate "3/1/1980"}
            {:lastName "Potter" :firstName "Harry" :gender "M" :favoriteColor "Green" :birthDate "7/31/1980"}
            {:lastName "Lovegood" :firstName "Luna" :gender "F" :favoriteColor "Periwinkle" :birthDate "2/13/1981"}
            {:lastName "Granger" :firstName "Hermione" :gender "F" :favoriteColor "Teal" :birthDate "9/19/1979"}]
           (parse-string body true)))))

(deftest post
  (is (empty? @parse/records))
  (let [{:keys [status headers body]} (app {:request-method :post :uri "/records" :body (ByteArrayInputStream. (.getBytes "Cicale | Julia | F | Green | 1990-05-24"))})]
    (is (= 200 status))
    (is (json-response? headers))
    (is (= {:lastName "Cicale", :firstName "Julia", :gender "F", :favoriteColor "Green", :birthDate "5/24/1990"}
           (parse-string body true)))
    (is (= 1 (count @parse/records)))))

(deftest post-bad-request
  (is (empty? @parse/records))
  (let [{:keys [status headers body]} (app {:request-method :post :uri "/records" :body (ByteArrayInputStream. (.getBytes "Cicale | Julia | F | Green"))})]
    (is (= 400 status))
    (is (json-response? headers))
    (is (= {:message "Incorrect number of columns submitted."} (parse-string body true)))
    (is (empty? @parse/records))))