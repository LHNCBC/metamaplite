# MetaMap Lite

## System Description

### Processing stages

MetaMapLite segments text using sentence or blank line markers using
OpenNLP's sentence segmenter or MetaMapLite's blank line seqmenter.
Tokenizes sentences using tokenizer based of Original MetaMap's
tokenization regime.  Part-of-speech information is added to tokenized
text using OpenNLP's part-of-speech tagger.  Mapping is done by
dividing sentence or line based chunks into sublists and then each
sublist is normalized and then looked up in a dictionary.  Any match
found in the dictionary that is subsumed by a longer match is
discarded.

MetaMapLite processing stages

    input text ->
      sentence/line segmentation ->
        tokenization ->
          part-of-speech tagging ->
            token window generation ->
              term normalization ->
                concept dictionary lookup ->
                  negation detection ->
				  result presentation

Below is an example of sentence level named entity recognition in
which a token list for the sentence "Papillary Thyroid Carcinoma is a
Unique Clinical Entity." is processed.

Given the example:

        "Papillary Thyroid Carcinoma is a Unique Clinical Entity."
      
The token-based sublists are produced as follows:

        "Papillary Thyroid Carcinoma is a Unique Clinical Entity"
        "Papillary Thyroid Carcinoma is a Unique Clinical"
        "Papillary Thyroid Carcinoma is a Unique"
        "Papillary Thyroid Carcinoma is a"
        "Papillary Thyroid Carcinoma is"
        "Papillary Thyroid Carcinoma"   --> match
                                    "is a Unique Clinical Entity"
                                    "is a Unique Clinical"
                                    "is a Unique"
	                                "is a"
		                            "is"
	                                   "a Unique Clinical Entity"
									   "a Unique Clinical"
									   "a Unique"
                                       "a"
								         "Unique Clinical Entity"
									     "Unique Clinical"
									     "Unique" --> match
									            "Clinical Entity"
									            "Clinical" --> match
                                                "Entity" --> match

Four entities are found by MetaMapLite:

       "Papillary Thyroid Carcinoma"
       "Unique"
	   "Clinical"
	   "Entity"

### Term Normalization

MetaMapLite normalizes the terms in the current token window using a
Java implementation of of MetaMap's Prolog predicate
`mwi_utilities:normalize_meta_string/2` (used by `filter_mrconso`)
with some slight modifications (including the removal of some
operations.)

The following operations are performed of the term before dictionary
lookup:

   * removal of (left []) parentheticals;
   * syntactic uninversion;
   * conversion to lowercase;
   * stripping of possessives.

### Dictionary Lookup

The underlying dictionary lookup uses a inverted file implemenations
where the dictionary is divided in to several partitions where each
partition contains only terms which have the same termlength.  The
implementation uses the Java NIO class java.nio.MappedByteBuffer to
access the systems virtual memory facilities to improve I/O
performance.

### Negation Detection

Wendy Chapman's ConText is used for negation detection using the
current sentence and the list of found entities.

### Dictionaries

MetaMapLite currently uses three dictionaries originally created for MetaMap:

    cuiconcept
    cuisourceinfo
    cuist

#### cuisourceinfo

This is the table used for mapping strings in the input text to
concepts, this is the primary dictionary used by MetaMapLite.

Each record contains the UMLS concept identifier, UMLS string
identifier, a sequence number, the source derived string, the source
abbreviation, and the source term type:

    cui|sui|seqno|string|src|tty

Sample records are below:

    C0000005|S0007492|1|(131)I-Macroaggregated Albumin|MSH|PEN
    C0000005|S0007491|2|(131)I-MAA|MSH|EN
    C0000039|S0007564|1|1,2-Dipalmitoylphosphatidylcholine|MSH|MH
    C0000039|S0007564|2|1,2-Dipalmitoylphosphatidylcholine|NDFRT|PT
    C0000039|S0007564|3|1,2-Dipalmitoylphosphatidylcholine|MTH|PN
    C0000039|S1357296|4|1,2 Dipalmitoylphosphatidylcholine|MSH|PM
    C0000039|S0007560|5|1,2-Dihexadecyl-sn-Glycerophosphocholine|NDFRT|SY
    C0000039|S0007560|6|1,2-Dihexadecyl-sn-Glycerophosphocholine|MSH|EN
    C0000039|S1357276|7|1,2 Dihexadecyl sn Glycerophosphocholine|MSH|PM
    C0000039|S0007563|8|1,2-Dipalmitoyl-Glycerophosphocholine|NDFRT|SY


#### cuiconcept

This table maps concept identifiers to concept preferred names.

format:

    cui|preferred name

Some example records are below:

    C0000005|(131)I-Macroaggregated Albumin
    C0000039|1,2-Dipalmitoylphosphatidylcholine
    C0000052|1,4-alpha-Glucan Branching Enzyme
    C0000074|1-Alkyl-2-Acylphosphatidates
    C0000084|1-Carboxyglutamic Acid
    C0000096|1-Methyl-3-isobutylxanthine
    C0000097|1-Methyl-4-phenyl-1,2,3,6-tetrahydropyridine
    C0000098|1-Methyl-4-phenylpyridinium
    C0000102|1-Naphthylamine
    C0000103|1-Naphthylisothiocyanate

