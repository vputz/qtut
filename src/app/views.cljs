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
              r (doto (create-renderer e)
                  (.setClearColor 0xffffff 1.0))
                                        ;              e (js/THREE.OutlineEffect. r)
              ]
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

(defn bloch-arrow
  [phi theta radius]
  (let [dir (doto (js/THREE.Vector3. (* (.cos js/Math phi) (.sin js/Math theta))
                                     (* (.sin js/Math phi) (.sin js/Math theta))
                                     (.cos js/Math theta))
              (.normalize))
        origin (js/THREE.Vector3. 0 0 0)
        color 0x000000
        arrow (js/THREE.ArrowHelper. dir origin radius color)]
    arrow))

(defn bloch-sphere-group
  [radius]
  (let [
        axis-radius (+ radius 2)
        widthsegments 50
        heightsegments 50
        axis-opacity 0.7
        sphere-opacity 0.1
        xplane (js/THREE.CircleBufferGeometry. axis-radius widthsegments)
        yplane (doto (js/THREE.CircleBufferGeometry. axis-radius widthsegments)
                 (.rotateY (/ 3.14 2)))
        zplane (doto (js/THREE.CircleBufferGeometry. axis-radius widthsegments)
                 (.rotateX (/ 3.14 2)))
        sphere (js/THREE.SphereBufferGeometry. radius widthsegments heightsegments)
        smaterial (js/THREE.MeshBasicMaterial. #js {:color 0x000000
                                                    :wireframe false
                                                    :wireframeLinewidth 0.5
                                                    :transparent true
                                                    :side js/THREE.DoubleSide
                                                    :opacity sphere-opacity})
        x-material (js/THREE.MeshToonMaterial. #js {:color "blue"
                                                    :wireframe false
                                                    :transparent true
                                                    :side js/THREE.DoubleSide
                                                    :opacity axis-opacity})
        y-material (js/THREE.MeshToonMaterial. #js {:color "red"
                                                    :wireframe false
                                                    :transparent true
                                                    :side js/THREE.DoubleSide
                                                    :opacity axis-opacity})
        z-material (js/THREE.MeshToonMaterial. #js {:color 0x00ff00;"green"
                                                    :wireframe false
                                                    :transparent true
                                                    :side js/THREE.DoubleSide
                                                    :opacity axis-opacity})
        smesh (js/THREE.Mesh. sphere smaterial)
        xplane-mesh (js/THREE.Mesh. xplane x-material)
        yplane-mesh (js/THREE.Mesh. yplane y-material)
        zplane-mesh (js/THREE.Mesh. zplane z-material)]
    (doto (js/THREE.Group.)
      (.add xplane-mesh)
      (.add yplane-mesh)
      (.add zplane-mesh)
      (.add smesh)
      )
    ))

(defn bloch-sphere-component
  "A bloch sphere represented in Three.js"
  [alpha beta]
  (let [
        radius 50
        viewdist 150
        camphi (/ (.-PI js/Math) 4)
        camtheta (/ (.-PI js/Math) 3)
        arrow-phi (/ (.-PI js/Math) 2)
        arrow-theta (/ (.-PI js/Math) 4)
        sphere        (bloch-sphere-group radius)
        arrow (bloch-arrow arrow-phi arrow-theta radius)
        scene (doto (js/THREE.Scene.)
                (.add (js/THREE.AmbientLight. 0x888888))
                (.add (doto (js/THREE.DirectionalLight. 0xffff88 0.5)
                        (-> (.-position) (.set -600 300 600))))
                (.add (js/THREE.AxisHelper. 50)) ; x red y green z blue
                (.add arrow)
                (.add sphere)
                )
        camera (doto (js/THREE.PerspectiveCamera. 70 1 1 1000)
                 (-> (.-position) (.set (* viewdist (* (js/Math.cos camphi)
                                                     (js/Math.sin camtheta)) )
                                      (* viewdist (* (js/Math.sin camphi)
                                                     (js/Math.sin camtheta)))
                                      (* viewdist (js/Math.cos camtheta))))
                 (-> (.-up) (.set 0 0 1))

                 (.lookAt (.-position scene)))
        tick (fn []
                                        ; (fly-around-z-axis camera scene)
               )]
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
