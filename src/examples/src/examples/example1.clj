(ns examples.example1
  (:use [ring.adapter.jetty]
        [ring.middleware.etag]
        [compojure.core]))

;;Display heading
(defn response [request]
  {:status 200
   :headers {}
   :body (str "<h1>Hello ETags World! - ETag is: " (get-in request [:headers "if-none-match"] "TEST") "</h1>")})

(def +global-etag+ "Clojure-etags")

(defn create-etag [_]
  +global-etag+)

(def response
  (-> response
      (with-etag :etag-fn create-etag)))
  
;; Create a basic index route 
(defroutes example1-app 
  (GET "/" request (response request)))
  
;; Run the server, {:join? false} runs the sever in its own thread returning immeadiately
(defonce server
  (run-jetty #'example1-app
             {:join? false
              :port 8080}))