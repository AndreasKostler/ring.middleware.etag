(ns examples.example1
  (:use [ring.adapter.jetty]
        [ring.middleware.etag.core :as etag]
        [compojure.core]))

;;Display heading
(defn response [request]
  {:status 200
   :headers {}
   :body (str "<h1>Hello ETags World!</h1>")})

(def +global-etag+ "Clojure-etag")

(defn create-etag [_]
  +global-etag+)

(def response
  (-> response
      (etag/with-etag {:etag-generator create-etag})))
  
;; Create a basic index route 
(defroutes example1-app 
  (GET "/" request (response request)))
  
;; Run the server, {:join? false} runs the sever in its own thread returning immeadiately
(defonce server
  (run-jetty #'example1-app
             {:join? false
              :port 8080}))