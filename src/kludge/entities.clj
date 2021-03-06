(ns kludge.entities
  (:require [clj-uuid :as uuid]
            [kludge.utils :as u])
  (:import [com.badlogic.gdx Gdx Graphics]
           [com.badlogic.gdx.graphics Camera Color GL20]
           [com.badlogic.gdx.graphics.g2d Batch NinePatch ParticleEffect Sprite
            TextureRegion]
           [com.badlogic.gdx.graphics.g3d Environment ModelBatch ModelInstance]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer]
           [com.badlogic.gdx.math Matrix4]
           [com.badlogic.gdx.scenes.scene2d Actor]))

(defprotocol Entity
  (draw! [this screen batch] "Draws the entity"))

(extend-protocol Entity
  clojure.lang.PersistentArrayMap
  (draw! [this screen batch])
  clojure.lang.PersistentHashMap
  (draw! [this screen batch])
  nil
  (draw! [this screen batch]))

(defrecord TextureEntity [object] Entity
  (draw! [{:keys [^TextureRegion object x y width height
                  scale-x scale-y angle color]}
          _
          batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getRegionWidth object)))
          height (float (or height (.getRegionHeight object)))]
      (when-let [[r g b a] color]
        (.setColor ^Batch batch r g b a))
      (if (or scale-x scale-y angle)
        (let [scale-x (float (or scale-x 1))
              scale-y (float (or scale-y 1))
              angle (float (or angle 0))]
          (.draw ^Batch batch object x y 0 0 width height
            scale-x scale-y angle))
        (.draw ^Batch batch object x y width height))
      (when color
        (.setColor ^Batch batch Color/WHITE)))))

(defrecord SpriteEntity [object] Entity
  (draw! [{:keys [^Sprite object
                          alpha
                          x y width height scale-x scale-y origin-x origin-y
                          alpha angle color]
           :or {x (.getX object)
                y (.getY object)
                width (.getWidth object)
                height (.getHeight object)
                scale-x (.getScaleX object)
                scale-y (.getScaleY object)
                origin-x (.getOriginX object)
                origin-y (.getOriginY object)
                angle (.getRotation object)
                color (.getColor object)}}
          _
          batch]
    (.setBounds object
                (float x)
                (float y)
                (float width)
                (float height))
    (.setOrigin object
                (float origin-x)
                (float origin-y))
    (.setScale object
               (float scale-x)
               (float scale-y))
    (.setRotation object angle)
    (if (instance? Color color)
      (.setColor object color)
      (let [[r g b a] color]
        (.setColor object r g b a)))
    (if alpha
      (.draw object ^Batch batch alpha)
      (.draw object ^Batch batch))))

(defrecord NinePatchEntity [object] Entity
  (draw! [{:keys [^NinePatch object x y width height]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getTotalWidth object)))
          height (float (or height (.getTotalHeight object)))]
      (.draw object ^Batch batch x y width height))))

(defrecord ParticleEffectEntity [object] Entity
  (draw! [{:keys [^ParticleEffect object x y delta-time]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          ^Graphics g (Gdx/graphics)
          delta-time (float (or delta-time (.getDeltaTime g)))]
      (.setPosition object x y)
      (.draw object ^Batch batch delta-time))))

(defrecord ActorEntity [object] Entity
  (draw! [{:keys [^Actor object x y width height
                  scale-x scale-y angle origin-x origin-y]} _ batch]
    (when (.getStage object)
      (some->> x (.setX object))
      (some->> y (.setY object))
      (some->> width (.setWidth object))
      (some->> height (.setHeight object))
      (when (or scale-x scale-y angle)
        (let [scale-x (float (or scale-x 1))
              scale-y (float (or scale-y 1))
              origin-x (float (or origin-x (/ (.getWidth object) 2)))
              origin-y (float (or origin-y (/ (.getHeight object) 2)))
              angle (float (or angle 0))]
          (.setScaleX object scale-x)
          (.setScaleY object scale-y)
          (.setOriginX object origin-x)
          (.setOriginY object origin-y)
          (.setRotation object angle)))
      (.draw object ^Batch batch 1))))

