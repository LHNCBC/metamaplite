#!/bin/sh

WORKAREA=/net/lhcdevfiler/vol/cgsb5/ind/II_Group_WorkArea
PROJECTDIR=$WORKAREA/wjrogers/Projects/metamaplite

ANALYZERS=$HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/4.10.0/lucene-analyzers-common-4.10.0.jar
CORE=$HOME/.m2/repository/org/apache/lucene/lucene-core/4.10.0/lucene-core-4.10.0.jar
QUERYPARSER=$HOME/.m2/repository/org/apache/lucene/lucene-queryparser/4.10.0/lucene-queryparser-4.10.0.jar
OPENNLPTOOLS=$HOME/.m2/repository/org/apache/opennlp/opennlp-tools/1.5.3/opennlp-tools-1.5.3.jar
OPENNLPMAXENT=$HOME/.m2/repository/org/apache/opennlp/opennlp-maxent/3.0.3/opennlp-maxent-3.0.3.jar
LOG4JAPI=$HOME/.m2/repository/org/apache/logging/log4j/log4j-api/2.1/log4j-api-2.1.jar
LOG4JCORE=$HOME/.m2/repository/org/apache/logging/log4j/log4j-core/2.1/log4j-core-2.1.jar
BIOC=$HOME/.m2/repository/bioc/bioc/1.0.1/bioc-1.0.1.jar
NLP=$HOME/.m2/repository/gov/nih/nlm/nls/nlp/2.4.C/nlp-2.4.C.jar
CONTEXT=$HOME/.m2/repository/context/context/2012/context-2012.jar

JARSPATH=$ANALYZERS:$CORE:$QUERYPARSER:$OPENNLPTOOLS:$OPENNLPMAXENT:$BIOC:$NLP:$LOG4JAPI:$LOG4JCORE:$CONTEXT

# OPENNLP_MODELS=/usr/local/pub/nlp/opennlp/models
OPENNLP_MODELS=$PROJECTDIR/data/models

JVMOPTS="-Den-sent.bin.path=$OPENNLP_MODELS/en-sent.bin \
    -Den-token.bin.path=$OPENNLP_MODELS/en-token.bin \
    -Den-pos-maxent.bin.path=$OPENNLP_MODELS/en-pos-maxent.bin \
    -Dlog4j.configurationFile=$PROJECTDIR/config/log4j2-debug.xml \
    -Dmetamaplite.property.file=$PROJECTDIR/config/bioc.metamaplite.properties \
    -Dmetamaplite.entitylookup.resultlength=1500"

echo java -cp $PROJECTDIR/target/classes:$JARSPATH $JVMOPTS gov.nih.nlm.nls.metamap.lite.BioCPipeline $* 
java -cp $PROJECTDIR/target/classes:$JARSPATH $JVMOPTS gov.nih.nlm.nls.metamap.lite.BioCPipeline $* 
