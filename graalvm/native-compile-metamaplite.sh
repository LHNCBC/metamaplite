#!/bin/sh
#
# Generate native image of MetaMapLite using GraalVM's native-image
# using metamaplite standalone jar
#
# From toplevel directory:
#
#    $ GRAALVM=<directory where grallvm is installed>
#    $ M2_REPO=<maven repository directory (usually $HOME/.m2/repository)>
#    $ mvn package
#    $ bash graalvm/native-compile-metamaplite.sh
#
# For more information see also:
#  
#  "Configuring Graal Native AOT for reflection": https://blog.frankel.ch/configuring-graal-native-aot-reflection/
#  GraalVM main website: https://www.graalvm.org
#  GraalVM Github page: https://github.com/oracle/graal
#  GrallVM Community Edition (What I used for this): https://github.com/oracle/graal/releases
#
GRAALVM=$TOOLS/graalvm-ce-1.0.0-rc14
# M2_REPO=$HOME/.m2/repository
PATH=$PATH:$GRAALVM/bin
native-image -H:+ReportUnsupportedElementsAtRuntime \
	     -H:ReflectionConfigurationFiles=graalvm/graalvm-metamaplite.json \
	     -H:IncludeResources=config/log4j2.xml \
	     -H:+ReportExceptionStackTraces \
	     -H:-UseServiceLoaderFeature \
	     --allow-incomplete-classpath \
	     --enable-url-protocols=http \
	     --enable-url-protocols=https \
	     -cp target/metamaplite-3.6.2rc8-standalone.jar\
	     gov.nih.nlm.nls.ner.MetaMapLite

# --initialize-at-run-time=org.apache.logging.log4j.core.async.AsyncLogger \
# --initialize-at-run-time=org.apache.logging.log4j.core.appender.mom.JmsAppender \
# :$M2_REPO/org/osgi/org.osgi.core/4.3.0/org.osgi.core-4.3.0.jar 
