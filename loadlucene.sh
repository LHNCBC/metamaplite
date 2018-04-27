#!/bin/sh -x
DBROOT=/nfsvol/nls3aux18/DB

STRICT2014AB=$DBROOT/DB.USAbase.2014AB.strict
VARIANTS2014AB=$DBROOT/DB.USAbase.2014AB.strict
# INDEXROOT=/rhome/wjrogers/lucenedb/strict
INDEXROOT=/net/lhcdevfiler/vol/cgsb5/ind/II_Group_WorkArea/wjrogers/lucenedb/strict

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/cuiconcept $STRICT2014AB/cui_concept.txt cui concept

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/cui_st $STRICT2014AB/cui_st.txt cui semtype
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/cui_src $STRICT2014AB/cui_src.txt cui source

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/first_words $STRICT2014AB/first_words.txt word sui cui

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/cui_sourceinfo $STRICT2014AB/cui_sourceinfo.txt cui sui seqno str src tty

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/first_words_of_one $STRICT2014AB/first_words_of_two.txt word sui cui
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/first_words_of_two $STRICT2014AB/first_words_of_two.txt word sui cui

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/sui_cui $STRICT2014AB/sui_cui.txt sui cui
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/sui_nmstr_str $STRICT2014AB/sui_nmstr_str.txt sui nmstr string

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/first_words_of_one_WIDE $STRICT2014AB/first_words_of_one_WIDE.txt word nmstr str concept
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/first_words_of_two_WIDE $STRICT2014AB/first_words_of_two_WIDE.txt word nmstr str concept

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/all_words $STRICT2014AB/all_words.txt word sui cui
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/all_words_WIDE $STRICT2014AB/all_words_WIDE.txt word nmstr str concept

# De-normalized cui_srcs_sts table
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/cui_srcs_sts $STRICT2014AB/cui_srcs_sts.txt cui source semtype

# variant tables
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/vars    $VARIANTS2014AB/vars.txt    word wcat var vcat dist hist roots
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/varsan  $VARIANTS2014AB/varsan.txt  word wcat var vcat dist hist roots
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/varsanu $VARIANTS2014AB/varsanu.txt word wcat var vcat dist hist roots
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $INDEXROOT/varsu   $VARIANTS2014AB/varsu.txt   word wcat var vcat dist hist roots
