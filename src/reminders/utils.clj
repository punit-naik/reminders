(ns reminders.utils
  (:require [buddy.sign.jwt :refer [sign unsign]]
            [clojure.string :refer [split]]
            [diehard.core :refer [defratelimiter with-rate-limiter defbulkhead with-bulkhead]]
            [clojure.spec.alpha :as s]))

(defonce ^:private secret (or (System/getenv "APP_SECRET") (System/getProperty "APP_SECRET")))

(defn unix->iso8601
  "Converts a unix timestamp to ISO8601 format"
  [unix-timestamp]
  (.format
    (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:SS")
    (java.util.Date. (* unix-timestamp 1000))))

(defn with-http-error-response
  "Throws an error and catches it, returning the http response passed as an argument"
  [response]
  (try
    (throw (Exception. ""))
    (catch Exception e response)))

(defmacro with-exception-api
  "Wraps the code in an exception when there is an error in executing the API code"
  [api-fn success-status-code]
  `(try
     {:status ~success-status-code
      :body ~api-fn}
     (catch Exception ~'e
       {:status 500
        :body "API Error"})))

(defn authenticated?
  "Checks if a request is authenticated by checking the JWT in the authorization header"
  [request]
  (try (-> request
           (get-in [:headers "authorization"])
           (split #"\s")
           second
           (unsign secret))
    (catch Exception e nil)))

(defmacro with-auth
  "Wraps the code in an exception when there is an authentication error while executing the API code
   Passes it to the `with-data-validation` macro"
  [request api-fn]
  `(if (authenticated? ~request)
     (with-data-validation ~request ~api-fn)
     (with-http-error-response
       {:status 401
        :body "Not authorized"})))

;; Request map spec

(s/def ::id (fn [x] (re-matches #"\d+" x)))
(s/def ::route-params (s/keys :opt-un [::id]))
(s/def ::date_format #(or (= % "unix") (= % "iso8601")))
(s/def ::description string?)
(s/def ::scheduled_time (fn [x] (re-matches #"\d+" x)))
(s/def ::params (s/keys :opt-un [::date_format ::description ::scheduled_time]))
(s/def ::request (s/keys :opt-un [::params ::route-params]))

(defmacro with-data-validation
  "Wraps the code in an exception when there is a data validation error while executing the API code"
  [request api-fn]
  `(if (s/valid? ::request ~request)
     (~api-fn ~request)
     (with-http-error-response
       {:status 400
        :body (s/explain-str ::request ~request)})))

;; API Throttling

; 100 Requests per second
(defratelimiter my-rl {:rate 100})
; 10 concurrent threads
(defbulkhead my-bh {:concurrency 10})

(defmacro with-throttler
  "Throttles functions based on per sond requests and concurrent requests"
  [api-fn]
  `(with-rate-limiter my-rl
     (with-bulkhead my-bh ~api-fn)))
