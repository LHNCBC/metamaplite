# MetaMapLite: A lighter named-entity recognizer

The primary goal of MetaMapLite to provide a near real-time
named-entity recognizer which is not a rigorous as MetaMap but much
faster while allowing users to customize and augment its behavior for
specific purposes.

It uses some of the tables used by MetaMap but all lexical variants
used in the table are pre-processed.  Named Entities are found using
longest match.  Restriction by UMLS source and Semantic type is
optional.  Part-of-speech tagging which improves precision by a small
amount (at the cost of speed) is also optional.  Negation detection is
available using either Wendy Chapman's context or a native negation
detection algorithm based on Wendy Chapman's NegEx which is somewhat
less effective, but faster.


It has:

+ longest match based entity detection
+ Negation Detection (either ConTexT or negation function based on Wendy Chapman's NegEx)
+ Restriction by UMLS source and semantic type
+ Part of Speech tagging (optional)
+ Abbreviation detection using Lynette Hirschman's algorithm.

What is missing:

+ No detection of disjoint entities
+ No Scoring
+ No derivational variants
+ No word sense disambiguation (to be added later)
+ No overmatching
+ No term processing
+ No dynamic variant generation



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

    --                             Read from standard input
    --pipe                         Read from standard input

  Configuration Options:

    --configfile=<filename>        Use configuration file
    --set_property=name=value      set property "name" to value

Options that can be used to override configuration file or when
configuration file is not present:

    --indexdir=<directory>         location of program's index directory
    --modelsdir=<directory>        location of models for sentence breaker and part-of-speech tagger
	--specialtermsfile=<filename>  location of file of terms to be excluded

  document processing options:

    --freetext      Text with no markup.
    --ncbicorpus    NCBI Disease Corpus: tab separated fields: id \t title \t abstract
    --chemdner      CHEMDNER document: tab separated fields: id \t title \t abstract
    --chemdnersldi  CHEMDNER document: id with pipe followed by tab separated fields: id |t title \t abstract
	--inputformat=<loadername>
	                Use input format specified by loader name.

  output options:

    --bioc|cdi|bc|bc-evaluate   output compatible with evaluation program bc-evaluate
    --mmilike|mmi               similar to MetaMap Fielded MMI output
    --brat                      BRAT annotation format

  processing options:

    --restrict_to_sts=<semtype>[,<semtype>,<semtype>...]
    --restrict_to_sources=<source>[,<source>...]
    --segmentation_method=SENTENCES|BLANKLINES|LINES
                           Set method for text segmentation
	--segment_sentences    Segment text by sentence
    --segment_blanklines   Segment text by blankline
    --segment_lines        Segment text by line
	--usecontext           Use ConText Negation Detector instead of NLM's implementation of NegEx
	--negationDetectorClass=className
	                       Use a user-defined class for negation detector, class must implement to
	                       gov.nih.nlm.nls.metamap.lite.NegationDetector interface.

  alternate output options:

    --list_sentences          list sentences in input
    --list_acronyms           list acronyms in input if present.
    --list_sentences_postags  list sentences in input with part-of-speech tags

## Properties

### Command line and System properties for metamaplite

These properties can be set using a System property
(-D{propertyname}={value}).

    | metamaplite.property.file             | load configuration from file (default: ./config/metamaplite.properties)

These properties can be set using a System property
(-D{propertyname}={value}) or in configuration file.

    | metamaplite.document.inputtype        | document input type (default: freetext)
    | metamaplite.outputextension           | result output file extension (default: .mmi)
    | metamaplite.outputformat              | result output format (default: mmi)

### Processing properties

    | metamaplite.segmentation.method       | Set method for text segmentation (values: SENTENCES, BLANKLINES, LINES; default: SENTENCES)
    | metamaplite.sourceset                 | use only concepts from listed sources (default: all)
    | metamaplite.semanticgroup             | use only concepts belonging to listed semantic types (default: all)
	| metamaplite.negation.detector         | negation detector class: default: gov.nih.nlm.nls.metamap.lite.NegEx
	                                                                   Alternate: 
    | metamaplite.normalized.string.cache.size | set maximum size of string -> normalized string cache
    | metamaplite.normalized.string.cache.enable | if true enable string -> normalized string cache
    | metamaplite.entitylookup4.term.concept.cache.enable | if true enable term -> concept info cache
    | metamaplite.entitylookup4.term.concept.cache.size | set maximum size of term -> concept info cache
    | metamaplite.entitylookup4.cui.preferredname.cache.enable |  if true enable cui -> preferred name cache
    | metamaplite.entitylookup4.cui.preferredname.cache.size | set maximum size cui -> preferred name cache


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
	| metamaplite.enable.postagging         | Enable part of speech tagging (default: "true" [on])

## Using MetaMapLite from Java

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
	document.setID("1");
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


## Processing Single Terms (without periods)

Disable the Part of Speech Tagger using the following property:
"metamaplite.enable.postagging=false".  Add the following line right
before instantiating the MetaMapLite instance.

    myProperties.setProperty("metamaplite.enable.postagging", "false");
    MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);

