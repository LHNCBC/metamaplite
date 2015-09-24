# MetaMap Lite: A lighter named-entity recognizer

## Prerequisites

* Java 1.7
* Maven or Ant

## Installation

Installation on Unix (Linux/Mac OS/X):

    $ tar xvfj public_mm_lite_{year}.tar.bz2
    $ cd public_mm_lite 
    $ sh install.sh

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
    processing options:
      --restrict_to_sts=<semtype>[,<semtype>,<semtype>...]
      --restrict_to_sources=<source>[,<source>...]
    alternate output options:
      --list_sentences
      --list_acronyms

## Tables and Indexes

Currently, three tables are used:

* cuisourceinfo
* cuisemantictype (cuist)
* cuiconcept

## Adding custom input document formats

New document loader class must conform to BioCDocumentLoader interface.

## Adding custom result output formats

New result formatter class must conform to ResultFormatter interface.

