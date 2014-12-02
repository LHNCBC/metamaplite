#!/bin/sh -x
DBACCESSROOT=/nfsvol/nls/specialist/module/db_access
STRICT2014AA=$DBACCESSROOT/data.Base.2014AA/MetaWordIndex/model.strict
VARIANTS2014AA=$DBACCESSROOT/data.Base.2014AA/Variants
DBROOT=/rhome/wjrogers/lucenedb/strict

 time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/cuiconcept $STRICT2014AA/cui_concept.txt cui concept

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/cui_st $STRICT2014AA/cui_st.txt cui semtype
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/cui_src $STRICT2014AA/cui_src.txt cui source

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/first_words $STRICT2014AA/first_words.txt word sui cui

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/cui_sourceinfo $STRICT2014AA/cui_sourceinfo.txt cui sui seqno str src tty

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/first_words_of_one $STRICT2014AA/first_words_of_two.txt word sui cui
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/first_words_of_two $STRICT2014AA/first_words_of_two.txt word sui cui

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/sui_cui $STRICT2014AA/sui_cui.txt sui cui
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/sui_nmstr_str $STRICT2014AA/sui_nmstr_str.txt sui nmstr string

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/first_words_of_one_WIDE $STRICT2014AA/first_words_of_one_WIDE.txt word nmstr str concept
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/first_words_of_two_WIDE $STRICT2014AA/first_words_of_two_WIDE.txt word nmstr str concept

time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/all_words $STRICT2014AA/all_words.txt word sui cui
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/all_words_WIDE $STRICT2014AA/all_words_WIDE.txt word nmstr str concept

# De-normalized cui_srcs_sts table
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/cui_srcs_sts $STRICT2014AA/cui_srcs_sts.txt cui source semtype

# variant tables
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/vars    $VARIANTS2014AA/vars.txt    word wcat var vcat dist hist roots
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/varsan  $VARIANTS2014AA/varsan.txt  word wcat var vcat dist hist roots
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/varsanu $VARIANTS2014AA/varsanu.txt word wcat var vcat dist hist roots
time ./run.sh gov.nih.nlm.nls.metamap.lite.lucene.LoadTable $DBROOT/varsu   $VARIANTS2014AA/varsu.txt   word wcat var vcat dist hist roots
