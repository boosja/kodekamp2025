(ns kk.probe-test
  (:require [clojure.test :refer [deftest is testing]]
            [kk.probe :as probe]))

(deftest neighbors-test
  (testing "get neighboring cells"
    (is (= (probe/neighbors {:x 2 :y 3})
           #{{:x 1 :y 3}
             {:x 3 :y 3}
             {:x 2 :y 2}
             {:x 2 :y 4}}))
    (is (= (probe/neighbors {:x 0 :y 0})
           #{{:x 1 :y 0}
             {:x -1 :y 0}
             {:x 0 :y 1}
             {:x 0 :y -1}}))
    (is (= (probe/neighbors {:x -4 :y -8})
           #{{:x -3 :y -8}
             {:x -5 :y -8}
             {:x -4 :y -7}
             {:x -4 :y -9}}))
    (is (= (probe/neighbors {:x -4 :y 8})
           #{{:x -3 :y 8}
             {:x -5 :y 8}
             {:x -4 :y 9}
             {:x -4 :y 7}}))))

(deftest distance-test
  (testing "distance of enemy to the east"
    (is (= (probe/distance {:x 2 :y 3} {:x 4 :y 3})
           2)))

  (testing "distance of enemy to the west"
    (is (= (probe/distance {:x 2 :y 3} {:x 0 :y 3})
           2)))

  (testing "distance of enemy to the north"
    (is (= (probe/distance {:x 2 :y 3} {:x 2 :y 5})
           2)))

  (testing "distance of enemy to the south"
    (is (= (probe/distance {:x 2 :y 3} {:x 2 :y 1})
           2)))

  (testing "distance of enemy to the north-east"
    (is (= (probe/distance {:x 2 :y 3} {:x 3 :y 4})
           2)))

  (testing "distance of enemy to the south-east"
    (is (= (probe/distance {:x 2 :y 3} {:x 3 :y 2})
           2)))

  (testing "distance of enemy to the south-west"
    (is (= (probe/distance {:x 2 :y 3} {:x 1 :y 2})
           2)))

  (testing "distance of enemy to the north-west"
    (is (= (probe/distance {:x 2 :y 3} {:x 1 :y 4})
           2)))

  (testing "distance with negative coords"
   (is (= (probe/distance {:x 2 :y 3} {:x -4 :y -7})
          16)))

  (testing "YOU ARE ON TOP OF EACH OTHER!!!"
    (is (= (probe/distance {:x 2 :y 3} {:x 2 :y 3})
           0))))

(deftest within-test
  (testing "is within range"
    (is (probe/within? 3 {:x 2 :y 3} {:x 2 :y 6}))
    (is (probe/within? 4 {:x 2 :y 3} {:x 2 :y 7})))

  (testing "is beyond range"
    (is (not (probe/within? 3 {:x 2 :y 3} {:x 2 :y 7})))
    (is (not (probe/within? 4 {:x 2 :y 3} {:x 2 :y 8})))))

(deftest idx-test ;; grid starts at 0,0
  (testing "finds idx in 'size' grid vector"
    (is (= (probe/idx 5 {:x 2 :y 3})
           17)))

  (testing "finds 0,0"
    (is (= (probe/idx 5 {:x 0 :y 0})
           0))))

(deftest xy-test
  (testing "finds xy from idx"
    (is (= (probe/xy 5 17)
           {:x 2 :y 3}))))

(deftest only-within-world-test
  (is (= (probe/only-within-world 4 4 #{{:x 1 :y 2}
                                        {:x 4 :y 2}})
         #{{:x 1 :y 2}})))
