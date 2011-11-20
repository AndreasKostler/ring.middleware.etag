(ns ring.middleware.etag
  (:use [ring.adapter.jetty]
        [compojure.core]))

;; A bunch of useful hashing/uuid etag generators.
(defn- md5 [s]
  (org.apache.commons.codec.digest.DigestUtils/md5Hex s))
(defn- sha [s]
  (org.apache.commons.codec.digest.DigestUtils/shaHex s))
(defn- sha256 [s]
  (org.apache.commons.codec.digest.DigestUtils/sha256Hex s))
(defn- sha384 [s]
  (org.apache.commons.codec.digest.DigestUtils/sha384Hex s))
(defn- sha512 [s]
  (org.apache.commons.codec.digest.DigestUtils/sha512Hex s))
(defn- uid [_]
  (.toString (java.util.UUID/randomUUID)))

(defmulti calculate-etag class)

(defmethod calculate-etag String [s fun] (fun s))
(defmethod calculate-etag File
  [f fun]
  (calculate-etag (slurp f) fun))

(defn create-md5-etag [response]
  (calculate-etag (:body response) md5))

(defn create-sha1-etag [response]
  (calculate-etag (:body response) sha1))

(defn create-uid-etag [_]
  (uid))

(defn- not-modified-response [etag]
  {:status 304 :body "" :headers {"etag" etag}})

(defn with-etag
  "Generates an etag header for a response body according to etag-generator and transforms response according to response-fn."
  [handler {:keys [etag-constructor response-fn]
            :or [etag-generator create-uid
                 response-fn cached-response]}]
  (fn [request]
    (let [old-etag (get-in request [:headers "if-none-match"])
          response (handler request)
          new-etag (etag-fn response)]
      (response-fn old-etag new-etag response))))

(defn- cached-response
  "Attach an etag header to response header. If old-etag and new-etag match then return a 304."
  [old-etag new-etag response]
  (if (= old-etag new-etag)
    (not-modified-response new-etag)
    (assoc-in response [:headers "etag"] new-etag)))

(defn with-etag-cache [handler]
  (with-etag handler :etag-generator create-md5 :response-fn cached-response))