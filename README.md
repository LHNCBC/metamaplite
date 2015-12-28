# MetaMap Lite: A lighter named-entity recognizer

## Prerequisites

### For running

* Java 1.7 JRE

### For Development

* Java 1.7 JDK
* Maven or Ant

## Command Line Usage

    ./metamaplite.sh [options] [<input file>|--]

Example of use:

     $ java -cp public_mm_lite/target/metamaplite-2.0-SNAPSHOT.jar \
          gov.nih.nlm.nls.ner.MetaMapLite \
          --indexdir=public_mm_lite/data/ivf/strict \
          --modelsdir=public_mm_lite/data/models \
          --specialtermsfile=public_mm_lite/data/specialterms.txt  <arguments>

Current options are:

  input options:

      --              Read from standard input

  Configuration Options:

    --configfile=<filename>        Use configuration file

Options that can be used to override configuration file or when
configuration file is not present:

    --indexdir=<directory>         location of program's index directory
    --modelsdir=<directory>        location of models for sentence breaker and part-of-speech tagger
	--specialtermsfile=<filename>  location of file of terms to be excluded

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

### Processing properties

    | metamaplite.segment.sentences         | use sentence segmenter (default: true)
    | metamaplite.segment.blanklines        | segment using blankline as separator. (default: false)
    | metamaplite.sourceset                 | use only concepts from listed sources (default: all)
    | metamaplite.semanticgroup             | use only concepts belonging to listed semantic types (default: all)
    | metamaplite.usecontext                | Use ConText for negation detection (default: true)

### Configuration properties

    | metamaplite.excluded.termsfile        | cui/terms pairs that are exclude from results (default: data/specialterms.txt)
    | metamaplite.index.directory           | the directory the indexes resides (sets the following properties)
    | metamaplite.ivf.cuiconceptindex       | cui/concept/preferredname index
    | metamaplite.ivf.cuisourceinfoindex    | cui/sourceinfo index 
    | metamaplite.ivf.cuisemantictypeindex  | cui/semantictype index

    | opennlp.models.directory              | the directory the models resides (sets the following properties. default: data/models)
    | opennlp.en-pos.bin.path               | (default: data/models/en-pos-maxent.bin)
    | opennlp.en-token.bin.path             | (default: data/models/en-token.bin)
    | opennlp.en-sent.bin.path              | (default: data/models/en-sent.bin)

### Command line metamaplite only properties

    | metamaplite.document.inputtype        | document input type (default: freetext)
    | metamaplite.outputextension           | result output file extension (default: .mmi)
    | metamaplite.property.file             | load configuration from file (default: ./config/metamaplite.properties)
    | metamaplite.outputformat              | result output format (default: mmi)



## Using MetaMap from Java

Creating properties for configuring MetaMapLite Instance:
    
    Properties myProperties = new Properties();
    MetaMapLite.expandModelsDir(myProperties,
                   "/home/piro/public_mm_lite/data/models");
    MetaMapLite.expandIndexDir(myProperties,
			       "/home/piro/Projects/public_mm_lite/data/ivf/strict");
    myProperties.setProperty("metamaplite.excluded.termsfile",
			       "/home/piro/Projects/public_mm_lite/data/specialterms.txt");

Creating a metamap lite instance:

    MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);

Creating a document list with one or more documents:

    BioCDocument document = FreeText.instantiateBioCDocument("diabetes");
	document.setId("1");
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    documentList.add(document);

Getting a list of entities for the document list:

    List<Entity> entityList = metaMapLiteInst.processDocumentList(documentList);

Traversing the entity list displaying cui and matching text:

    List<Entity> entityList = metaMapLiteInst.processDocumentList(documentList);
    for (Entity entity: entityList) {
      for (Ev ev: entity.getEvSet()) {
     	System.out.print(ev.getConceptInfo().getCUI() + "|" + entity.getMatchedText());
	    System.out.println();
      }
    }


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

     java -cp target/metamaplite-2.0-SNAPSHOT.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes <mrconsofile> <mrstyfile> <ivfdir>

The resulting indices are in <ivfdir>/indices.  The tables the indexes
are generated from are in <ivfdir>/tables.

To use the new indexes do one of the following:

Use the --indexdir=<directory> option:

    java -cp target/metamaplite-2.0-SNAPSHOT.jar \
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

## Future

Added mechanism to use custom user-supplied segmenters.
