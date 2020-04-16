#!/bin/sh

PROJECTDIR=$PWD

PROJECTDIR=$(dirname $0)

MML_VERSION=3.6.2rc5

BIOC=$PROJECTDIR/lib/bioc-1.0.1.jar
LOG4JAPI=$PROJECTDIR/lib/log4j-api-2.1.jar
ANALYZERS=$PROJECTDIR/lib/lucene-analyzers-common-4.10.0.jar
QUERYIES=$PROJECTDIR/lib/lucene-queries-4.10.0.jar
NLP=$PROJECTDIR/lib/nlp-2.4.C.jar
OPENNLPTOOLS=$PROJECTDIR/lib/opennlp-tools-1.5.3.jar
CONTEXT=$PROJECTDIR/lib/context-2012.jar
LOG4JCORE=$PROJECTDIR/lib/log4j-core-2.1.jar
CORE=$PROJECTDIR/lib/lucene-core-4.10.0.jar
QUERYPARSER=$PROJECTDIR/lib/lucene-queryparser-4.10.0.jar
OPENNLPMAXENT=$PROJECTDIR/lib/opennlp-maxent-3.0.3.jar
OPENCSV=$PROJECTDIR/lib/opencsv-2.3.jar
IRUTILS=$PROJECTDIR/lib/irutils-2.0-SNAPSHOT.jar
STRINGSIM=$HOME/.m2/repository/info/debatty/java-string-similarity/0.23/java-string-similarity-0.23.jar
METAMAPLITE=$PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar
JARSPATH=$ANALYZERS:$CORE:$QUERYPARSER:$OPENNLPTOOLS:$OPENNLPMAXENT:$BIOC:$NLP:$LOG4JAPI:$LOG4JCORE:$CONTEXT:$OPENCSV:$IRUTILS:$STRINGSIM:$METAMAPLITE

# OPENNLP_MODELS=/usr/local/pub/nlp/opennlp/models
OPENNLP_MODELS=$PROJECTDIR/data/models

# metamaplite properties
MML_OPTS="-Dopennlp.en-sent.bin.path=$OPENNLP_MODELS/en-sent.bin \
    -Dopennlp.en-token.bin.path=$OPENNLP_MODELS/en-token.bin \
    -Dopennlp.en-pos.bin.path=$OPENNLP_MODELS/en-pos-perceptron.bin \
    -Dlog4j.configurationFile=$PROJECTDIR/config/log4j2.xml \
    -Dmetamaplite.entitylookup.resultlength=1500 \
    -Dmetamaplite.index.directory=$PROJECTDIR/data/ivf/2017AA/USAbase/strict
    -Dmetamaplite.ivf.cuiconceptindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/cuiconcept \
    -Dmetamaplite.ivf.firstwordsofonewideindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/first_words_of_one_WIDE \
    -Dmetamaplite.ivf.cuisourceinfoindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/cuisourceinfo \
    -Dmetamaplite.ivf.cuisemantictypeindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/cuist \
    -Dmetamaplite.ivf.varsindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/vars \
    -Dmetamaplite.ivf.meshtcrelaxedindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/meshtcrelaxed \
    -Dmetamaplite.excluded.termsfile=$PROJECTDIR/data/specialterms.txt"

java $MML_JVM_OPTS -cp $PROJECTDIR/target/classes:$PROJECTDIR/build/classes:$PROJECTDIR/classes:$JARSPATH:$CONFIGDIR $MML_OPTS $*



