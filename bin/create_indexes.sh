#!/bin/sh
#
# Script for generating indexes on low-memory workstations
#
# usage: create_indices.bat mrconso mrsty mrsat ivfdir
#
if [ $# -lt 4 ]; then
    echo "usage: $0 mrconso mrsty mrsat ivfdir"
    exit 0
fi

MRCONSO=$1
MRSTY=$2
MRSAT=$3
IVFDIR=$4

if [ ! -f $MRCONSO ]; then
    echo "MRCONSO file $MRCONSO not found! aborting!"
    exit 1
fi

if [ ! -f $MRSTY ]; then
    echo "MRSTY file $MRSTY not found! aborting!"
    exit 1
fi

if [ ! -f $MRSAT ]; then
    echo "MRSAT file $MRSAT not found! aborting!"
    exit 1
fi

echo "MRCONSO file: $MRCONSO"
echo "MRSTY file: $MRSTY"
echo "MRSAT file: $MRSAT"
echo "Inverted file database directory: $IVFDIR"
    
MML_VERSION=3.6.2rc5

# IMPORTANT NOTE: Location of LVG properties file must be defined
# before running this script:
#
# For example:
# export LVGCONFIG=$TOOLS/lvg2020/data/config/lvg.properties

PROJECTDIR=$(dirname $0)/..

# make directory to place generated tables
mkdir -p $IVFDIR/tables

# Do Table generation first then index generation
#
# build table $IVFDIR/tables/cuiconcept.txt
#
java -Xmx2g \
     -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrconsoPreferredNames \
     $MRCONSO $IVFDIR/tables/cuiconcept.txt

# build table $IVFDIR/tables/cuisourceinfo.txt
#
java -Xmx4g \
     -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrconsoSources \
     $MRCONSO $IVFDIR/tables/cuisourceinfo.txt

# build table $IVFDIR/tables/cuist.txt
#
java -Xmx2g \
      -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrstySemanticTypes \
      $MRSTY $IVFDIR//tables/cuist.txt

# build table $IVFDIR/tables/mesh_tc_relaxed.txt
#
java -Xmx2g \
     -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
     gov.nih.nlm.nls.metamap.dfbuilder.ExtractTreecodes \
     $MRCONSO $MRSAT $IVFDIR/tables/mesh_tc_relaxed.txt

# build table $IVFDIR/tables/vars.txt
#
java -Xmx4g \
     -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
     -Dgv.lvg.config.file=$LVGCONFIG \
     gov.nih.nlm.nls.metamap.dfbuilder.GenerateVariants \
     $MRCONSO $IVFDIR/tables/vars.txt

# generate ifconfig file in $IVFDIR

if [ ! -e $IVFDIR/tables/ifconfig ]; then
    echo "cuist.txt|cuist|2|0|cui|st|TXT|TXT" > $IVFDIR/tables/ifconfig
    echo "cuisourceinfo.txt|cuisourceinfo|6|0,1,3|cui|sui|i|str|src|tty|TXT|TXT|INT|TXT|TXT|TXT" >> $IVFDIR/tables/ifconfig
    echo "cuiconcept.txt|cuiconcept|2|0,1|cui|concept|TXT|TXT" >> $IVFDIR/tables/ifconfig
    echo "mesh_tc_relaxed.txt|meshtcrelaxed|2|0,1|mesh|tc|TXT|TXT" >> $IVFDIR/tables/ifconfig
    echo "vars.txt|vars|7|0,2|term|tcat|word|wcat|varlevel|history||TXT|TXT|TXT|TXT|TXT|TXT|TXT" >> $IVFDIR/tables/ifconfig
fi

# build index $IVFDIR/indices/cuiconcept
#
java -Xmx6g \
      -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex \
      $IVFDIR cuiconcept

# build index $IVFDIR/indices/cuisourceinfo
java -Xmx10g \
      -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex \
      $IVFDIR cuisourceinfo

# build index $IVFDIR/indices/cuist
java -Xmx7g \
      -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex \
      $IVFDIR cuist

# build index $IVFDIR/indices/meshtcrelaxed
java -Xmx5g \
      -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex \
      $IVFDIR meshtcrelaxed


# build index $IVFDIR/indices/vars
java -Xmx6g \
      -cp $PROJECTDIR/target/metamaplite-${MML_VERSION}-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.BuildIndex \
      $IVFDIR vars
