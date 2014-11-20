#!/bin/sh

ANALYZERS=$HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/4.10.0/lucene-analyzers-common-4.10.0.jar
CORE=$HOME/.m2/repository/org/apache/lucene/lucene-core/4.10.0/lucene-core-4.10.0.jar
QUERYPARSER=$HOME/.m2/repository/org/apache/lucene/lucene-queryparser/4.10.0/lucene-queryparser-4.10.0.jar
OPENNLPTOOLS=$HOME/.m2/repository/org/apache/opennlp/opennlp-tools/1.5.3/opennlp-tools-1.5.3.jar
OPENNLPMAXENT=$HOME/.m2/repository/org/apache/opennlp/opennlp-maxent/3.0.3/opennlp-maxent-3.0.3.jar
LOG4JAPI=$HOME/.m2/repository/org/apache/logging/log4j/log4j-api/2.1/log4j-api-2.1.jar
LOG4JCORE=$HOME/.m2/repository/org/apache/logging/log4j/log4j-core/2.1/log4j-core-2.1.jar

JARSPATH=$ANALYZERS:$CORE:$QUERYPARSER:$OPENNLPTOOLS:$OPENNLPMAXENT:$LOG4JAPI:$LOG4JCORE

OPENNLP_MODELS=/usr/local/pub/nlp/opennlp/models

JVMOPTS="-Den-sent.bin.path=$OPENNLP_MODELS/en-sent.bin \
    -Den-token.bin.path=$OPENNLP_MODELS/en-token.bin \
    -Den-pos-maxent.bin.path=$OPENNLP_MODELS/en-pos-maxent.bin \
    -Dlog4j.configurationFile=$PWD/config/log4j2.xml"

java -cp target/classes:$JARSPATH $JVMOPTS gov.nih.nlm.nls.metamap.lite.Pipeline $* 
