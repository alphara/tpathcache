(ns user
  (:require [mount.core :as mount]
            tpathcache.core))

(defn start []
  (mount/start-without #'tpathcache.core/repl-server))

(defn stop []
  (mount/stop-except #'tpathcache.core/repl-server))

(defn restart []
  (stop)
  (start))


