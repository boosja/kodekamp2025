(ns kk.server
  (:require [compojure.core :refer [defroutes HEAD GET POST]]
            [compojure.route :as route]
            [kk.app :as app]
            [kk.storage :as storage]
            [org.httpkit.server :as server]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(defonce server (atom nil))

(defn persist-body [handler]
  (fn [request]
    (when (#{:post} (:request-method request))
      (future (storage/save (:body request))))
    (handler request)))

(defroutes app-routes
  (HEAD "/" _ {:status 202})
  (GET "/*" _ {:status 202 :body "👋"})
  (POST "/" req (app/handle-req req))
  (route/not-found "404"))

(def wrapped-app
  (-> app-routes
      persist-body
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn start []
  (if @server
    (throw (ex-info "Server is already running!" {}))
    (let [s (server/run-server #'wrapped-app {:legacy-return-value? false
                                              :host "0.0.0.0"
                                              :port 8484})]
      (println (format "Klar for KAMP på port %s!" (server/server-port s)))
      (reset! server s))))

(defn stop []
  (when-let [s @server]
    (server/server-stop! s)
    (reset! server nil)))

(defn reset []
  (stop)
  (start))
