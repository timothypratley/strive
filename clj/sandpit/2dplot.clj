;; 2dplot.clj
;;
;; Ever wanted to visualise some simple shapes but don't have matlab handy?
;; Here is one way to do it!
;; Comes with some default shapes to demonstrate,
;; but I recommend you run it interactively
;; user=> (load-file "2dplot.clj")
;; (clean)				to remove any existing shapes
;; (add-poly "label" x1,y1 x2,y2 ...)	to add new shapes
;; (init-view)				sets the view to see everything
;;
;; Keyboard controls are the default java controls:
;; forward arrow - move forward
;; left arrow - turn left
;; page down - look up
;; etc...
;; These allow you to move around the plot.
;;
;; Timothy Pratley, 2008
;; timothypratley@gmail.com

; You need to have java3d installed
(import '(javax.media.j3d BranchGroup LineArray GeometryArray Shape3D Transform3D TransformGroup BoundingSphere Background)
        '(javax.vecmath Point3f Vector3f Point3d Color3f)
        '(com.sun.j3d.utils.universe SimpleUniverse)
        '(com.sun.j3d.utils.applet MainFrame)
        '(com.sun.j3d.utils.behaviors.keyboard KeyNavigatorBehavior)
        '(com.sun.j3d.utils.geometry Text2D)
        '(java.applet Applet)
        '(java.awt BorderLayout Font))

; See Figure 1-7 - Chapter 1. Getting Started (Java3D Tutorial)
; The scene graph comprises:
; universe - contains the view related tree
; content - contains the visual objects branch, and is a child of universe
(def #^SimpleUniverse *universe* (SimpleUniverse.))
(def #^BranchGroup *content* (BranchGroup.))
(.setCapability *content* BranchGroup/ALLOW_CHILDREN_EXTEND)
(.setCapability *content* BranchGroup/ALLOW_CHILDREN_READ)
(.setCapability *content* BranchGroup/ALLOW_CHILDREN_WRITE)

; add a keyboard navigation behavior
; and associate the content with the viewer
(.addBranchGraph
  *universe*
  (doto (BranchGroup.)
    (.addChild
      (doto (KeyNavigatorBehavior.
              (-> *universe* .getViewingPlatform .getViewPlatformTransform))
        (.setSchedulingBounds (BoundingSphere. (Point3d.) 10000.0))))
  (.addChild *content*)))

; hack to let us see stuff - default limits view ditances too much
(-> *universe* .getViewer .getView (.setBackClipDistance 1000))
(-> *universe* .getViewer .getView (.setFrontClipDistance 1))
(doto (Background.)
  (.setApplicationBounds (BoundingSphere. (Point3d.) 10000.0)))

; function to set the initial viewpoint to see everything
(defn init-view []
  (let [content-center (Point3d.)]
    (-> *content* .getBounds (.getCenter content-center))
    (-> *universe* .getViewingPlatform .getViewPlatformTransform
      (.setTransform
        (doto (Transform3D.)
          (.setTranslation
            (Vector3f.
	      (.x content-center)
	      (.y content-center)
	      ; not sure if 3 is a constant, just experimented to find
 	      (* 3 (-> *content* .getBounds .getRadius))))))))
   (-> *universe* .getViewingPlatform .getViewPlatformTransform))

; Convert a list of coordinates into lines
(defn coords2points [[x y & coords]]
  (if coords (cons [x y] (coords2points coords)) [[x y]]))
(defn points2lines-helper [[a & more]]
  (if more
    (cons a (cons a (points2lines-helper more)))
    [a a]))
(defn points2lines [[a & more]]
  (concat (cons a (points2lines-helper more)) [a]))
(points2lines '[a b c d])
(defn
  #^{:test (fn []
    (assert (= (coords2lines '[xa ya xb yb xc yc xd yd])
      '[[xa ya] [xb yb] [xb yb] [xc yc] [xc yc] [xd yd] [xd yd] [xa ya]])))}
  coords2lines [coords]
  (points2lines (coords2points coords)))

(defn rand-color []
  (Color3f.  (+ 0.2 (rand 0.8)) (+ 0.2 (rand 0.8)) (+ 0.2 (rand 0.8))))

; Create a polygon ready for insertion into the content.
(defn create-poly [#^String label points]
  (let [lines (LineArray. (count points)
	  (bit-or GeometryArray/COORDINATES GeometryArray/COLOR_3))
        color (rand-color)
        text (Text2D. label color "Helvetica" 24 Font/PLAIN)]
    ; collect all the lines of the polygon into a LineArray
    (dorun (map (fn [idx [x y]]
            (.setCoordinate lines (int idx) (Point3f. x y 0))
            (.setColor lines (int idx) color))
          (iterate inc 0) points))
    ; add a text label to the shape and
    ; wrap it up in a BranchGroup or it can't be added to live content
    (let [shape (Shape3D. lines)
          shape-up (Point3d.)
          shape-low (Point3d.)]
      (-> shape .getBounds (.getUpper shape-up))
      (-> shape .getBounds (.getLower shape-low))
      (.setRectangleScaleFactor text
        (/ (- (.x shape-up) (.x shape-low)) 48 (.length label)))
      (let [text-upper (Point3d.)
	    text-lower (Point3d.)]
        (-> text .getBounds (.getUpper text-upper))
        (-> text .getBounds (.getLower text-lower))
        (let [text-width (- (.x text-upper) (.x text-lower))
              text-height (- (.y text-upper) (.y text-lower))]
          (doto (BranchGroup.)
            (.setCapability BranchGroup/ALLOW_DETACH)
            (.addChild (Shape3D. lines))
            (.addChild
              (doto (TransformGroup.)
                (.addChild text)
	        (.setTransform
                  (doto (Transform3D.)
	            (.setTranslation
                      (Vector3f.
		        (- (/ (+ (.x shape-low) (.x shape-up)) 2)
                          (/ text-width 2))
		        (- (/ (+ (.y shape-low) (.y shape-up)) 2)
                          (/ text-height 2))
		        0))))))))))))

; Add a polygon defined by points to the scene graph content
; points is of the form [[x1 y1] [x2 y2] [x3 y3]]
(defn add-poly [label & coords]
  (let [lines (coords2lines coords)
	poly (create-poly label lines)]
    ; compact the geometry
    (.compile poly)
    (.addChild *content* poly)
    poly))

(defn clean []
  (.removeAllChildren *content*)
  *content*)

;; add-poly adds a new shape to the plot.
;; User can add these dynamically
;; eg: if you do a (load-file "2dplot.clj") from the REPL
;; then type (add-poly "label" x1,y1 x2,y2 ...)
;; it will add it right in... a command driven drawing program :)
(add-poly "A1" 3915.415,3970.098 4083.650,3970.098 4083.650,3915.200 3915.415,3915.200)
(add-poly "A2" 3915.42,3935.2 4083.650,3935.200 4083.65,3671.35 3915.42,3671.35)
(add-poly "A3" 3915.42,3691.35 4082.87,3691.35 4087.22,3645.43 4077.13,3638.43 4052,3638.53 4052,3600.54 3915.42,3600.54)
(add-poly "B1" 4063.65,3970.1 4199.15,3970.1 4199.15,3915.2 4063.65,3915.19)
(add-poly "B2" 4063.65,3935.2 4199.15,3935.2 4199.15,3671.35 4063.65,3671.35)
(add-poly "B3" 4063.65,3691.35 4199.22,3691.35 4199.22,3645.43 4087.22,3645.43 4077.13,3638.43 4067.09,3638.43 4063.65,3671.35)
(add-poly "C1" 4179.15,3970.1 4314.65,3970.1 4314.65,3915.2 4179.15,3915.2)
(add-poly "C2" 4179.15,3935.2 4314.65,3935.2 4314.65,3671.35 4179.15,3671.35)
(add-poly "C3" 4179.15,3691.35 4314.65,3691.35 4313.52,3645.43 4178.91,3645.43)
(add-poly "D1" 4294.65,3970.1 4430.15,3970.1 4430.15,3915.2 4294.65,3915.2)
(add-poly "D2" 4294.65,3935.2 4430.15,3935.2 4430.15,3671.35 4294.65,3671.35)
(add-poly "D3" 4294.96,3691.35 4430.73,3691.35 4428.13,3645.71 4293.52,3645.43)
(add-poly "E1" 4410.15,3970.1 4550.05,3970.1 4550.05,3915.2 4410.15,3915.2)
(add-poly "E2" 4410.15,3935.2 4550.05,3935.2 4550.05,3671.35 4410.15,3671.35)
(add-poly "E3" 4410.15,3691.35 4550.05,3691.35 4549.05,3640 4495.05,3640 4475.43,3645.43 4408.13,3645.71)
(add-poly "F1" 4530.05,3970.1 4661.15,3970.1 4661.15,3915.2 4530.05,3915.2)
(add-poly "F2" 4530.05,3935.2 4661.15,3935.2 4661.15,3671.35 4530.05,3671.35)
(add-poly "F3" 4528.73,3691.35 4528.73,3640 4661.15,3640 4661.15,3691.35)
(add-poly "G1" 4641.15,3970.09 4776.65,3970.1 4776.65,3915.2 4641.15,3915.2)
(add-poly "G2" 4641.15,3935.2 4776.71,3935.2 4775.28,3681.18 4641.15,3681.18)
(add-poly "G3" 4641.15,3701.18 4801.5,3701.18 4801.5,3640 4641.15,3640)
(add-poly "G4" 4641.23,3660 4774.8,3660 4774.8,3618.9 4729.35,3618.9 4729.35,3555.95 4641.15,3555.95)
(add-poly "F4" 4475.43,3660 4661.15,3660 4661.15,3555.95 4605.05,3555.95 4605.05,3617.79 4503.35,3617.79 4475.43,3645.43)

;; set the camera to see all the shapes added,
;; NB you can call this after adding new shapes.
(init-view)

; Somehow a window pops up without this code,
; This just makes the window larger than the default
(doto (MainFrame. (doto (Applet.)
    (.setLayout (BorderLayout.))
	(.add "Center" (.getCanvas *universe*)))
    1024 512)
  (.setTitle "2D Plotting in Clojure"))


