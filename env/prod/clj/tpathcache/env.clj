(ns tpathcache.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[tpathcache started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[tpathcache has shut down successfully]=-"))
   :middleware identity})
