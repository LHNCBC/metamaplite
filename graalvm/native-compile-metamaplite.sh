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
# GRAALVM=$TOOLS/graalvm-ce-19.2.1
# M2_REPO=$HOME/.m2/repository
PATH=$PATH:$GRAALVM/bin
native-image -H:+ReportUnsupportedElementsAtRuntime \
	     -H:ReflectionConfigurationFiles=graalvm/graalvm-metamaplite.json \
	     -H:IncludeResources=config/log4j2.xml \
	     -H:+ReportExceptionStackTraces \
	     -H:-UseServiceLoaderFeature \
	     --allow-incomplete-classpath \
	     --initialize-at-run-time=org.apache.logging.log4j.core.async.AsyncLogger \
	     --initialize-at-run-time=org.apache.logging.log4j.core.appender.mom.JmsAppender \
	     --enable-url-protocols=http \
	     --enable-url-protocols=https \
	     -cp target/metamaplite-3.6.2rc4-standalone.jar:$M2_REPO/org/osgi/org.osgi.core/4.3.0/org.osgi.core-4.3.0.jar \
	     gov.nih.nlm.nls.ner.MetaMapLite
