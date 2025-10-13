(ns kk.app-test
  (:require [clojure.test :refer [deftest is testing]]
            [kk.app :as app]))

(deftest handle-req-test
  (testing "it works!"
    (is (= (:status (app/handle-req {}))
           200))))
