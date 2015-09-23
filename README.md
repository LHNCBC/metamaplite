# MetaMap Lite: A lighter named-entity recognizer

## Prerequisites

* Java 1.7
* Maven or Ant

## Installation


## modifying configuration file

### inverted files

    metamaplite.ivf.cuiconceptindex: data/ivf/strict/indices/cuiconcept
    metamaplite.ivf.firstwordsofonewideindex: data/ivf/strict/indices/first_words_of_one_WIDE
    metamaplite.ivf.cuisourceinfoindex: data/ivf/strict/indices/cui_sourceinfo
    metamaplite.ivf.cuisemantictypeindex: data/ivf/strict/indices/cui_st
    metamaplite.ivf.varsindex: data/ivf/strict/indices/vars

### OpenNLP model files 

    opennlp.en-sent.bin.path: data/models/en-sent.bin
    opennlp.en-token.bin.path: data/models/en-token.bin
    opennlp.en-pos-maxent.bin.path: data/models/en-pos-maxent.bin

## Usage

    ./metamaplite.sh [options] <input file>

Current options are:

document processing options:
  --freetext      Text with no markup.
  --ncbicorpus    NCBI Disease Corpus: tab separated fields: id \t title \t abstract
  --chemdner      CHEMDNER document: tab separated fields: id \t title \t abstract
  --chemdnersldi  CHEMDNER document: id with pipe followed by tab separated fields: id | title \t abstract
output options:
  --bioc|cdi|bc|bc-evaluate   output compatible with evaluation program bc-evaluate
  --mmilike|mmi               similar to MetaMap Fielded MMI output
  --brat                      BRAT annotation format

## Tables and Indexes

Currently, three tables are used:

* cuisourceinfo
* cuisemantictype
* cuiconcept

## Adding custom input document formats

New document loader class must conform to BioCDocumentLoader interface.

## Adding custom result output formats

New result formatter class must conform to ResultFormatter interface.

