(ns build.lets
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defn log [& stuff] (apply println "[bermain@jank]" stuff))

;; jank pseudo-deps-edn, powered by just babashka

(defn ->jank-deps-edn [basis]
  (let [{:keys [jank classpath argmap]} basis
        no-clojure-classpath
        (into [] (remove #(and (str/includes? % "org")
                               (str/includes? % "clojure")))
              (keys classpath))]
    (-> jank
        (assoc :module-path (str/join File/pathSeparator no-clojure-classpath))
        (merge (:jank argmap)))))

(defn ->flags [flag coll]
  (into [] (mapcat (fn [entry] [(str flag entry)])) coll))

(def wherejank
  (try (edn/read-string (slurp "wherejank.edn"))
       (catch Throwable _ {:home nil :bin "jank"})))

(defn jank-command
  [jank-deps-edn command]
  (let [{:keys [module-path include-dirs library-dirs linked-libraries extras main-module main-args]} jank-deps-edn]
    (into []
          (remove nil?)
          (concat
           [(or (:bin wherejank) "jank") "--module-path" module-path]
           (->flags "-I" include-dirs)
           (->flags "-L" library-dirs)
           (->flags "-l" linked-libraries)
           extras
           [command (str main-module)]
           ["--"]
           main-args))))

(defn prep-basis [which-bug]
  (let [bug-key (keyword (str "bug-" which-bug))]
    (b/create-basis {:project "deps.edn" :aliases [:default-main bug-key]})))

(defn jank
  {:org.babashka/cli {:exec-args {:args [] :bug 0}
                      :coerce {:args [:string] :bug :int}}}
  [{which-bug :bug main-args :args}]
  (let [base (prep-basis which-bug)
        jedn (assoc (->jank-deps-edn base) :main-args main-args)
        jcmd (jank-command jedn "run-main")]
    (log (str "jank run-main for :bug-" which-bug))
    (log "pseudo deps.edn with" jedn)
    (log "jank command:" jcmd)
    (b/process {:command-args jcmd})))

(defn compile-jank
  {:org.babashka/cli {:exec-args {:bug 0} :coerce {:bug :int}}}
  [{which-bug :bug}]
  (let [base (prep-basis which-bug)
        jedn (->jank-deps-edn base)
        jout (-> jedn :compile-out)
        jedn (update jedn :extras (fnil conj []) "--name" jout "-O3")
        jcmd (jank-command jedn "compile")]
    (log (str "jank run-main for :bug-" which-bug))
    (log "pseudo deps.edn with" jedn)
    (log "jank command:" jcmd)
    (b/process {:command-args jcmd})))
