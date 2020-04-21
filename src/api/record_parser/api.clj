(ns record-parser.api
  (:require [compojure.core :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.route :as route]
            [record-parser.parse :as parse])
  (:gen-class))

(defroutes main-routes
  (context "/records" []

    (GET "/gender" []
      {:status  200
       :headers {}
       :body    (parse/sort-output compare :gender :lastName)})

    (GET "/birthdate" []
      {:status  200
       :headers {}
       :body    (parse/sort-output compare :birthDate)})

    (GET "/name" []
      {:status  200
       :headers {}
       :body    (parse/sort-output #(compare %2 %1) :lastName)})

    (POST "/" request
      {:status  200
       :headers {}
       :body    (parse/parse-record (slurp (:body request)))}))

  (route/not-found "Resource not found"))

(defn wrap-error-handling [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (if (:message (ex-data e))
             {:status  (:status (ex-data e))
              :headers {}
              :body    {:message (:message (ex-data e))}}
             {:status  500
              :headers {}
              :body    {:message "An unexpected error occurred."}})))))

(def app
  (-> main-routes
      (wrap-reload)
      (wrap-error-handling)
      (wrap-json-response)))

(defn -main [& args]
  (run-jetty (fn [req] (#'app req)) {:port 3000 :join? false}))