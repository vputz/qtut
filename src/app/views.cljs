(ns app.views
  (:require [reagent.core :as reagent]
            [taoensso.timbre :as log]
            [re-frame.core :refer [subscribe dispatch]]
            [cljsjs.d3 :as d3]
            [cljsjs.three :as three]))

;;http://timothypratley.blogspot.com/2017/01/reagent-deep-dive-part-2-lifecycle-of.html

(defn create-renderer [element]
  (doto (js/THREE.WebGLRenderer. #js {:canvas element :antialias true})
    (.setPixelRatio js/window.devicePixelRatio)))

(defn three-canvas [attributes camera scene tick]
  (let [requested-animation (atom nil)]
    (reagent/create-class
     {:display-name "three-canvas"
      :reagent-render
      (fn three-canvas-render []
        [:canvas attributes])
      :component-did-mount
      (fn three-canvas-did-mount [this]
        (let [e (reagent/dom-node this)
              r (create-renderer e)]
          ((fn animate []
             (tick)
             (.render r scene camera)
             (reset! requested-animation (js/window.requestAnimationFrame animate))))))
      :component-will-unmount
      (fn [this]
        (js/window.cancelAnimationFrame @requested-animation))})))

(defn fly-around-z-axis [camera scene]
  (let [t (* (js/Date.now) 0.0002)]
    (doto camera
      (-> (.-position) (.set (* 100 (js/Math.cos t)) (* 100 (js/Math.sin t)) 100))
      (.lookAt (.-position scene)))))

(defn bloch-sphere-component
  "A bloch sphere represented in Three.js"
  [alpha beta]
  (let [
        geometry (js/THREE.BoxBufferGeometry. 50 50 50)
        material (js/THREE.MeshBasicMaterial. #js {:color "green" :wireframe true})
        mesh (js/THREE.Mesh. geometry material)
        scene (doto (js/THREE.Scene.)
                (.add (js/THREE.AmbientLight. 0x888888))
                (.add (doto (js/THREE.DirectionalLight. 0xffff88 0.5)
                        (-> (.-position) (.set -600 300 600))))
                (.add (js/THREE.AxisHelper. 50))
                (.add mesh))
        camera (js/THREE.PerspectiveCamera. 70 1 1 1000)
        tick (fn []
               (fly-around-z-axis camera scene))]
    [three-canvas {:width 500 :height 500} camera scene tick]))


(defn create-scene []
  (doto (js/THREE.Scene.)
    (.add (js/THREE.AmbientLight. 0x888888))
    (.add (doto (js/THREE.DirectionalLight. 0xffff88 0.5)
            (-> (.-position) (.set -600 300 600))))
    (.add (js/THREE.AxisHelper. 50))))

(defn mesh [geometry color]
  (js/THREE.SceneUtils.createMultiMaterialObject.
   geometry
   #js [(js/THREE.MeshBasicMaterial. #js {:color color :wireframe true})
        (js/THREE.MeshLambertMaterial. #js {:color color})]))



(defn v3 [x y z]
  (js/THREE.Vector3. x y z))

(defn lambda-3d []
  (let [camera (js/THREE.PerspectiveCamera. 45 1 1 2000)
        curve (js/THREE.CubicBezierCurve3.
               (v3 -30 -30 10)
               (v3 0 -30 10)
               (v3 0 30 10)
               (v3 30 30 10))
        path-geometry (js/THREE.TubeGeometry. curve 20 4 8 false)
        scene (doto (create-scene)
                (.add
                 (doto (mesh (js/THREE.CylinderGeometry. 40 40 5 24) "green")
                   (-> (.-rotation) (.set (/ js/Math.PI 2) 0 0))))
                (.add
                 (doto (mesh (js/THREE.CylinderGeometry. 20 20 10 24) "blue")
                   (-> (.-rotation) (.set (/ js/Math.PI 2) 0 0))))
                (.add (mesh path-geometry "white")))
        tick (fn []
               (fly-around-z-axis camera scene))]
    [three-canvas {:width 150 :height 150} camera scene tick]))
