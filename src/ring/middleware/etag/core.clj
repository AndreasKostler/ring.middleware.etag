(ns ring.middleware.etag.core)

(defn- not-modified-response [etag]
  {:status 304 :body "" :headers {"etag" etag}})

(defn- uid []
  (.toString (java.util.UUID/randomUUID)))

(defn- cached-response
  "Attach an etag header to response header. If old-etag and new-etag match then return a 304."
  [old-etag new-etag response]
  (if (= old-etag new-etag)
    (not-modified-response new-etag)
    (assoc-in response [:headers "etag"] new-etag)))

(defn with-etag
  "Generates an etag header for a response body according to etag-generator and transforms response according to response-fn."
  [handler {:keys [etag-generator response-fn]
            :or {etag-generator #(uid)
                 response-fn cached-response}}]
  (fn [request]
    (let [old-etag (get-in request [:headers "if-none-match"])
          response (handler request)
          new-etag (etag-generator response)]
      (response-fn old-etag new-etag response))))

(defn with-etag-cache [handler hash-fn]
  (with-etag handler
    {:etag-generator (fn [respone] (hash-fn (:body response)))
     :response-fn cached-response}))
