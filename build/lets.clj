(ns build.lets
  (:require
   [babashka.fs :as fs]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defonce deps-basis (delay (b/create-basis {:project "deps.edn" :aliases [:release]})))
(defonce repl-dev-basis (delay (b/create-basis {:project "deps.edn" :aliases [:repl-dev]})))
(def target-dir (delay (or (-> @deps-basis :jank :target-dir) "target")))

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
  (into [] (mapcat (fn [entry] [flag entry])) coll))

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

(defn prep-kondo
  [{}]
  (try
    (let [{:keys [module-path]} (->jank-deps-edn @deps-basis)]
      (b/process {:command-args ["clj-kondo" "--lint" module-path
                                 "--dependencies" "--copy-configs" "--skip-lint"]}))
    (catch Throwable err
      (log "error when running clj-kondo! cause:" (:cause (Throwable->map err))))))

(defn tell-clangd ;; about our project
  [{}]
  (spit "compile_flags.txt"
        (->> (concat (jank-command (->jank-deps-edn @deps-basis))
                     ["-I" (str (:home wherejank) "/compiler+runtime/include/cpp")])
             (drop 3)
             (str/join "\n"))))

(defn prep [args]
  (prep-kondo args)
  (tell-clangd args))

(defn clean [& _]
  (println "cleaning target...")
  (b/delete {:path @target-dir}))

(defn jank
  {:org.babashka/cli {:exec-args {:profile? false
                                  :args []}
                      :coerce {:profile? :bool
                               :args [:string]}}}
  [{:keys [args profile?]}]
  (log "setting up jank pseudo deps.edn project" args)
  (let [jedn (->jank-deps-edn @deps-basis)
        jcmd (jank-command
              jedn "run-main"
              {:main-module (get-main-module @deps-basis)
               :extra (flatten [(when profile? ["--profile"]) "--" args] )})]
    (log jcmd)
    (b/process
     (let [JANK_EXTRA_FLAGS (str/join (cond-> []
                                        profile? (conj "-gcodeview")))]
       {:env {"JANK_EXTRA_FLAGS" JANK_EXTRA_FLAGS}
        :command-args jcmd}))))

(defn repl-dev
  [{:keys [args]}]
  (log "setting up jank pseudo deps.edn project" args)
  (let [jedn (->jank-deps-edn @repl-dev-basis)
        jcmd (jank-command jedn "repl" {:main-module (get-main-module @repl-dev-basis)})]
    (log jcmd)
    (b/process {:command-args jcmd})))

(defn compile-jank
  {:org.babashka/cli {:exec-args {:profile? false}
                      :coerce {:profile? :bool}}}
  [{:keys [profile?] :as args}]
  (println "compile-jank with" args)
  (let [jedn (->jank-deps-edn @deps-basis)
        jout (cond-> (-> @deps-basis :jank :compile-out)
               profile? (str "---profile"))
        jcmd (jank-command jedn "compile" {:main-module (get-main-module @deps-basis)
                                           :extra ["--name" jout "-O3"]})]
    (log jcmd)
    (let [JANK_EXTRA_FLAGS
          (str/join " "
                    (cond-> []
                      ;; https://discourse.llvm.org/t/generate-coff-symbols-even-when-generating-pdb/80488/2
                      profile? (conj "-g -gcodeview"
                                     "-fuse-ld=lld"
                                     "-Wl,/debug:full"
                                     "-Wl,/pdbaltpath:%_PDB%")
                      (fs/windows?) (conj "-Wl,-subsystem,windows")))]
      (b/process {:env {"JANK_EXTRA_FLAGS" JANK_EXTRA_FLAGS}
                  :command-args jcmd}))))

(defn play [& _]
  (let [main-exe (str @target-dir "/" (-> @deps-basis :jank :compile-out) ".exe")]
    (b/process {:command-args [main-exe]})))

(defn ship [& _]
  (let [jout (-> @deps-basis :jank :compile-out)
        🚢   (str "ship/" jout)
        exe  (str 🚢 "/" jout ".exe")]
    (io/make-parents exe)
    (fs/copy-tree "res" (str 🚢 "/res") {:replace-existing true})
    (fs/copy (str @target-dir "/" jout ".exe") exe {:replace-existing true})
    (fs/zip (str 🚢 ".zip") 🚢 {:root "ship"})))
