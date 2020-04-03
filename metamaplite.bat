::
echo off

set projectdir=%cd%

set MML_VERSION=3.6.2rc5

set OPENNLP_MODELS=$PROJECTDIR/data/models
set CONFIGDIR=$PROJECTDIR/config

set MML_JVM_OPTS=-Xmx12g

set METAMAPLITE=%projectdir%/target/metamaplite-%MML_VERSION%%-standalone.jar

set JARSPATH=%METAMAPLITE%

set OPENNLP_MODELS=%projectdir%/data/models
set CONFIGDIR=%projectdir%/config

set JVMOPTS=-Den-sent.bin.path=%OPENNLP_MODELS%/en-sent.bin ^
-Den-token.bin.path=%OPENNLP_MODELS%/en-token.bin ^
-Den-pos-maxent.bin.path=%OPENNLP_MODELS%/en-pos-maxent.bin ^
-Dopennlp.en-chunker.bin.path=%OPENNLP_MODELS%/en-chunker.bin ^
-Dlog4j.configurationFile=file:///%projectdir%/config/log4j2.xml ^
-Dmetamaplite.property.file=%CONFIGDIR%/metamaplite.properties ^
-Dmetamaplite.entitylookup.resultlength=1500 ^
-Dmetamaplite.ivf.cuiconceptindex=%projectdir%/data/ivf/2018AA/USAbase/strict/cuiconcept ^
-Dmetamaplite.ivf.firstwordsofonewideindex=%projectdir%/data/ivf/2018AA/USAbase/strict/first_words_of_one_WIDE ^
-Dmetamaplite.ivf.cuisourceinfoindex=%projectdir%/data/ivf/2018AA/USAbase/strict/cuisourceinfo ^
-Dmetamaplite.ivf.cuisemantictypeindex=%projectdir%/data/ivf/2018AA/USAbase/strict/cuist ^
-Dmetamaplite.ivf.varsindex=%projectdir%/data/ivf/2018AA/USAbase/strict/vars ^
-Dmetamaplite.ivf.meshtcrelaxedindex=$PROJECTDIR/data/ivf/2018AA/USAbase/strict/indices/meshtcrelaxed

java -cp %projectdir%/target/classes;%projectdir%/build/classes;%projectdir%/classes;%JARSPATH%;%CONFIGDIR% ^
     %JVMOPTS% gov.nih.nlm.nls.ner.MetaMapLite %* 

