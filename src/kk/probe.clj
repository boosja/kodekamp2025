(ns kk.probe)

(defn neighbors [{:keys [x y]}]
  #{{:x (- x 1) :y y}
    {:x (+ x 1) :y y}
    {:x x :y (- y 1)}
    {:x x :y (+ y 1)}})

(defn distance [c1 c2]
  (+ (abs (- (:x c1) (:x c2)))
     (abs (- (:y c1) (:y c2)))))

(defn within? [r c1 c2]
  (< (distance c1 c2) (inc r)))

(defn idx [w c]
  (+ (* (:y c) w) (:x c)))

(defn xy [w idx]
  {:x (mod idx w)
   :y (quot idx w)})

(defn only-within-world [w h cells]
  (set (filter #(and (< (dec 0) (:x %) w)
                     (< (dec 0) (:y %) h))
               cells)))

(defn moves-toward-enemy [unit enemy]
  (let [dx (compare (:x enemy) (:x unit))
        dy (compare (:y enemy) (:y unit))]
   #{{:x (+ dx (:x unit))
      :y (:y unit)}
     {:x (:x unit)
      :y (+ dy (:y unit))}}))

(defn moves-away-enemy [unit enemy]
  (let [dx (compare (:x enemy) (:x unit))
        dy (compare (:y enemy) (:y unit))]
    #{{:x (- dx (:x unit))
       :y (:y unit)}
      {:x (:x unit)
       :y (- dy (:y unit))}}))
