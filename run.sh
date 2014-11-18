#!/bin/sh

ANALYZERS=$HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/4.10.0/lucene-analyzers-common-4.10.0.jar
CORE=$HOME/.m2/repository/org/apache/lucene/lucene-core/4.10.0/lucene-core-4.10.0.jar
QUERYPARSER=$HOME/.m2/repository/org/apache/lucene/lucene-queryparser/4.10.0/lucene-queryparser-4.10.0.jar
OPENNLPTOOLS=$HOME/.m2/repository/org/apache/opennlp/opennlp-tools/1.5.3/opennlp-tools-1.5.3.jar
OPENNLPMAXENT=$HOME/.m2/repository/org/apache/opennlp/opennlp-maxent/3.0.3/opennlp-maxent-3.0.3.jar
JARSPATH=$ANALYZERS:$CORE:$QUERYPARSER:$OPENNLPTOOLS:$OPENNLPMAXENT

OPENNLP_MODELS=/usr/local/pub/nlp/opennlp/models

PROPERTIES="-Den-sent.bin.path=$OPENNLP_MODELS/en-sent.bin \
    -Den-token.bin.path=$OPENNLP_MODELS/en-token.bin \
    -Den-pos-maxent.bin.path=$OPENNLP_MODELS/en-pos-maxent.bin" \

java -cp target/classes:$JARSPATH $PROPERTIES $*
