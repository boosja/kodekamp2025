(ns kk.storage-test
  (:require [clojure.test :refer [deftest is testing]]
            [kk.storage :as storage]))

(deftest bounded-conj-test
  (testing "conjoins item to the beginning"
    (is (= (storage/bounded-conj 10 '(1) 2)
           '(2 1))))

  (testing "removes old items when max size is hit"
    (is (= (storage/bounded-conj 10 '(9 8 7 6 5 4 3 2 1 0) 10)
           '(10 9 8 7 6 5 4 3 2 1)))
    (is (= (storage/bounded-conj 10 '(10 9 8 7 6 5 4 3 2 1) 11)
           '(11 10 9 8 7 6 5 4 3 2)))))
