(defproject gov.nih.nlm.nls/metamaplite "3.6.2rc3"
  :description "Clojure code for exploring MetaMapLite in lieu of a debugger."
  :url "http://usa.gov/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.apache.opennlp/opennlp-tools "1.5.3"]
                 [org.apache.opennlp/opennlp-maxent "3.0.3"]
                 [instaparse "1.4.5"]
                 [clojure-opennlp "0.3.3" :exclusions [instaparse]] ;; uses Opennlp 1.5.3
                 [org.apache.logging.log4j/log4j-api "2.1"]
                 [org.apache.logging.log4j/log4j-core "2.1"]
                 ;; [org.apache.lucene/lucene-analyzers-common "4.10.0"]
                 ;; [org.apache.lucene/lucene-queryparser "4.10.0"]
                 [org.apache.uima/uimaj-core "2.3.1"]
                 [net.sf.opencsv/opencsv "2.3"]
                 [gov.nih.nlm.nls/nlp "2.4.C"]
                 ;; https://mvnrepository.com/artifact/org.jyaml/jyaml
                 [org.jyaml/jyaml "1.3"]
                 ;; https://mvnrepository.com/artifact/org.yaml/snakeyaml
                 [org.yaml/snakeyaml "1.17"]
                 [io.forward/yaml "1.0.5"]
                 [context "2012"] ;; see context/* under src/main/java/
                 [bioc "1.0.1"]
                 ;; [gov.nih.nlm.nls/aec_mrd_wsd "1.0-SNAPSHOT"]
                 [junit/junit "4.11"]
                 [umls-tables "0.1.0-SNAPSHOT"]
                 [skr "0.1.0-SNAPSHOT"]
                 [lvgclj "0.1.0-SNAPSHOT"]
                 [clj-fuzzy "0.3.3"]
                 [info.debatty/java-string-similarity "0.23"]
                 [com.rpl/specter "1.0.0"]
                 [org.functionaljava/functionaljava "4.7"]
                 [org.functionaljava/functionaljava-java8 "4.7"]
                 [org.functionaljava/functionaljava-quickcheck "4.7"]
                 [org.functionaljava/functionaljava-java-core "4.7"]]
  :repositories [["ii" "https://metamap.nlm.nih.gov/maven2/"]]
  :profiles {:dev
             {:dependencies
              [[org.clojure/java.classpath "0.2.2"]]}
             :uberjar {:aot :all}}
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java" "src/test/java"]
  :test-paths ["src/test/java" "src/test/clojure"]
  :resource-paths ["src/main/resource"]
  :pom-plugins [[org.codehaus.mojo/exec-maven-plugin "1.4.0"
                 {:configuration ([:executable "java"]
                                  [:arguments ([:argument "-Xmx10g"]
                                              [:argument "-classpath"]
                                              :classpath
                                              [:argument "${classnameArg}"]
                                              [:argument "${arg0}"]
                                              [:argument "${arg1}"]
                                              [:argument "${arg2}"]
                                              [:argument "${arg3}"]
                                              [:argument "${arg4}"])])
                  :executions [:execution [:goals ([:goal "exec"])]]}]
                [org.apache.maven.plugins/maven-shade-plugin "2.4.1"
                 {:configuration ([:shadedArtifactAttached "true"]
                                  [:shadedClassifierName "standalone"])
                  :executions [:execution ([:phase "package"]
                                           [:goals ([:goal "shade"])])]}]]
  :marginalia {:javascript ["mathjax/MathJax.js?config=default"]})

