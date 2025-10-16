(ns kk.app
  (:require [cheshire.core :as chesire]
            [clojure.set :as set]
            [kk.probe :as probe]
            [kk.storage :as storage]))

(defn ->xy [c]
  (select-keys c [:x :y]))

(defn get-cells-with-enemy [enemies cells]
  (set/intersection cells (set (map ->xy enemies))))

(defn get-free-cells [units cells]
  (set/difference cells (set (map ->xy units))))

(defn get-enemy-cells-within-range [enemies c]
  (when (:range c)
    (->> enemies
         (filter #(probe/within? (:range c) c %))
         (map ->xy)
         set)))

(defn ->attack [c cell]
  (merge {:unit (:id c)
          :action :attack}
         cell))

(defn cells->attacks [c cells-with-enemies]
  (when (seq cells-with-enemies)
    (mapv #(->attack c %) cells-with-enemies)))

(defn ->move [c cell]
  (merge {:unit (:id c)
          :action :move}
         cell))

(defn cells->moves [c neighbors]
  (when (seq neighbors)
    (mapv #(->move c %) neighbors)))

(defn grid->cells [grid]
  (for [y (range (count grid))
        x (range (count (first grid)))]
    {:x x
     :y y
     :type (get-in grid [y x])}))

(defn get-terrains-of-type [terrain t]
  (set (filter #(#{t} (:type %)) terrain)))

(defn remove-thorns-from-neighbors [terrain neighbors]
  (let [thorns (get-terrains-of-type terrain "thorns")]
    (set/difference neighbors thorns)))

(defn remove-wholes-from-neighbors [terrain neighbors]
  (let [thorns (get-terrains-of-type terrain "hole")]
    (set/difference neighbors thorns)))

(defn with-distance [enemies c]
  (->> enemies
       (map #(assoc % :d (probe/distance c %)))
       (sort-by :d)))

(defn friendly-actions [state c]
  (let [{:keys [w h]} (:boardSize state)
        terrain (grid->cells (:board state))

        enemies (:enemyUnits state)
        friendly (:friendlyUnits state)
        all-units (into friendly enemies)

        neighbors-in-world (probe/only-within-world w h (probe/neighbors c))
        free-neighbors (remove-wholes-from-neighbors terrain
                                                     (remove-thorns-from-neighbors terrain
                                                                                   (get-free-cells all-units neighbors-in-world)))

        nearest-enemy (first (with-distance enemies c))
        cells-toward-enemy (probe/moves-toward-enemy c nearest-enemy)
        priority-toward-cells (set/intersection free-neighbors cells-toward-enemy)

        cells-away-from-enemy (probe/moves-away-enemy c nearest-enemy)
        priority-away-cells (set/intersection free-neighbors cells-away-from-enemy)

        cells-with-enemies (get-cells-with-enemy enemies neighbors-in-world)
        ranged-cells-with-enemies (get-enemy-cells-within-range enemies c)

        available-attacks (cells->attacks c cells-with-enemies)
        available-ranged-attacks (cells->attacks c ranged-cells-with-enemies)
        available-moves (cond
                          (seq priority-toward-cells)
                          (cells->moves c priority-toward-cells)

                          :else
                          (cells->moves c free-neighbors))
        available-ranged-moves (cells->moves c free-neighbors)]
    (cond
      (:range c)
      (cond-> []
        available-ranged-attacks
        (into (repeat (:attacks c)
                      (nth available-ranged-attacks
                           (rand-int (count available-ranged-attacks)))))

        available-ranged-moves
        (conj (nth available-ranged-moves (rand-int (count available-ranged-moves)))))

      :else
      (cond-> []
        available-attacks
        (into (repeat (:attacks c)
                      (nth available-attacks (rand-int (count available-attacks)))))

        available-moves
        (conj (nth available-moves (rand-int (count available-moves))))))))

(defn calc-actions [state]
  (try
    (let [friendly (:friendlyUnits state)]
      (mapcat #(friendly-actions state %) friendly))
    (catch Exception e
      (swap! storage/errors conj e)
      [])))

;; v2
#_(defn find-actions [state c]
    (let [{:keys [w h]} (:boardSize state)
          terrain (grid->cells (:board state))

          enemies (:enemyUnits state)
          friendly (:friendlyUnits state)
          all-units (into friendly enemies)

          neighbors-in-world (probe/only-within-world w h (probe/neighbors c))
          free-neighbors (remove-wholes-from-neighbors terrain
                                                       (remove-thorns-from-neighbors terrain
                                                                                     (get-free-cells all-units neighbors-in-world)))

          nearest-enemy (first (with-distance enemies c))
          cells-toward-enemy (probe/moves-toward-enemy c nearest-enemy)
          priority-toward-cells (set/intersection free-neighbors cells-toward-enemy)

          cells-away-from-enemy (probe/moves-away-enemy c nearest-enemy)
          priority-away-cells (set/intersection free-neighbors cells-away-from-enemy)

          cells-with-enemies (get-cells-with-enemy enemies neighbors-in-world)
          ranged-cells-with-enemies (get-enemy-cells-within-range enemies c)

          available-attacks (cells->attacks c cells-with-enemies)
          available-ranged-attacks (cells->attacks c ranged-cells-with-enemies)
          available-moves (cond
                            (seq priority-toward-cells)
                            (cells->moves c priority-toward-cells)

                            :else
                            (cells->moves c free-neighbors))
          available-ranged-moves (cells->moves c free-neighbors)]
      [c (concat available-attacks
                 available-ranged-attacks
                 available-moves
                 available-ranged-moves)]))

#_(defn calc-actions-2 [state]
  (try
    (let [actions (map #(find-actions state %) (:friendlyUnits state))]
     (loop [attack-actions (:attackActionsAvailable state)
            move-actions (:moveActionsAvailable state)
            actions []]
       ()))
    (catch Exception e
      (swap! storage/errors conj e)
      [])))

(defn handle-req [req]
  {:status 200
   :body (calc-actions (:body req))})


(comment

  (mapcat #(friendly-actions state %) (:friendlyUnits state))

  (def size (:boardSize state))
  (def w (:w size))
  (def h (:h size))
  (def enemies (:enemyUnits state))
  (def friendly (:friendlyUnits state))
  (def all-units (into friendly enemies))
  (def neighbors-in-world (probe/only-within-world w h (probe/neighbors bunit)))
  (def free-neighbors (get-free-cells all-units neighbors-in-world))
  (def cells-with-enemies (get-cells-with-enemy enemies neighbors-in-world))
  (def ranged-cells-with-enemies (get-enemy-cells-within-range enemies bunit))
  (def available-attacks (cells->attacks bunit cells-with-enemies))
  (def available-moves (cells->moves bunit free-neighbors))

  (map (juxt :id :kind :x :y) friendly)
  (["unit-3" "warrior" 2 2]
   ["unit-4" "archer" 3 2]
   ["unit-5" "warrior" 1 0])


  (def terrain (grid->cells (:board state)))
  (get-terrains-of-type terrain "mud")
  (remove-thorns-from-neighbors terrain free-neighbors)



  (def nearest-enemy (first (with-distance enemies bunit)))
  (def cells-toward-enemy (probe/moves-toward-enemy bunit nearest-enemy))
  (def priority-toward-cells (set/intersection free-neighbors cells-toward-enemy))
  (def cells-away-from-enemy (probe/moves-away-enemy bunit nearest-enemy))
  (def priority-away-cells (set/intersection free-neighbors cells-away-from-enemy))


  (def available-moves (cond
                         (seq priority-toward-cells)
                         (cells->moves bunit priority-toward-cells)

                         :else
                         (cells->moves bunit free-neighbors)))

  (conj [] (nth available-moves (rand-int (count available-moves))))

  (do
    (def terrain (grid->cells (:board state)))
    (def enemies (:enemyUnits state))
    (def friendly (:friendlyUnits state))
    (def all-units (into friendly enemies))
    (def neighbors-in-world (probe/only-within-world w h (probe/neighbors c)))
    (def free-neighbors (remove-wholes-from-neighbors terrain (remove-thorns-from-neighbors terrain (get-free-cells all-units neighbors-in-world))))
    (def nearest-enemy (first (with-distance enemies c)))
    (def cells-toward-enemy (probe/moves-toward-enemy c nearest-enemy))
    (def priority-toward-cells (set/intersection free-neighbors cells-toward-enemy))
    (def cells-away-from-enemy (probe/moves-away-enemy c nearest-enemy))
    (def priority-away-cells (set/intersection free-neighbors cells-away-from-enemy))
    (def cells-with-enemies (get-cells-with-enemy enemies neighbors-in-world))
    (def ranged-cells-with-enemies (get-enemy-cells-within-range enemies c))
    (def available-attacks (cells->attacks c cells-with-enemies))
    (def available-ranged-attacks (cells->attacks c ranged-cells-with-enemies))
    (def available-moves (cond
                           (seq priority-toward-cells)
                           (cells->moves c priority-toward-cells)

                           :else
                           (cells->moves c free-neighbors)))
    (def available-ranged-moves (cells->moves c free-neighbors)))

  (defn do-it []
    (cond
      (:range c)
      (cond-> []
        available-ranged-attacks
        (into (repeat (:attacks c)
                      (nth available-ranged-attacks
                           (rand-int (count available-ranged-attacks)))))

        available-ranged-moves
        (conj (nth available-ranged-moves (rand-int (count available-ranged-moves)))))

      :else
      (cond-> []
        available-attacks
        (into (repeat (:attacks c)
                      (nth available-attacks (rand-int (count available-attacks)))))

        available-moves
        (conj (nth available-moves (rand-int (count available-moves)))))))

  (do-it)

  (def c
    {:y 2,
     :moves 2,
     :attackStrength 3,
     :id "unit-3",
     :kind "warrior",
     :health 4,
     :side "player-2",
     :armor 2,
     :x 2,
     :attacks 1,
     :maxHealth 7})

  (def state
    (chesire/parse-string
     "{
  \"turnNumber\": 4,
  \"yourId\": \"76796e7d977b5225ec9335e9e47b0987b380621d\",
  \"enemyUnits\": [
    {
      \"y\": 2,
      \"moves\": 2,
      \"maxHealth\": 7,
      \"attackStrength\": 3,
      \"id\": \"unit-0\",
      \"kind\": \"warrior\",
      \"health\": 7,
      \"side\": \"player-1\",
      \"armor\": 2,
      \"x\": 1,
      \"attacks\": 1
    },
    {
      \"y\": 3,
      \"moves\": 3,
      \"maxHealth\": 7,
      \"attackStrength\": 1,
      \"isPiercing\": true,
      \"id\": \"unit-1\",
      \"kind\": \"archer\",
      \"health\": 7,
      \"side\": \"player-1\",
      \"armor\": 0,
      \"x\": 0,
      \"attacks\": 2,
      \"range\": 4
    },
    {
      \"y\": 4,
      \"moves\": 2,
      \"maxHealth\": 7,
      \"attackStrength\": 3,
      \"id\": \"unit-2\",
      \"kind\": \"warrior\",
      \"health\": 6,
      \"side\": \"player-1\",
      \"armor\": 1,
      \"x\": 3,
      \"attacks\": 1
    }
  ],
  \"boardSize\": {
    \"w\": 4,
    \"h\": 5
  },
  \"player2\": {
    \"name\": \"Mathias I\",
    \"experience\": \"kjent\"
  },
  \"player1\": {
    \"name\": \"Hel1 + Sim1\",
    \"experience\": \"nykommer\"
  },
  \"moveActionsAvailable\": 4,
  \"attackActionsAvailable\": 2,
  \"friendlyUnits\": [
    {
      \"y\": 2,
      \"moves\": 2,
      \"maxHealth\": 7,
      \"attackStrength\": 3,
      \"id\": \"unit-3\",
      \"kind\": \"warrior\",
      \"health\": 4,
      \"side\": \"player-2\",
      \"armor\": 2,
      \"x\": 2,
      \"attacks\": 1
    },
    {
      \"y\": 2,
      \"moves\": 3,
      \"maxHealth\": 7,
      \"attackStrength\": 1,
      \"isPiercing\": true,
      \"id\": \"unit-4\",
      \"kind\": \"archer\",
      \"health\": 7,
      \"side\": \"player-2\",
      \"armor\": 0,
      \"x\": 3,
      \"attacks\": 2,
      \"range\": 4
    },
    {
      \"y\": 0,
      \"moves\": 2,
      \"maxHealth\": 7,
      \"attackStrength\": 3,
      \"id\": \"unit-5\",
      \"kind\": \"warrior\",
      \"health\": 7,
      \"side\": \"player-2\",
      \"armor\": 1,
      \"x\": 1,
      \"attacks\": 1
    }
  ],
  \"uuid\": \"68f0d0a5-0877-4803-adf7-0b4f0e938a81\",
  \"board\": [
    [
      \"grass\",
      \"grass\",
      \"grass\",
      \"mud\"
    ],
    [
      \"mud\",
      \"thorns\",
      \"grass\",
      \"grass\"
    ],
    [
      \"grass\",
      \"castle\",
      \"castle\",
      \"grass\"
    ],
    [
      \"grass\",
      \"grass\",
      \"thorns\",
      \"mud\"
    ],
    [
      \"mud\",
      \"grass\",
      \"grass\",
      \"grass\"
    ]
  ]
}"

     keyword))

  )




(comment










  )
