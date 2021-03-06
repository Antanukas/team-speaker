(ns team-speaker.core.predicates
  (:require
    [team-speaker.core.context :as ctx]
    [team-speaker.core.calendar :as wd]))

;Predicates to check review request
(defn is-expired-mr? [mr]
  (let [expiration-period-minutes (:review-expiration-min @ctx/config)
        mr-creation-date (:created_at mr)
        mr-age (wd/working-minute-count
                 mr-creation-date
                 (:work-start @ctx/config)
                 (:work-end @ctx/config))]
    (> mr-age expiration-period-minutes)))

(defn is-new-mr? [prev-mrs]
  (fn [mr] (not (contains? prev-mrs (:id mr)))))

(defn is-build-with-status? [expected-status & args]
  (fn [build] ((set (conj args expected-status)) (:status build))))
