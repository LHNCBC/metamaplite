e#
set projectdir=%cd%

set BIOC=%projectdir%/lib/bioc-1.0.1.jar
set LOG4JAPI=%projectdir%/lib/log4j-api-2.1.jar
set ANALYZERS=%projectdir%/lib/lucene-analyzers-common-4.10.0.jar
set QUERYIES=%projectdir%/lib/lucene-queries-4.10.0.jar
set NLP=%projectdir%/lib/nlp-2.4.C.jar
set OPENNLPTOOLS=%projectdir%/lib/opennlp-tools-1.5.3.jar
set CONTEXT=%projectdir%/lib/context-2012.jar
set LOG4JCORE=%projectdir%/lib/log4j-core-2.1.jar
set CORE=%projectdir%/lib/lucene-core-4.10.0.jar
set QUERYPARSER=%projectdir%/lib/lucene-queryparser-4.10.0.jar
set OPENNLPMAXENT=%projectdir%/lib/opennlp-maxent-3.0.3.jar
set OPENCSV=%projectdir%/lib/opencsv-2.3.jar
set IRUTILS=%projectdir%/lib/irutils-2.0-SNAPSHOT.jar
set METAMAPLITE=%projectdir%/target/metamaplite-3.6.1p2.jar

set JARSPATH=%ANALYZERS%;%CORE%;%QUERYPARSER%;%OPENNLPTOOLS%;%OPENNLPMAXENT%;%BIOC%;%NLP%;%LOG4JAPI%;%LOG4JCORE%;%CONTEXT%;%OPENCSV%;%IRUTILS%;%METAMAPLITE%

set OPENNLP_MODELS=%projectdir%/data/models
set CONFIGDIR=%projectdir%/config

set JVMOPTS=-Den-sent.bin.path=%OPENNLP_MODELS%/en-sent.bin -Den-token.bin.path=%OPENNLP_MODELS%/en-token.bin -Den-pos-maxent.bin.path=%OPENNLP_MODELS%/en-pos-maxent.bin -Dopennlp.en-chunker.bin.path=%OPENNLP_MODELS%/en-chunker.bin -Dlog4j.configurationFile=file://%projectdir%/config/log4j2.xml -Dmetamaplite.property.file=%CONFIGDIR%/metamaplite.properties -Dmetamaplite.entitylookup.resultlength=1500 -Dmetamaplite.ivf.cuiconceptindex=%projectdir%/data/ivf/2017AA/USAbase/strict/cuiconcept -Dmetamaplite.ivf.firstwordsofonewideindex=%projectdir%/data/ivf/2017AA/USAbase/strict/first_words_of_one_WIDE -Dmetamaplite.ivf.cuisourceinfoindex=%projectdir%/data/ivf/2017AA/USAbase/strict/cuisourceinfo -Dmetamaplite.ivf.cuisemantictypeindex=%projectdir%/data/ivf/2017AA/USAbase/strict/cuist -Dmetamaplite.ivf.varsindex=%projectdir%/data/ivf/2017AA/USAbase/strict/vars -Dmetamaplite.ivf.meshtcrelaxedindex=$PROJECTDIR/data/ivf/2017AA/USAbase/strict/indices/meshtcrelaxed

java -cp %projectdir%/target/classes;%projectdir%/build/classes;%projectdir%/classes;%JARSPATH%;%CONFIGDIR% %JVMOPTS% gov.nih.nlm.nls.ner.MetaMapLite %* 

