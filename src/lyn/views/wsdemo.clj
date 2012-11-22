(ns lyn.views.wsdemo
  (:require [cheshire.core :as json])
  (:use noir-async.core
        [noir.core :only [defpage]]
        [clojure.string :only [lower-case split]]
        [hiccup.page :only [include-css html5 include-js]]
        [hiccup.element :only [javascript-tag link-to]]
        [lamina.core :only [permanent-channel siphon enqueue]]))


;---------------  Creates a channel for broadcasting simulation data ---------------

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

(def running false)

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
  (when running
    (send-off *agent* #'simulateTrack))
  (if (get track-steps currentStep)
    (send-position currentStep)
    0
    ))

(defn start-simulation []
  (def running true)
  (send-off (agent 0) simulateTrack))

(defn stop-simulation []
  (def running false))


;----------  Makes WebSocket available on given path --------------
(defpage-async "/simulation" {} conn
  (when (not running)
    (println "Starting simulation")
    (start-simulation))

  (subscribe-channel (:request-channel conn))

  (on-close conn (fn [] (println (str "Conn closed")))))


;-----  Demo page ----------------
(defpage "/" {}
  (html5
    [:html {:class "no-js" :lang "en"}
    [:head [:meta {:charset "utf-8"}]
     [:title "WebSockes Demo"]
     (include-css
       "/css/main.css")
     (include-js
      "/js/wsdemo.js")
     ]
    [:body
     [:div {:id "container"}
       [:header
         [:h1
          (link-to "/" "WebSockets Demo")]]
       [:div {:id "main" :role "main"}
        [:div {:id "about"}
         "Simulator feed."
         ]
        [:div {:id "wsdata"}
         [:div {:id "ws-messages"}]
         ]
        ]
      ]
     ]]))