#### cuist

This table maps concept identifiers to semantic types.

Each record contains the UMLS concept identifier and a semantic type abbreviation:

    cui|semantic type abbreviation

Usually, there are several records for each UMLS concept:

    C0000005|aapp
    C0000005|irda
    C0000005|phsu
    C0000039|orch
    C0000039|phsu
    C0000052|aapp
    C0000052|enzy
    C0000074|orch
    C0000084|aapp
    C0000084|bacs

## Dictionary and Postings File Organization

The dictionary is constructed as several partitions, each partition
consists of three files for each set of terms of a specific length
from a specific column of the originating table: a dictionary
characteristics file, a term-dictionary file, and a postings extents
file.  These files refer to a global postings file used by all of the
partitions.  Each term-dictionary contains records of term with the
same length with the number of postings for the term and the address

Within the postings extents file
(*indexname*-*column*-*termlength*-partition-offsets) each record
contains address of posting with the length of the posting.

Each partition has four files, the file
*cuisourceinfo-3-30-term-dictionary-stats.txt* contains term and record
lengths along with the number of records in the partition:

    termlength|30
    reclength|46
    datalength|16
    recordnum|76361

The file *cuisourceinfo-3-30-term-dictionary* is a binary file of records,
all the same length with each record containing a term, the number of
postings for that term and address of the posting extents for that
term:

    |            term              | # of postings | address |
    +------------------------------+---------------+---------+
    |Dipalmitoylphosphatidylcholine|       4       | FFF4556 | 


The file *cuisourceinfo-3-30-partition-offsets* is a binary file that
contains the start and end offsets (the extent) of each posting:

    | address | start |  len  |
    +---------+-------+-------+
    | FFF4556 |   58  |   57  |
    |   ...   |  176  |   66  |
    |   ...   |  279  |   59  |
    |   ...   |    0  |   58  |

The postings for all of the partitions reside in the file *postings*.

    address | data 
    --------+-------------------------------------------------------------------
          0 | C0000039|S0033298|4|Dipalmitoylphosphatidylcholine|SNMI|PT
         58 | C0000039|S0033298|7|Dipalmitoylphosphatidylcholine|LNC|CN
        176 | C0000039|S0033298|6|Dipalmitoylphosphatidylcholine|SNOMEDCT_US|OAP
        279 | C0000039|S0033298|5|Dipalmitoylphosphatidylcholine|NDFRT|SY


The postings file is shared among all of the partitions.


## Feature Requests

