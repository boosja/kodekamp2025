(ns kk.storage)

(defonce path "history.edn")

(defn save [data]
  (spit path (str data "\n") :append true))
