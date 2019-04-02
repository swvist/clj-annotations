(ns clj-annotations.core
  "Access and manipulate annotations.

  Annotations are arbitrary properties associated with values that can be used for a
  variety of use cases. Typically you define a \"schema\" using `defschema` specifying its
  attributes Each of the schemas and attributes can have arbitrary properties - key/value
  pairs - associated with them.

  This namespace provides functions and macros to define and access annotations.")


;;
;; Annotation accessor functions
;;

(defn get-annotations
  "Access the annotations associated with a schema, an attribute, and a property. Returns
  all properties if only `schema` and `attribute` are provided. Returns `nil` if the
  specified property does not exist, or the `not-found` value if supplied."
  ([schema]
   (:attributes schema))
  ([schema attr]
   (get-in schema [:attributes attr]))
  ([schema attr prop]
   (get-in schema [:attributes attr prop]))
  ([schema attr prop not-found]
   (get-in schema [:attributes attr prop] not-found)))

(defn scan-attributes
  "Fetch a sequence of attribute ids of a schema having a specified property set to the
  specified value."
  [schema prop value]
  (letfn [(prop-check [[attr props]]
            (= (get props prop) value))]
    (map first (filter prop-check (:attributes schema)))))


;;
;; Annotation definition functions
;;

(defn merge-attrs
  "a1 and a2 are annotation attribute maps of the form {:attr1 m1, :attr2 m2, ...} and
  {:attr1 n1, :attr2 n2, ...}. merge-attrs produces a map of the form {:attr1 (merge m1
  n1), :attr2 (merge m2 n2), ...}. If an attribute exists only in one map, that value is
  used."
  [a1 a2]
  (merge-with merge a1 a2))

(defn make-schema
  [{:keys [include attributes] :as m}]
  (let [attrs (merge-attrs (:attributes include) attributes)]
    (-> (merge include m)
        (dissoc :include)
        (assoc :attributes attrs)
        (with-meta {:type ::schema}))))

(defmacro defschema
  "Define a schema and binds it to `name`.

  `kvs` are key-value pairs. The following keys are defined:

  `:include`:     Include the kvs from another schema to this schema. If a key occurs in
                  both the schemas, the one in this schema wins.
  `:name`:        A short name for this schema used for documentation purposes.
  `:description`: A description for this schema used for documentation purposes.
  `:attributes`:  A map from attribute IDs to attribute annotations."
  [name & kvs]
  `(defonce ~name (make-schema (assoc {} ~@kvs))))