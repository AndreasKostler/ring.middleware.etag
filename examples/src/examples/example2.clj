(ns js-caching-example.core
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.response :as ring.response])
  (:use [ring.adapter.jetty]
        [compojure.core]
        [ring.middleware.etag.core :as etag]))

;; +++ The third-party web-app +++

(html/deftemplate index "js_caching_example/template.html" [])

(defroutes third-party-app 
  (GET "/" [] (index)))

(defonce third-party-server
  (run-jetty
   #'third-party-app
   {:join? false
    :port 8080}))

;; +++ END +++

;; +++ JS Server +++

(defroutes static-js-app 
  (GET "/static" []
       (ring.response/file-response "src/js_caching_example/static.js")))

(def create-md5-etag (etag/create-hashed-etag-fn etag/md5))

(def static-js-app
  (-> static-js-app
      (etag/with-etag {:etag-generator create-md5-etag})))

(defonce js-server
  (run-jetty
   static-js-app
   {:join? false
    :port 8888}))

(run-jetty
 static-js-app
 {:join? false
  :port 8888})

(.start third-party-server)
(.stop third-party-server)

(.start js-server)
(.stop js-server)

(def create-md5-etag (etag/create-hashed-etag-fn etag/md5))


