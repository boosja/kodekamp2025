(ns kk.app-test
  (:require [clojure.test :refer [deftest is testing]]
            [kk.app :as app]))

(deftest handle-req-test
  (testing "it works!"
    (is (= (:status (app/handle-req {}))
           200))))

(deftest get-cells-with-enemy-test
  (testing "finds cells with enemies"
    (is (= (app/get-cells-with-enemy [{:x 4 :y 3 :id "e1"}
                                      {:x 2 :y 2 :id "e2"}
                                      {:x 4 :y 2 :id "e3"}]
                                     #{{:x 1 :y 3}
                                       {:x 3 :y 3}
                                       {:x 2 :y 2}
                                       {:x 2 :y 4}})
           #{{:x 2 :y 2}})))

  (testing "and when multiple"
    (is (= (app/get-cells-with-enemy [{:x 1 :y 3 :id "e1"}
                                      {:x 2 :y 2 :id "e2"}
                                      {:x 4 :y 2 :id "e3"}]
                                     #{{:x 1 :y 3}
                                       {:x 3 :y 3}
                                       {:x 2 :y 2}
                                       {:x 2 :y 4}})
           #{{:x 1 :y 3}
             {:x 2 :y 2}}))))

#_(deftest attack-neigbors-test
  (testing ""))



{:x 1 :y 2 :id "u1"}
[{:x 2 :y 3 :id "e1"}
 {:x 2 :y 2 :id "e2"}
 {:x 4 :y 2 :id "e3"}]

[{:unit "u1"
  :action :attack
  :x 2 :y 2}]
