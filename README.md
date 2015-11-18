# MetaMap Lite: A lighter named-entity recognizer

## Prerequisites

* Java 1.7
* Maven or Ant

## Using MetaMapLite without installing

    $ java -cp public_mm_lite/target/metamaplite-1.0-SNAPSHOT.jar gov.nih.nlm.nls.ner.MetaMapLite [options]

Configuration Options:

    --configfile=<filename>        Use configuration file

Options that can be used to override configuration file or when
configuration file is not present:

    --indexdir=<directory>         location of program's index directory
    --modelsdir=<directory>        location of models for sentence breaker and part-of-speech tagger
	--specialtermsfile=<filename>  location of file of terms to be excluded

Example of use:

     $ java -cp public_mm_lite/target/metamaplite-1.0-SNAPSHOT.jar \
          gov.nih.nlm.nls.ner.MetaMapLite \
          --indexdir=public_mm_lite/data/ivf/strict \
          --modelsdir=public_mm_lite/data/models \
          --specialtermsfile=public_mm_lite/data/specialterms.txt

## Installation

Installation on Unix (Linux/Mac OS/X):

    $ tar xvfj public_mm_lite_{year}.tar.bz2
    $ cd public_mm_lite 
    $ sh install.sh

## Usage

    ./metamaplite.sh [options] [<input file>|--]

Current options are:

  input options:

      --              Read from standard input

  document processing options:

      --freetext      Text with no markup.
      --ncbicorpus    NCBI Disease Corpus: tab separated fields: id \t title \t abstract
      --chemdner      CHEMDNER document: tab separated fields: id \t title \t abstract
      --chemdnersldi  CHEMDNER document: id with pipe followed by tab separated fields: id | title \t abstract
	  --inputformat=<loadername>

  output options:

      --bioc|cdi|bc|bc-evaluate   output compatible with evaluation program bc-evaluate
      --mmilike|mmi               similar to MetaMap Fielded MMI output
      --brat                      BRAT annotation format

  processing options:

      --restrict_to_sts=<semtype>[,<semtype>,<semtype>...]
      --restrict_to_sources=<source>[,<source>...]
	  --segment_sentences=[<true>|<false>]
      --segment_blanklines=[<true>|<false>]
	  --usecontext                Use ConText Negation Detector

  alternate output options:

      --list_sentences
      --list_acronyms

## Properties

    metamaplite.index.directory: data/ivf/strict
    metamaplite.ivf.cuiconceptindex: data/ivf/strict/indices/cuiconcept
    metamaplite.ivf.firstwordsofonewideindex: data/ivf/strict/indices/first\_words\_of\_one_WIDE
    metamaplite.ivf.cuisourceinfoindex: data/ivf/strict/indices/cuisourceinfo
    metamaplite.ivf.cuisemantictypeindex: data/ivf/strict/indices/cuist
    metamaplite.ivf.varsindex: data/ivf/strict/indices/vars

# irutils indexes



## Tables and Indexes

Currently, three tables are used:

* cuisourceinfo
* cuisemantictype (cuist)
* cuiconcept

### Generating the indexes from the tables

The CreateIndexes class generates tables cuiconcept, cuisourceinfo,
and cuist from MRCONSO.RRF and MRSTY.RRF and then produces
corresponding indexes for tables.

Usage: 

     java -cp target/metamaplite-1.0-SNAPSHOT.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes <mrconsofile> <mrstyfile> <ivfdir>

The resulting indices are in <ivfdir>/indices.  The tables the indexes
are generated from are in <ivfdir>/tables.

To use the new indexes do one of the following:

Use the --indexdir=<directory> option:

    java -cp target/metamaplite-1.0-SNAPSHOT.jar \
     gov.nih.nlm.nls.metamap.ner.MetaMapLite --indexdir=<ivfdir> <other-options> <otherargs>

Modify the configuration file config/metamap.properties:

    metamaplite.ivf.cuiconceptindex: data/multi-key-index-test/indices/cuiconcept
    mmetamaplite.ivf.cuisourceinfoindex: data/multi-key-index-test/indices/cuisourceinfo
    metamaplite.ivf.cuisemantictypeindex: data/multi-key-index-test/indices/cuist


## Adding custom input document formats

New document loader class must conform to BioCDocumentLoader
interface.  One can add a document loader class in MetaMapLite's
classpath to MetaLite's list of document loaders by adding it to the
properties using System properties or modifying MetaMapLite's
configuration file:

Set as system property:

    -Dbioc.document.loader.<name>=<fully-specified class name>

For example creating a loader with the name "qadocument":

    -Dbioc.document.loader.qadocument=gov.nih.nlm.nls.metamap.document.QAKeyValueDocument

Or add it to config/metamaplite.properties:

    bioc.document.loader.qadocument: gov.nih.nlm.nls.metamap.document.QAKeyValueDocument

## Adding custom result output formats

New result formatter class must conform to ResultFormatter interface.
One can add the result formatter to MetaMapLite by adding its class
file to MetaMapLite's classpath and then adding a reference to it as a
property:

Set as system property:

    -Dmetamaplite.result.formatter.<name>=<fully-specified class name>

For example creating a formatter with the name "brat":

    -Dmetamaplite.result.formatter.brat=gov.nih.nlm.nls.metamap.lite.resultformats.Brat

Or add it to config/metamaplite.properties:

    metamaplite.result.formatter.brat: gov.nih.nlm.nls.metamap.lite.resultformats.Brat
