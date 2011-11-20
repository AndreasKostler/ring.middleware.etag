(ns ring.middleware.etag.core
  (:import [org.apache.commons.codec.digest DigestUtils]))

;; A bunch of useful hashing/uuid etag generators.
(defn- md5 [s]
  (DigestUtils/md5Hex s))
(defn- sha [s]
  (DigestUtils/shaHex s))
(defn- sha256 [s]
  (DigestUtils/sha256Hex s))
(defn- sha384 [s]
  (DigestUtils/sha384Hex s))
(defn- sha512 [s]
  (DigestUtils/sha512Hex s))
(defn- uid []
  (.toString (java.util.UUID/randomUUID)))

(defmulti calculate-etag class)

(defmethod calculate-etag String [s fun] (fun s))
(defmethod calculate-etag java.io.File
  [f fun]
  (calculate-etag (slurp f) fun))

(defn create-hashed-etag-fn [hash-fn]
  (fn [response]
    (calculate-etag (:body response) hash-fn)))

(defn create-uid [_]
  (uid))

(defn- not-modified-response [etag]
  {:status 304 :body "" :headers {"etag" etag}})

(defn with-etag
  "Generates an etag header for a response body according to etag-generator and transforms response according to response-fn."
  [handler {:keys [etag-generator response-fn]
            :or {etag-generator create-uid
                 response-fn cached-response}}]
  (fn [request]
    (let [old-etag (get-in request [:headers "if-none-match"])
          response (handler request)
          new-etag (etag-generator response)]
      (response-fn old-etag new-etag response))))

(defn- cached-response
  "Attach an etag header to response header. If old-etag and new-etag match then return a 304."
  [old-etag new-etag response]
  (if (= old-etag new-etag)
    (not-modified-response new-etag)
    (assoc-in response [:headers "etag"] new-etag)))

(defn with-etag-cache [handler]
  (with-etag handler {:etag-generator (create-hashed-etag-fn md5) :response-fn cached-response}))