Add each term as a single document:

    BioCDocument document = FreeText.instantiateBioCDocument(term);


## Adding MetaMapLite to a webapp (servlet).

### Where to place indices and models files in Tomcat

#### WebApp Local Configuration



#### Alternate Configuration

Place the "metamaplite.properties" file in the tomcat "conf/"
directory and specify that in servlet:

    public class SampleWebApp extends HttpServlet {
      /** location of metamaplite.properties configuration file */
      static String configPropertyFilename =
        System.getProperty("metamaplite.property.file", "conf/metamaplite.properties");
      Properties properties;
      MetaMapLite metaMapLiteInst;
    
      public SampleWebApp() {
        try {
          this.properties = new Properties();
          // default properties that can be overriden 
          this.properties.setProperty("metamaplite.ivf.cuiconceptindex","data/ivf/strict/indices/cuiconcept");
          ...
          // load user properties
          this.properties.load(new FileReader(configPropertyFilename));
          this.metaMapLiteInst = new MetaMapLite(this.properties);
		  ...
      	} catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      ...
    }
    
The absolute locations of indexes and model files can be specified in
"metamaplite.properties".

### Using Maven

#### Installing metamaplite and dependencies into local Maven repository

From public\_mm\_lite directory install Context, BioC, and NLS NLP libraries

    $ mvn install:install-file \
         -Dfile=lib/context-2012.jar \
         -DgroupId=context \
	     -DartifactId=context \
	     -Dversion=2012 \
         -Dpackaging=jar

    $ mvn install:install-file \
         -Dfile=lib/bioc-1.0.1.jar \
         -DgroupId=bioc \
	     -DartifactId=bioc \
	     -Dversion=1.0.1 \
         -Dpackaging=jar

    $ mvn install:install-file \
         -Dfile=lib/nlp-2.4.C.jar \
         -DgroupId=gov.nih.nlm.nls \
	     -DartifactId=nlp \
	     -Dversion=2.4.C \
         -Dpackaging=jar

Then install metamaplite into your local Maven repository:

    $ mvn install



#### Add metamaplite dependency to POM file

Add the following dependency to your webapps pom.xml:

    <dependency>
      <groupId>gov.nih.nlm.nls</groupId>
      <artifactId>metamaplite</artifactId>
      <version>3.0-SNAPSHOT</version>
    </dependency>

# irutils indexes

## Tables and Indexes

Currently, three tables are used:

* cuisourceinfo
* cuisemantictype (cuist)
* cuiconcept

## New indexes used for MetaMap-like scoring and MMI ranked output

Two new indexes have been introduced to support scoring similar to the
original MetaMap and MMI ranking of which MetaMap scoring is a
component.

+ treecodes - an indexing of MeSH Terms and their associated positions in MeSH hierarchy.
+ vars - an index of terms and their lexical variants.

NOTE: Currently, the only mechanism for generating the treecodes and
vars (variants) tables from a UMLS subset (generated by Metamorphosys)
is by installing the original MetaMap and the Data File Builder using
the Data Builder to generate the necessary Treecodes and Vars table
files.  See the next section for information on adding these indices
to a custom dataset.

## Generating indexes from UMLS tables

The CreateIndexes class generates tables cuiconcept, cuisourceinfo,
and cuist from MRCONSO.RRF and MRSTY.RRF and then produces
corresponding indexes for tables.  If the variants file, vars.txt, and
the treecodes file, mesh\_tc\_relaxed.txt, are present in the directory
{ivfdir}/tables then those files will be indexed as well.

Usage: 

     java -Xmx5g -cp target/metamaplite-<version>-standalone.jar \
      gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes <mrconsofile> <mrstyfile> <ivfdir>

The resulting indices are in <ivfdir>/indices.  The tables the indexes
are generated from are in <ivfdir>/tables.

## Checking newly generated indexes

You can use the class irutils.MappedMultiKeyIndexLookup to check the
new indexes:

     java -Xmx20g -cp target/metamaplite-<version>-standalone.jar \
      irutils.MappedMultiKeyIndexLookup lookup workingdir indexname column

For example:

     java -Xmx20g -cp target/metamaplite-<version>-standalone.jar \
      irutils.MappedMultiKeyIndexLookup lookup data/ivf/2016AB/USAbase/strict cuisourceinfo 0

## Using newly generated indexes with MetaMapLite


To use the new indexes do one of the following:

Use the --indexdir=<directory> option:

    java -cp target/metamaplite-<version>-standalone.jar \
     gov.nih.nlm.nls.ner.MetaMapLite --indexdir=<ivfdir> <other-options> <other-args>

Or modify the configuration file config/metamap.properties:

    metamaplite.ivf.cuiconceptindex: <ivfdir>/indices/cuiconcept
    mmetamaplite.ivf.cuisourceinfoindex: <ivfdir>/indices/cuisourceinfo
    metamaplite.ivf.cuisemantictypeindex: <ivfdir>/indices/cuist

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

## A BioC XML to A BioC XML implementation of MetaMapLite

The class gov.nih.nlm.nls.metamap.lite.BioCProcess allows MetaMapLite
to process BioC XML input and write the results in BioC XML.

### Using from Java

Below is an example using BioC Processing with MetaMapLite

    // Initialize MetaMapLite
    Properties defaultConfiguration = getDefaultConfiguration();
	String configPropertiesFilename =
		System.getProperty("metamaplite.propertyfile",
		                   "config/metamaplite.properties");
    Properties configProperties = new Properties();
    // set any in-line properties here.
	configProperties.load(new FileReader(configPropertiesFilename));
	configProperties.setProperty("metamaplite.semanticgroup",
	"acab,anab,bact,cgab,dsyn,emod,inpo,mobd,neop,patf,sosy");
	Properties properties =
	  Configuration.mergeConfiguration(configProperties,
					   defaultConfiguration);
	BioCProcess process = new BioCProcess(properties);

	// read BioC XML collection
	Reader inputReader = new FileReader(inputFile);
	BioCFactory bioCFactory = BioCFactory.newFactory("STANDARD");
	BioCCollectionReader collectionReader =
	bioCFactory.createBioCCollectionReader(inputReader);
	BioCCollection collection = collectionReader.readCollection();

	// Run named entity recognition on collection
	BioCCollection newCollection = process.processCollection(collection);

    // write out the annotated collection
    File outputFile = new File(outputFilename);
	Writer outputWriter = new PrintWriter(outputFile, "UTF-8");
	BioCCollectionWriter collectionWriter = bioCFactory.createBioCCollectionWriter(outputWriter);
	collectionWriter.writeCollection(newCollection);
	outputWriter.close();

This process attempts to preserve any annotation present in the
original BioC XML input.    Some annotations applied to BioC
structures before entity lookup, including `tokenization and
part-of-speech-tagging, are discarded before the writing out the final
annotated collection.   This occurs in the entity lookup class
BioCEntityLookup (java package: gov.nih.nlm.nls.metamap.lite).

### Command Line Usage:

The file biocprocess.sh is a wrapper for the BioC to BioC pipeline.

    ./biocprocess.sh <bioc-xml-input-file> <bioc-xml-output-file>

Note that the class is missing the command line options handler that
is present in the gov.nih.nlm.nls.ner.MetaMapLite class, use the
config/metamaplite.properties file to set any custom properties or
place the properties in another custom file and use the system
property "metamaplite.propertyfile" to refer to the custom file.

### Omissions in BioC version

The current version of the BioCProcess class does not call the
abbreviation detector or the negation detector.   This should be
relatively simple to add (particularly the abbreviation detector) and
will probably be added in the next release.

## Future

+ add optional scoring
+ Use |vars| files to add lexical distance for optional scoring
+ Create a ReSTful interface for MetaMapLite.
+ Create a pipeline using a chunker.
+ Create a pipeline using a full parser.
+ Add a mechanism to use custom user-supplied segmenters.


