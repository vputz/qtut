(ns app.devcards
  (:require
   [app.views :as views]
   [devcards.core :as dc]
   [taoensso.timbre :as log]
   [re-frame.core :as re-frame])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))


(def test-db
  {})

(defcard-rg bloch-sphere-card
  "Bloch Sphere"
  (fn [data-atom _]
    [views/bloch-sphere-component 1 0])
  test-db
  {:iframe true})

(defcard-rg lambda-card
  "Lambda"
  (fn [data-atom _]
    [views/lambda-3d])
  test-db
  {:iframe true})

(defn ^:export main []
  (enable-console-print!)
  (println "Starting devcard ui")
  ;;  (setup-example-1)
                                        ;  (re-frame/dispatch-sync [:initialize-db test-db])
  (dc/start-devcard-ui!))
