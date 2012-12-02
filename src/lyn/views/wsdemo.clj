(ns lyn.views.wsdemo
  (:require [cheshire.core :as json])
  (:use noir-async.core
        [noir.core :only [defpage]]
        [clojure.string :only [lower-case split]]
        [lamina.core :only [permanent-channel siphon enqueue]]))


;-----------  Creates a channel for broadcasting simulation data ----------

(def sim-data-channel (permanent-channel))

(defn subscribe-channel [ch]
  (siphon sim-data-channel ch))

(defn send-data [data]
  (enqueue sim-data-channel
    (json/generate-string
      {:type "sim-data"
       :data  data})))


;----------------   SIMULATOR PART ---------------------
(def track-steps (split (slurp "data/gps.log") #"\n"))

(def running (atom false))

(defn send-position [currentStep]
  (let [
        geopos (get track-steps currentStep)
        [lat lon] (split geopos #" ")
        sim-data {:latitude (load-string lat) :longitude (load-string lon)}
        ]
    (send-data sim-data)
    (inc currentStep)
  ))

(defn simulateTrack [currentStep]
  (. Thread (sleep  1000))
  (when @running
    (send-off *agent* #'simulateTrack))
  (if (get track-steps currentStep)
    (send-position currentStep)
    0
    ))

(defn start-simulation []
  (reset! running true)
  (send-off (agent 0) simulateTrack))

(defn stop-simulation []
  (reset! running false))


;----------  Makes WebSocket available on given path --------------
(defpage-async "/simulation" {} conn
  (when (not @running)
    (println "Starting simulation")
    (start-simulation))

  (subscribe-channel (:request-channel conn))

  (on-close conn 
            (fn [] 
              (println (str "Conn closed")))))

