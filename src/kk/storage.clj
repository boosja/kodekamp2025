(ns kk.storage
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]
           [java.util Locale]))

(defonce path "history.edn")
(def latest-bounds 10)

(defn current-datetime []
  (let [formatter (DateTimeFormatter/ofPattern
                   "yyyy-MM-dd HH:mm:ss.SSS"
                   (Locale. "no" "NO"))]
    (.format (LocalDateTime/now) formatter)))

(defn bounded-conj [limit seq x]
  (take limit (conj seq x)))

(defonce latest (atom ()))

(comment

  (reset! latest ())

  @latest
  (first @latest)

  )

(defn save [data]
  (let [data (merge {::received (current-datetime)}
                    data)]
    (reset! latest (bounded-conj latest-bounds @latest data))
    (spit path (str data "\n") :append true)))
