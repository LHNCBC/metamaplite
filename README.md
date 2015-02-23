# MetaMap Lite: A lighter named-entity recognizer

## Prerequisites

* Java 1.7
* Maven or Ant

## Installation


You'll need the model file for the sentence extractor "en-sent.bin"
which can be downloaded from the opennlp project at
http://opennlp.sourceforge.net/models-1.5

Set the system property "en-sent.bin.path":

    en-sent.bin.path=<location of en-sent.bin>

Modify metamaplite.sh to set the location of the model file and then
run it.

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
Other options:
  --luceneresultlen           set length of result sets returned from Lucene


## Tables and Lucene Indexes

Currently, three tables are used:

* cuisourceinfo
* cuisemantictype
* cuiconcept

## Adding custom input document formats

New document loader class must conform to BioCDocumentLoader interface.

## Adding custom result output formats

New result formatter class must conform to ResultFormatter interface.

