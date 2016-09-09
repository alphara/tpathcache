(ns tpathcache.routes.home
  (:require [tpathcache.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.core.cache :as cache]
            ))
(use 'clojure.pprint)

(defn wait-for
 "Invoke predicate every interval (default 10) seconds until it returns true,
  or timeout (default 150) seconds have elapsed. E.g.:
      (wait-for #(< (rand) 0.2) :interval 1 :timeout 10)
  Returns nil if the timeout elapses before the predicate becomes true, otherwise
  the value of the predicate on its last evaluation."
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

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))


; Using Clojure develop a caching backend for an unstable 3rd party service.
; Each request to 3rd party service is very expensive so your backend should minimize number of calls to the bare minimum. At the same time backend should not return data older than 24 hours.
(def my-cache (atom (cache/ttl-cache-factory {} :ttl 86400000)))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))

  ; Your backend should expose one endpoint: GET /geocode?address="some address"
  ; which returns and caches all successful responses from 3rd party service.
  (GET "/geocode" {params :params} []
    (def address (get params :address))
    (print "address: ") (println address)
    (def kw-address (keyword address))


    ; (println "")
    ; (print "BEFORE. mycache: ") (pprint @my-cache)
    ; (println "")

    (if (cache/has? @my-cache kw-address)
      (do
        (println "get from cache"))
      (do
        (println "request from truckerpath and put to cache")

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

; ; TODO: remove; just for testing
; (if (get params :status)
;   (do
;     (def status (get params :status))
;     (print "change status on: ") (println status)))

          ; 3rd party service might occasionally return 5xx errors or
          ; respond slowly.
          (if (= status 200)
            (do
              (println "update cache with data")
              (reset! my-cache (cache/miss @my-cache kw-address geo-data)))
            (do
              (println "remove from cache")
              ; Errors should not be cached because service (most probably)
              ; will respond successfully next time.
              (reset! my-cache (cache/evict @my-cache kw-address))))

          (reset! ready-atom true)
          (println "[Thread] completed client/get")
        )

        (println "[Main] started waiting")

        ; Clients awaits the response from your backend for 1 sec at max,
        ; and may retry a few times.
        (wait-for #(= @ready-atom true) :timeout 900) ; running for 900 ms
                                                      ;            < 1 second
        (if (= @ready-atom true)
          (do
            (println "data is ready"))
          (do
            (println "data is not ready; timeout")))

        (println "[Main] completed waiting")
      ))

    ; (println "")
    ; (print "AFTER. mycache: ") (pprint @my-cache)
    ; (println "")


    (def data (cache/lookup @my-cache kw-address))
    (def headers (get data :headers))
    (def body (get data :body))

    (if (= headers nil)
      (def headers {}))
    (if (= body nil)
      (def body {:error "data is not ready"}))

    (print "headers:") (pprint headers)
    (print "body: ") (pprint body)

    {:body body :headers headers}

    )
  )

