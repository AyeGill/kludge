(defproject kludge "0.0.1-SNAPSHOT"
  :description "A libGDX wrapper for easy cross-platform game development. Forked from play-clj."
  :url "https://github.com/AyeGill/kludge"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[com.badlogicgames.gdx/gdx "1.9.3"]
                 [com.badlogicgames.gdx/gdx-box2d "1.9.3"]
                 [com.badlogicgames.gdx/gdx-bullet "1.9.3"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.9.3"]
                 [danlentz/clj-uuid "0.1.7"]
                 [com.badlogicgames.gdx/gdx-platform "1.9.3" :classifier "natives-desktop"]
                 [org.clojars.oakes/clojure "1.6.0"]]
  :repl-options {:init-ns kludge.core}
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :source-paths ["src"]
  :java-source-paths ["src-java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"])
