#!/bin/sh

ANALYZERS=/usr/local/pub/ir/lucene/lucene-4.10.0/analysis/common/lucene-analyzers-common-4.10.0.jar
CORE=/usr/local/pub/ir/lucene/lucene-4.10.0/core/lucene-core-4.10.0.jar
QUERYPARSER=/usr/local/pub/ir/lucene/lucene-4.10.0/queryparser/lucene-queryparser-4.10.0.jar
OPENNLPTOOLS=/export/home/wjrogers/.m2/repository/org/apache/opennlp/opennlp-tools/1.5.3/opennlp-tools-1.5.3.jar
OPENNLPMAXENT=/export/home/wjrogers/.m2/repository/org/apache/opennlp/opennlp-maxent/3.0.3/opennlp-maxent-3.0.3.jar
JARSPATH=$ANALYZERS:$CORE:$QUERYPARSER:$OPENNLPTOOLS:$OPENNLPMAXENT

PROPERTIES="-Den-sent.bin.path=/usr/local/pub/nlp/opennlp/models/en-sent.bin \
    -Den-token.bin.path=/usr/local/pub/nlp/opennlp/models/en-token.bin \
    -Den-pos-maxent.bin.path=/usr/local/pub/nlp/opennlp/models/en-pos-maxent.bin" \

java -cp target/classes:$JARSPATH $PROPERTIES $*
