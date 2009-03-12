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
(add-poly "Clojure RULES" 4000,3500 3800,3900 4300,4100 4700,3600 4400,3400)
(add-poly "A1" 3725.75,3937.95 3725.75,3970.25 4073.65,3970.25 4073.65,3937.95)
(add-poly "A2" 3725.75,3937.95 3725.75,3679.70 4073.65,3679.70 4073.65,3937.95)
(add-poly "A3" 3915.41,3602.90 3915.41,3681.35 4077.13,3681.35 4077.13,3602.90)
(add-poly "B1" 4073.65,3937.95 4073.65,3970.25 4189.15,3970.25 4189.15,3937.95)
(add-poly "B2" 4073.65,3681.35 4073.65,3937.95 4189.15,3937.95 4189.15,3681.35)
(add-poly "B3" 4073.65,3638.42 4073.65,3681.35 4189.15,3681.35 4189.15,3638.42)
(add-poly "C1" 4189.15,3937.95 4189.15,3970.25 4303.65,3970.25 4303.65,3937.95)
(add-poly "C2" 4189.15,3681.35 4189.15,3937.95 4303.65,3937.95 4303.65,3681.35)
(add-poly "C3" 4189.15,3638.42 4189.15,3681.35 4303.65,3681.35 4303.65,3638.42)
(add-poly "D1" 4303.65,3937.95 4303.65,3970.25 4420.15,3970.25 4420.15,3937.95)
(add-poly "D2" 4303.65,3813.90 4303.65,3937.95 4420.15,3937.95 4420.15,3813.90)
(add-poly "E3" 4407.785,3691.35 4550.374,3691.35 4550.374,3640.00 4407.785,3640.00)
(add-poly "F3" 4528.726,3691.35 4661.15,3691.35 4661.15,3640.00 4528.726,3640.00)
(add-poly "G3" 4641.15,3701.178 4774.80,3701.178 4774.80,3640.00 4641.15,3640.00)
(add-poly "G4" 4641.15,3660.00 4774.80,3660.00 4774.80,3618.90 4729.35,3618.90 4729.35,3555.95 4641.15,3555.95)
(add-poly "F4" 4485.05,3660.00 4661.15,3660.00 4661.15,3555.95 4605.05,3555.95 4605.05,3617.791 4485.08,3617.791)

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


