(set-env!
 :resource-paths #{"src" "html"}
 :dependencies '[[adzerk/boot-cljs            "1.7.48-5"      :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.3"          :scope "test"]
                 [adzerk/boot-reload          "0.5.1"          :scope "test"]
                 [pandeiro/boot-http          "0.7.6" :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [org.clojure/clojure         "1.8.0"]
                 [org.clojure/clojurescript   "1.9.518"]
                 [com.cemerick/piggieback     "0.2.1"          :scope "test"]
                 [weasel                      "0.7.0"          :scope "test"]
                 [cljsjs/d3 "4.3.0-4"]
                 [cljsjs/three "0.0.84-0"]
                 [com.taoensso/timbre "4.8.0"]
                 [devcards "0.2.2"]
                 [reagent "0.6.0"]
                 [re-frame "0.8.0"]
                 [hiccup "1.0.5"]
                 [tolitius/boot-check "0.1.4"]
                 [org.clojure/tools.nrepl     "0.2.13"         :scope "test"]])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[crisptrutski.boot-cljs-test  :refer [exit! test-cljs]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask testing []
  (merge-env! :resource-paths #{"test"})
  identity)

(deftask auto-test []
  (comp (testing)
        (watch)
        (speak)
        (test-cljs)))

(deftask dev []
  (comp (serve :dir "target/")
        (watch)
        (speak)
        (reload :on-jsload 'app.core/main)
        (cljs-repl)
        (cljs :source-map true :optimizations :none)))

(deftask test []
  (comp (testing)
        (test-cljs)
        (exit!)))

(deftask build []
  (cljs :optimizations :advanced))


(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.14.0"]
                [refactor-nrepl "2.3.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       concat '[cider.nrepl/cider-middleware
                refactor-nrepl.middleware/wrap-refactor])

(deftask cider "CIDER profile"
  []
  (require 'boot.repl)
  (swap! @(resolve 'boot.repl/*default-dependencies*)
         concat '[[org.clojure/tools.nrepl "0.2.12"]
                  [cider/cider-nrepl "0.13.0"]
                  [refactor-nrepl "2.0.0-SNAPSHOT"]])
  (swap! @(resolve 'boot.repl/*default-middleware*)
         concat '[cider.nrepl/cider-middleware
                  refactor-nrepl.middleware/wrap-refactor])
  identity
  )
