::
:: A simplified metamaplite batch script using metamaplite standalone
:: jar, should work in Command Prompt and maybe PowerShell on Windows.

@echo off

set projectdir=%cd%

set MML_VERSION=3.6.2rc8

set OPENNLP_MODELS=$PROJECTDIR/data/models
set CONFIGDIR=$PROJECTDIR/config

set MML_JVM_OPTS=-Xmx12g

set METAMAPLITE=%projectdir%/target/metamaplite-%MML_VERSION%-standalone.jar

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
-Dmetamaplite.index.directory=%projectdir%/data/ivf/2019AB/USAbase ^
-Dmetamaplite.excluded.termsfile=%projectdir%/data/specialterms.txt

java -cp %projectdir%/target/classes;%projectdir%/build/classes;%projectdir%/classes;%JARSPATH%;%projectdir% ^
     %JVMOPTS% gov.nih.nlm.nls.ner.MetaMapLite %* 



