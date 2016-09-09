(ns tpathcache.routes.home
  (:require [tpathcache.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.core.cache :as cache]))

(use 'clojure.pprint)

(defn wait-for
 "Invoke predicate every interval (default 10) seconds until it returns true,
  or timeout (default 150) seconds have elapsed. E.g.:
      (wait-for #(< (rand) 0.2) :interval 1 :timeout 10)
  Returns nil if the timeout elapses before the predicate becomes true,
  otherwise the value of the predicate on its last evaluation."
 [predicate & {:keys [interval timeout]
               :or {interval 50
                    timeout 1500}}]
 (let [end-time (+ (System/currentTimeMillis) timeout)]
   (loop []
     (if-let [result (predicate)]
       result
       (do
         (print ".")
         (Thread/sleep interval)
         (if (< (System/currentTimeMillis) end-time)
           (recur)))))))

; Using Clojure develop a caching backend for an unstable 3rd party service.
; Each request to 3rd party service is very expensive so your backend should
; minimize number of calls to the bare minimum. At the same time backend
; should not return data older than 24 hours.
(def my-cache (atom (cache/ttl-cache-factory {} :ttl 86400000)))

(defroutes home-routes

  ; React.js application
  (GET "/" []
    (layout/render "index.html"))

  ; Your backend should expose one endpoint:
  ; GET /geocode?address="some address"
  ; which returns and caches all successful responses from 3rd party service.
  (GET "/geocode" {params :params} []
    (def address (get params :address))
    (print "address: ") (println address)
    (def kw-address (keyword address))

    (if (cache/has? @my-cache kw-address)
      (println "get data from cache")
      (do
        (println "request data from truckerpath and put it to cache")

        (def ready-atom (atom false))

        (future
          (println "[Thread] started client/get")
          ; 3rd party service is
          ; GET http://geo.truckerpathteam.com/maps/api/geocode/json?
          ;                                         address="some address"
          (def geo-data (client/get
                       "http://geo.truckerpathteam.com/maps/api/geocode/json"
                       {:query-params {"address" address}}))
          (print "retrieved data from server: ") (pprint geo-data)

          (def status (get geo-data :status))
          (print "got status: ") (pprint status)

          ; 3rd party service might occasionally return 5xx errors or
          ; respond slowly.
          (if (= status 200)
            (do
              (println "update cache with new data")
              (reset! my-cache (cache/miss @my-cache kw-address geo-data)))
            (do
              (println "remove outdated data from cache")
              ; Errors should not be cached because service (most probably)
              ; will respond successfully next time.
              (reset! my-cache (cache/evict @my-cache kw-address))))

          (reset! ready-atom true)
          (println "[Thread] completed client/get")
        )

        (println "[Main] started waiting for request data")

        ; Clients awaits the response from your backend for 1 sec at max,
        ; and may retry a few times.
        (wait-for #(= @ready-atom true) :timeout 900) ; running for 900 ms
                                                      ;            < 1 second
        (if (= @ready-atom true)
          (println "data is ready")
          (println "data is not ready; timeout"))

        (println "[Main] completed waiting for request data")
      ))

    (def data (cache/lookup @my-cache kw-address))
    (def headers (get data :headers))
    (def body (get data :body))

    (if (= headers nil)
      (def headers {}))
    (if (= body nil)
      (def body {:status "Error: data is still not ready"}))

    (print "headers:") (pprint headers)
    (print "body: ") (pprint body)

    {:body body :headers headers}

    )
  )

