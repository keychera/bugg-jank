(ns build.lets
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defonce bare-basis (delay (b/create-basis {:project "deps.edn" :aliases [:default-main]})))
(def target-dir "target")

(defn log [& stuff]
  (apply println "[bermain@jank]" stuff))

;; jank pseudo-deps-edn, powered by https://github.com/babashka/tools.bbuild

(defn ->jank-deps-edn [basis]
  (let [{:keys [jank classpath]} basis
        no-clojure-classpath
        (into [] (remove #(and (str/includes? % "org")
                               (str/includes? % "clojure")))
              (keys classpath))]
    (assoc jank :module-path (str/join File/pathSeparator no-clojure-classpath))))

(defn get-main-module [deps-edn]
  (-> deps-edn :argmap :main str))

(defn ->flags [flag coll]
  (into [] (mapcat (fn [entry] [(str flag entry)])) coll))

(def wherejank
  (try (edn/read-string (slurp "wherejank.edn"))
       (catch Throwable _ {:home nil :bin "jank"})))

(defn jank-command
  ([jank-deps-edn] (jank-command jank-deps-edn nil {}))
  ([jank-deps-edn command {:keys [main-module extra]}]
   (let [{:keys [module-path include-dirs library-dirs linked-libraries]} jank-deps-edn]
     (into []
           (remove nil?)
           (concat
            [(or (:bin wherejank) "jank") "--module-path" module-path]
            (->flags "-I" include-dirs)
            (->flags "-L" library-dirs)
            (->flags "-l" linked-libraries)
            [command main-module]
            extra)))))

(defn prep-basis [which-bug]
  (let [bug-key (keyword (str "bug-" which-bug))]
    (b/create-basis {:project "deps.edn" :aliases [:default-main bug-key]})))

(defn jank
  {:org.babashka/cli {:exec-args {:bug 0} :coerce {:bug :int}}}
  [{which-bug :bug}]
  (let [base (prep-basis which-bug)
        jedn (->jank-deps-edn base)
        flag (-> base :argmap :jank-flags)
        jcmd (jank-command jedn "run-main" {:main-module (get-main-module base) :extra flag})]
    (log (str "jank run-main for :bug-" which-bug))
    (log "pseudo deps.edn with" (:argmap base))
    (log "jank command:" jcmd)
    (b/process {:command-args jcmd})))

(defn compile-jank
  {:org.babashka/cli {:exec-args {:bug 0} :coerce {:bug :int}}}
  [{which-bug :bug}]
  (let [base (prep-basis which-bug)
        jedn (->jank-deps-edn base)
        jout (-> base :jank :compile-out)
        flag (-> base :argmap :jank-flags)
        jcmd (jank-command jedn "compile" {:main-module (get-main-module base) :extra (concat flag ["--name" jout "-O3"])})]

    (log (str "jank compile for :bug-" which-bug))
    (log "pseudo deps.edn with" (:argmap base))
    (log "jank command:" jcmd)
    (b/process {:command-args jcmd})))
