(ns kk.storage)

(defonce path "history.edn")
(def latest-bounds 10)

(defn bounded-conj [limit seq x]
  (take limit (conj seq x)))

(def latest (atom ()))

(comment

  @latest
  (first @latest)

  )

(defn save [data]
  (reset! latest (bounded-conj latest-bounds @latest data))
  (spit path (str data "\n") :append true))