(defrecord ModelEntity [object] Entity
  (draw! [{:keys [^ModelInstance object x y z]}
          {:keys [^ModelBatch renderer ^Environment attributes]}
          _]
    (when (or x y z)
      (let [^Matrix4 m (. object transform)
            x (float (or x 0))
            y (float (or y 0))
            z (float (or z 0))]
        (.setTranslation m x y z)))
    (.render renderer object attributes)))

(defrecord ShapeEntity [object] Entity
  (draw! [{:keys [^ShapeRenderer object type draw!
                  x y scale-x scale-y angle]}
          {:keys [^Camera camera]}
          batch]
    (when batch
      (.end ^Batch batch))
    (when camera
      (.setProjectionMatrix object (. camera combined)))
    (.glEnable Gdx/gl GL20/GL_BLEND)
    (.glBlendFunc Gdx/gl GL20/GL_SRC_ALPHA GL20/GL_ONE_MINUS_SRC_ALPHA)
    (.begin object type)
    (when (or x y scale-x scale-y angle)
      (let [x (float (or x 0))
            y (float (or y 0))
            scale-x (float (or scale-x 1))
            scale-y (float (or scale-y 1))
            angle (float (or angle 0))]
        (.identity object)
        (.translate object x y 0)
        (.scale object scale-x scale-y 1)
        (.rotate object 0 0 1 angle)))
    (draw!)
    (.end object)
    (.glDisable Gdx/gl GL20/GL_BLEND)
    (when batch
      (.begin ^Batch batch))))

(defrecord BundleEntity [entities] Entity
  (draw! [{:keys [entities] :as entity} screen batch]
    (doseq [e entities]
      (draw! (merge e (apply dissoc entity (keys e))) screen batch))))

;code for working with entities map


(def +namespace-ent+ (uuid/v1))


(defn entity-uid
  "Get an uuid suitable for use as the reference to an entity.
  Can pass an argument to easily generate the same UID multiple times,
  although this is effectively creating global state which is bad form"
  ([]
    (uuid/v1))
  ([object] (uuid/v3 +namespace-ent+ object)))
(defn assoc-entity [entities entity key val]
  (assoc-in entities [entity key] val))

(defmacro update-entity [entities entity key f & args]
  `(update-in ~entities [~entity ~key] ~f ~@args))

(defn get-entity [entities entity key]
  (get-in entities [entity key]))

(defn create-entity
  "Creates a new entity. Note that the uid is not returned"
  ([entities uid record]
    (assoc entities uid record))
  ([entities record]
    (create-entity entities (entity-uid) record)))

(defn create-entities [entities records]
  (reduce create-entity entities records))

(comment
  "Component system probably shouldn't be in this namespace (maybe not entity record stuff either).
   Commented out for now"
(defn add-component-manager! [screen]
  (update! screen :component-manager {})) ;Should map component->uid set

(defn assoc-component
  "Like assoc-entity, but adds a record of entity having value"
  [screen entities uid key value]
    (flag-entity-component screen uid key)
    (assoc-entity entities uid key value))

(defn flag-entity-component [screen uid key]
  (update! screen :component-manager (update (:component-manager screen) key (fnil conj #{}) uid)))

(defn ents-with-component [screen key]
  (or (get-in screen [:component-manager key]) #{}))

(defn ents-with-components
  "Returns set of uids of entities with all components."
  [screen keys]
  (or (apply clojure.set/intersection (map (partial ents-with-component screen) keys)) #{}))

(defn update-with-components
  "Updates only those entities with certain registered components. Fun should be record->record"
  [screen entities keys fun]
  (reduce (fn [ent-map uid] (update ent-map uid fun)) entities (ents-with-components [screen keys])))

;Should try to eliminate redundancy between argslist and keys?
(defmacro with-components
  "Macro version of update-with-components. Just cleans away the (fun [...) bits.
  (with-components screen entities [:physics :explosive :homing]
                   [{:keys physics explosive homing :as entity}]
                   (if (check-stuff explosive physics ...)))"
  [screen entities keys arglist & forms]
  `(update-with-components ~screen ~entities ~keys (fn ~arglist ~@forms)))
)