1. add support for exclusion of some cui/term combination (see MetaMap's special terms file) [done?]
2. support entity lookup using two separate dataset (UMLS and custom, etc.) 

## Known bugs

1. Semantic Type filter does not always filter out all excluded types
2. Abbreviation detector doesn't always work

## Packaging

See file dist_metamap_lite_2015.tcl in repository:

    ${II_WORKAREA}/II_source_repositories/packagingv2.git.

### Required files

1. Java class and supporting jars
2. POM and ANT files
3. Open NLP sentence model files
4. Inverted Multi-key index files
5. Local libraries which are not available through Maven.

### Organization

     metamaplite -+- src 
                  +- config
                  +- data -+- models
                  |        +- ivf -+- strict -+- indices -+- cuisourceinfo
                  |                                       +- cuiconcept
				  |                                       +- cuist
                  +- logs
                  +- scripts
                  +- target

## Possible implementations

### IRUtils multi-key index

Multi-key Inverted File index is now the default implementation.

current tables:

    cuiconcept
    cuisourceinfo
    cuist

### Generating Tables

#### cuiconcept

format:

cui|preferred name

example records:

    C0000005|(131)I-Macroaggregated Albumin
    C0000039|1,2-Dipalmitoylphosphatidylcholine
    C0000052|1,4-alpha-Glucan Branching Enzyme
    C0000074|1-Alkyl-2-Acylphosphatidates
    C0000084|1-Carboxyglutamic Acid

program: 

    MRCONSO.RFF -> ExtractMrconsoPreferredNames -> cuiconcept

example of running program:

    $ java -cp $CLASSPATH \
    gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrconsoPreferredNames \
	data/ivf/2015AA/mrconso.eng \
	data/ivf/2015AA/strict/tables/cuiconcept.txt

#### cuisourceinfo

format:

    cui|sui|seqno|string|src|tty

example records:

    C0000005|S0007492|1|(131)I-Macroaggregated Albumin|MSH|PEN
    C0000005|S0007491|2|(131)I-MAA|MSH|EN
    C0000039|S0007564|1|1,2-Dipalmitoylphosphatidylcholine|MSH|MH
    C0000039|S0007564|2|1,2-Dipalmitoylphosphatidylcholine|NDFRT|PT
    C0000039|S0007564|3|1,2-Dipalmitoylphosphatidylcholine|MTH|PN

program: gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrconsoSources

    MRCONSO.RRF -> ExtractMrconsoSources -> cuisourceinfo

example of running program:

    $ java -cp $CLASSPATH \
	gov.nih.nlm.nls.mmtx.dfbuilder.ExtractMrconsoSources \
	data/ivf/2015AA/mrconso.eng \
	data/ivf/2015AA/cuisourceinfo.txt

#### cuist

format:

    cui|semantic type abbreviation

example records:

    C0000005|aapp
    C0000005|irda
    C0000005|phsu
    C0000039|orch
    C0000039|phsu

program: gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrstySemanticTypes

    MRSTY.RRF -> ExtractMrstySemanticTypes -> cuist

example of running program:

    $ java -cp $CLASSPATH \
	gov.nih.nlm.nls.metamap.dfbuilder.ExtractMrstySemanticTypes \
	data/ivf/2015AA/mrsty.rrf \
	data/ivf/2015AA/strict/tables/cuist.txt

### Building the inverted file indexes

The CreateIndexes class generates tables cuiconcept, cuisourceinfo,
and cuist from MRCONSO.RRF and MRSTY.RRF and then produces
corresponding indexes for tables.

Usage: 

     java -cp target/metamaplite-1.0-SNAPSHOT.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes <mrconsofile> <mrstyfile> <ivfdir>


### Trie based

Trie based NER using a lexically enhanced trie dictionary.

### Lucene index based

Tables represented by Lucene indices. 

## Optimizations

Cache references to entities in Map to avoid unnecessary lookups, this
is in current implementation.

## Input Formats

### Current

+ FreeText
+ Single Line Input (SLI)
+ Single Line Delimited Input (SLDI)
+ ChemDNER
+ ChemDNERSLDI
+ NCBICorpus

### Future

+ Medline 
+ XML (Medline, user-specified)
+ JSON 
+ EDN

# Pipeline

# Pipeline Definition

# Pipeline Protocols

object based

# Pipeline Wrapper Modules


# Plugins

Should use OSGi or JPF (Java Plugin Framework)  

## chunkers

## full parsers

## dictionary lookup

## normalization

## filtering for other 


# outputs


# MRCONSO file 

    /nfsvol/nls/specialist/module/metawordindex/data.Base.2014AA/mrconso.suppressed

5365358

# Evaluation

Similarity Measures

# Configuration

## Sample properties file

    # Lucene indexes
    metamaplite.cuiconceptindex: /nfsvol/nlsaux15/lucenedb/strict/cuiconcept
    metamaplite.firstwordsofonewideindex: /nfsvol/nlsaux15/lucenedb/strict/first_words_of_one_WIDE
    metamaplite.cuisourceinfoindex: /nfsvol/nlsaux15/lucenedb/strict/cui_sourceinfo
    metamaplite.cuisemantictypeindex: /nfsvol/nlsaux15/lucenedb/strict/cui_st
    metamaplite.varsindex: /nfsvol/nlsaux15/lucenedb/strict/vars
    #
    # OpenNLP model files
    opennlp.en-sent.bin.path:/usr/local/pub/nlp/opennlp/models/en-sent.bin
    opennlp.en-token.bin.path:/usr/local/pub/nlp/opennlp/models/en-token.bin
    opennlp.en-pos-maxent.bin.path:/usr/local/pub/nlp/opennlp/models/en-pos-maxent.bin
    

# Entities

Essentially a span within body of text.

Annotations are associated with the span.

typical annotations
   Concept Id with associated semantic types and source references


# Generating datafiles

## Generating cuisourceinfoindex (cui_sourceinfo)

Use extract\_mrconso\_sources.perl:

    http://indlx1.nlm.nih.gov:8000/cgi-bin/cgit.cgi/public_mm/tree/bin/extract_mrconso_sources.perl

## Generating cuisemantictypeindex (cui_st)




## Timing

NCBI Disease Training Corpus (592 documents)

### MetaMapLite

#### indlx1

First run (indlx1):

    real	29m12.477s
    user	39m51.089s
    sys		1m7.615s

Second run (indlx1):

    real	26m10.628s
    user	39m24.213s
    sys		0m37.145s

#### ii-server6

First run (ii-server6):

    real	44m47.399s
    user	52m13.420s
    sys		1m23.783s

Secord run  (ii-server6):

    real	39m53.229s
    user	47m53.044s
    sys		2m38.590s


### MetaMap

First run (ii-server6)

    start time: Wed Mar 11 08:33:52 EDT 2015
    end time: Wed Mar 11 09:20:22 EDT 2015

    47 minutes

Second run (ii-server6):

    start time: Wed Mar 11 09:23:50 EDT 2015
    end time: Wed Mar 11 10:05:18 EDT 2015
    2149.859u 230.497s 41:28.33 95.6%	0+0k 0+7760io 0pf+0w

42 minutes
