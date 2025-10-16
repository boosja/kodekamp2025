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
(def errors (atom ()))

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

(comment
  (def example
    {:attackActionsAvailable 1, :player2 {:name "Slimtryne", :experience "nykommer"},
     :moveActionsAvailable 2,
     :boardSize {:w 4, :h 4},
     :friendlyUnits [{:id "unit-0",
                      :x 1,
                      :y 2,
                      :moves 2,
                      :attacks 1,
                      :attackStrength 3,
                      :maxHealth 7
                      :health 7,
                      :armor 1,
                      :kind "warrior",
                      :side "player-1",}
                     {:y 2, :moves 2, :attackStrength 3, :id "unit-1", :kind "warrior", :health 7, :side "player-1", :armor 1, :x 0, :attacks 1, :maxHealth 7}
                     {:y 3, :moves 2, :attackStrength 3, :id "unit-2", :kind "warrior", :health 7, :side "player-1", :armor 1, :x 1, :attacks 1, :maxHealth 7}],
     :enemyUnits [{:y 1, :moves 2, :attackStrength 3, :id "unit-3", :kind "warrior", :health 7, :side "player-2", :armor 1, :x 2, :attacks 1, :maxHealth 7}
                  {:y 1, :moves 2, :attackStrength 3, :id "unit-4", :kind "warrior", :health 7, :side "player-2", :armor 1, :x 3, :attacks 1, :maxHealth 7}
                  {:y 0, :moves 2, :attackStrength 3, :id "unit-5", :kind "warrior", :health 7, :side "player-2", :armor 1, :x 2, :attacks 1, :maxHealth 7}],
     :uuid "68f09f42-e473-43b8-a6aa-718a96a8bf1d",
     :yourId "76796e7d977b5225ec9335e9e47b0987b380621d",
     :player1 {:name "Mathias I", :experience "kjent"},
     :turnNumber 1})

  (mapcat #(kk.app/friendly-actions example %) (:friendlyUnits example))




  )
