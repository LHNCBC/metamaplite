:: -*-bat-*-
:: Script for generating indexes on low-memory workstations
::
:: usage: create_indices.bat mrconso mrsty mrsat ivfdir
::
@echo off

set MRCONSO=%1%
set MRSTY=%2%
set MRSAT=%3%
set IVFDIR=%4%

set MML_VERSION=3.6.2rc5

set projectdir=%cd%

:: Location of LVG properties file must be defined before running this script:
::
:: For example:
::    set LVG_CONFIG=%INSTALLDIR%\lvg2020\data\config\lvg.properties
::
:: or
::    set LVG_DIR=%INSTALLDIR%\lvg2020
::
:: where INSTALLDIR is the directory where you installed LVG.

:: make directory to place generated tables
mkdir %IVFDIR%\tables

:: Do Table generation first then index generation
::
:: build table %IVFDIR%\tables\cuiconcept.txt
::
java -Xmx2g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrconsoPreferredNames ^
     %MRCONSO% %IVFDIR%\tables\cuiconcept.txt

:: build table %IVFDIR%\tables\cuisourceinfo.txt
::
java -Xmx4g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrconsoSources ^
     %MRCONSO% %IVFDIR%\tables\cuisourceinfo.txt

:: build table %IVFDIR%\tables\cuist.txt
::
java -Xmx2g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrstySemanticTypes ^
     %MRSTY% %IVFDIR%\\tables\cuist.txt

:: build table %IVFDIR%\tables\mesh_tc_relaxed.txt
::
java -Xmx2g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractTreecodes ^
     %MRCONSO% %MRSAT% %IVFDIR%\tables\mesh_tc_relaxed.txt

:: build table %IVFDIR%\tables\vars.txt
::
java -Xmx4g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
     gov.nih.nlm.nls.metamap.dfbuilder.GenerateVariants ^
     %MRCONSO% %IVFDIR%\tables\vars.txt

:: generate ifconfig file in %IVFDIR%

echo cuist.txt^|cuist^|2^|0^|cui^|st^|TXT^|TXT > %IVFDIR%\tables\ifconfig
echo cuisourceinfo.txt^|cuisourceinfo^|6^|0,1,3^|cui^|sui^|i^|str^|src^|tty^|TXT^|TXT^|INT^|TXT^|TXT^|TXT >> %IVFDIR%\tables\ifconfig
echo cuiconcept.txt^|cuiconcept^|2^|0,1^|cui^|concept^|TXT^|TXT >> %IVFDIR%\tables\ifconfig
echo mesh_tc_relaxed.txt^|meshtcrelaxed^|2^|0,1^|mesh^|tc^|TXT^|TXT >> %IVFDIR%\tables\ifconfig
echo vars.txt^|vars^|7^|0,2^|term^|tcat^|word^|wcat^|varlevel^|history^|^|TXT^|TXT^|TXT^|TXT^|TXT^|TXT^|TXT >> %IVFDIR%\tables\ifconfig

:: build index %IVFDIR%\indices\cuiconcept
::
java -Xmx4g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex %IVFDIR% cuiconcept

:: build index %IVFDIR%\indices\cuisourceinfo
java -Xmx7g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex %IVFDIR% cuisourceinfo

:: build index %IVFDIR%\indices\cuist
java -Xmx5g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex %IVFDIR% cuist

:: build index %IVFDIR%\indices\meshtcrelaxed
java -Xmx5g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex %IVFDIR% meshtcrelaxed


:: build index %IVFDIR%\indices\vars
java -Xmx6g -cp %projectdir%\target\metamaplite-%MML_VERSION%-standalone.jar ^
gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex %IVFDIR% vars
