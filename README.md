# MetaMapLite: A lighter named-entity recognizer

The primary goal of MetaMapLite is to provide a near real-time
named-entity recognizer which is not as rigorous as MetaMap but much
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
+ Scoring approximating the original MetaMap's scoring
* MMI Ranking similar to the original MetaMap

What is missing:

+ No detection of disjoint entities
+ No derivational variants
+ No word sense disambiguation (to be added later)
+ No overmatching
+ No term processing
+ No dynamic variant generation

## Prerequisites

### For running

* Java 1.8 JRE

### For Development

* Java 1.8 JDK
* Maven primarily.  Ant and Gradle build scripts are provided but are
  not supported.

## Command Line Usage

Example of invocation on Linux or MINGW using script:

    ./metamaplite.sh [options] [<input file>|--]

Example of invocation on Windows using batch file:

    metamaplite.bat [options] [<input file>|--]

Example of invocation using Java VM directly when running from the
__public_mm_lite__ directory:

    $ java -cp target/metamaplite-3.6.2rc5-standalone.jar \
          gov.nih.nlm.nls.ner.MetaMapLite \
          --indexdir=data/ivf/strict \
          --modelsdir=data/models \
          --specialtermsfile=data/specialterms.txt  [options] [<input file>|--]

### Reading from standard input

    echo "asymptomatic patient populations" | ./metamaplite.sh --pipe

or

    cat file | ./metamaplite.sh --pipe

Output will be sent to standard output.

### Restricting to a set of semantic types

The list of concepts returned can be restricted to a only those that
refer to a subset of the UMLS semantic types by semantic type
abbreviations:

    ./metamaplite.sh --restrict_to_sts=abbrev,abbrev ...

The following option restricts to concepts that have the semantic types
disease or syndrome (dsyn) and hazardous or poisonous substance (hops):

    ./metamaplite.sh --restrict_to_sts=dsyn,hops

A full list of semantic types and their abbreviations is at:

https://metamap.nlm.nih.gov/Docs/SemanticTypes_2018AB.txt

### Restricting to a set of source vocabularies

The follows option specifies that only concepts that appear in the
MeSH (MSH), and NCBI Taxonomy (NCBI) vocabularies will be returned:

    ./metamaplite.sh --restrict_to_sources=MSH,NCBI ...

A full list of the current source vocabularies and their abbreviations
can be found at:

https://www.nlm.nih.gov/research/umls/sourcereleasedocs/index.html


### Annotating a brat directory

To create annotation files (.ann) in a directory from the associated
text files (.txt)

    ./metamaplite.sh --brat directory/*.txt

### User defined acronyms

The option –uda allows a user to supply a list of user defined
acronyms or abbreviations with associated long forms.  When
MetaMapLite encounters a user defined acronym, it will attach the
information associated with the acronym's long form.

    ./metamaplite.sh --uda=acronymfile ...

The acronym file is the form "acronym|long form", for example:

     LAD|Left anterior descending coronary artery
     SVG|Saphenous Vein Graft
     PLB|Posterior lateral branch
     PDA|Patent Ductus Arteriosus
     IM|Intramuscular


### User defined concepts

The option –cuitermlistfile allows a user to add a list of concepts
not present in MetaMapLite’s dataset at invocation:

    ./metamaplite.sh --cuitermlistfile=conceptfile ...

The concepts file is the form "cui|term", for example:

     C5203670|COVID-19
     C5203671|Suspected COVID-19
     C5203672|SARS-CoV-2 vaccination
     C5203673|Antigen of SARS-CoV-2
     C5203674|Antibody to SARS-CoV-2
     C5203675|Exposure to SARS-CoV-2
     C5203676|severe acute respiratory syndrome coronavirus 2


### Current options

  input options:

    --                             Read from standard input
    --pipe                         Read from standard input

  Configuration Options:

    --configfile=<filename>        Use configuration file
    --set_property=name=value      set property "name" to value

    --filelistfn=<filename>        file containing a list of files to processed, one line per file
    --filelist=<file0,file1,...>   list of files to processed separated by commas

    --uda=<filename>               user defined acronyms file.
    --cuitermlistfile=<filename>   user defined concepts file.

Options that can be used to override configuration file or when
configuration file is not present:

    --indexdir=<directory>         location of program's index directory
    --modelsdir=<directory>        location of models for sentence breaker and part-of-speech tagger
	--specialtermsfile=<filename>  location of file of terms to be excluded

  document processing options:

    --freetext                  Text with no markup. (default)
    --inputformat=<loadername>	Use input format specified by loader name.
    --inputformat=pubmed        PubMed XML format
    --inputformat=medline       Medline format
    --inputformat=ncbicorpus    NCBI Disease Corpus: tab separated fields: id \t title \t abstract
    --inputformat=chemdner      CHEMDNER document: tab separated fields: id \t title \t abstract
    --inputformat=chemdnersldi  CHEMDNER document: id with pipe followed by tab separated fields: id |t title \t abstract

  output options:

    --mmilike|mmi               similar to MetaMap Fielded MMI output (default)
    --bioc|cdi|bc|bc-evaluate   output compatible with evaluation program bc-evaluate
    --brat                      BRAT annotation format
	--outputformat=<format>       
    --outputformat=json         JSON output format

  processing options:

    --restrict_to_sts=<semtype>[,<semtype>,<semtype>...]

          Restrict output to concepts that have at least one member
          in the list of user-specified semantic-types. The list of
          supported semantic type short forms used by this program is
          available at https://metamap.nlm.nih.gov/Docs/SemanticTypes_2018AB.txt

    --restrict_to_sources=<source>[,<source>...]
          Restrict output to concepts that belong to the list of specified vocabularies.
          A full list of the current source vocabularies and their abbreviations
          can be found at https://www.nlm.nih.gov/research/umls/sourcereleasedocs/index.html

    --segmentation_method=SENTENCES|BLANKLINES|LINES
                           Set method for text segmentation
	--segment_sentences    Segment text by sentence
    --segment_blanklines   Segment text by blankline
    --segment_lines        Segment text by line
	--usecontext           Use ConText Negation Detector instead of NLM's implementation of NegEx
	--negationDetectorClass=className
	                       Use a user-defined class for negation detector, class must implement to
	                       gov.nih.nlm.nls.metamap.lite.NegationDetector interface.
    --postaglist=tag,tag,...
                           List of part-of-speech tags to use for term lookup (each Penn Treebank
                           part-of-speech tag is separated by commas.)

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
    | metamaplite.index.directory           | the directory the indexes reside
    | opennlp.models.directory              | the directory the models reside (sets the following properties. default: data/models)
    | opennlp.en-pos.bin.path               | (default: data/models/en-pos-maxent.bin)
    | opennlp.en-token.bin.path             | (default: data/models/en-token.bin)
    | opennlp.en-sent.bin.path              | (default: data/models/en-sent.bin)
	| metamaplite.enable.postagging         | Enable part of speech tagging (default: "true" [on])
	| metamaplite.postaglist                | List of part-of-speech tags to use for term lookup
	                                        | (each Penn Treebank part-of-speech tag is separated by commas.)
	| metamaplite.enable.scoring            | score concepts (I.E.: turn on chunker [currently OpenNLP]).

	| metamaplite.uda.filename              | user defined acronyms file.
	| metamaplite.cuitermlistfile.filename  | user defined concepts file.

### Environment Variables
currently one

MML_INDEXDIR

## Using MetaMapLite from Java

Creating properties for configuring MetaMapLite Instance:
    
    Properties myProperties = new Properties();
    MetaMapLite.expandModelsDir(myProperties,
                   "/home/piro/public_mm_lite/data/models");
    MetaMapLite.expandIndexDir(myProperties,
			       "/home/piro/Projects/public_mm_lite/data/ivf/strict");
    myProperties.setProperty("metamaplite.excluded.termsfile",
 				   "/home/piro/Projects/public_mm_lite/data/specialterms.txt");


Loading properties file in "config":

    myProperties.load(new FileReader("config/metamaplite.properties"));


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

    $ mvn install:install-file  \
         -Dfile=lib/lvgdist-2020.0.jar \
         -DgroupId=gov.nih.nlm.nls.lvg \
         -DartifactId=lvgdist \
         -Dversion=2020.0 \
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

# Irutils Indexes

## Tables and Indexes

Currently, five tables are used:

* cuisourceinfo
* cuisemantictype (cuist)
* cuiconcept
* meshtcrelaxed (MeSH treecodes)
* vars (lexical variants)

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

The CreateIndexes class generates the tables needed by MetaMapLite for
a particular UMLS release.  The tables cuiconcept, cuisourceinfo,
cuist, meshtcrelaxed, and vars are derived from the UMLS tables
MRCONSO.RRF, MRSAT.RRF, and MRSTY.RRF and then CreateIndexes produces
the associated indexes for derived tables.  To produce the vars table
you will need to install the Lexical Variant Generator (LVG) which is
available from the Lexical Tools Page:
https://lexsrv3.nlm.nih.gov/LexSysGroup/Projects/lvg/current/web/index.html
.  LVG is used when generating the vars table from MRCONSO.RRF.

Usage: 

    java -Xmx15g -cp target/metamaplite-<version>-standalone.jar \
       gov.nih.nlm.nls.metamap.dfbuilder.CreateIndexes \
	   <mrconsofile> <mrstyfile> <mrsatfile> <ivfdir>

When running CreateIndexes must be provided the location of LVG
configuration file by one of two mechanisms: Setting an environment
variable __LVG_DIR__ or __LVG_CONFIG__ or setting a system property when
invoking java.  The simplest way of doing this by setting LVG_DIR to
the location of LVG and CreateIndexes program will infer the location
of the properties file:

on windows:

     set LVG_DIR=<location of lvg2020>

in bash on MacOS or Linux:

     export LVG_DIR=<location of lvg2020>

If you have a modified lvg.property file in an custom location you can
set the variable __LVG_CONFIG__ to the location of your custom lvg
property file.

Alternatively, you can set system property gv.lvg.dirname to the
location of LVG or setting the property gv.lvg.config.file to the
location of lvg.properties, usually
lvg2020/data/config/lvg.properties:

     -Dgv.lvg.dirname={location of lvg}

or:

     -Dgv.lvg.config.file={location of lvg.properties}


The resulting indices are in <ivfdir>/indices.  The tables the indexes
are generated from are in <ivfdir>/tables.

## Checking newly generated indexes

You can use the class irutils.MappedMultiKeyIndexLookup to check the
new indexes:

     java -Xmx20g -cp target/metamaplite-<version>-standalone.jar \
      irutils.MappedMultiKeyIndexLookup lookup workingdir indexname column query

For example:

     java -Xmx20g -cp target/metamaplite-<version>-standalone.jar \
      irutils.MappedMultiKeyIndexLookup lookup data/ivf/2016AB/USAbase/strict cuisourceinfo 3 heart

## Using newly generated indexes with MetaMapLite


To use the new indexes do one of the following:

Use the --indexdir=<directory> option:

    java -cp target/metamaplite-<version>-standalone.jar \
     gov.nih.nlm.nls.ner.MetaMapLite --indexdir=<ivfdir> <other-options> <other-args>

Or modify the configuration file config/metamap.properties:

    metamaplite.index.directory: <ivfdir>

## Adding custom input document formats

New document loader class must conform to BioCDocumentLoader
interface.

Example implementations of BioCDocumentLoader are available in
public\_mm_lite/src/main/java/gov/nih/nlm/nls/metamap/document and on
Github:
https://github.com/lhncbc/metamaplite/tree/master/src/main/java/gov/nih/nlm/nls/metamap/document.

One can add a document loader class in MetaMapLite's
classpath to MetaMapLite's list of document loaders by adding it to the
properties using System properties or modifying MetaMapLite's
configuration file:

Set as system property:

    -Dbioc.document.loader.<name>=<fully-specified class name>

For example creating a loader with the name "qadocument":

    -Dbioc.document.loader.qadocument=gov.nih.nlm.nls.metamap.document.QAKeyValueDocument

Or add it to config/metamaplite.properties:

    bioc.document.loader.qadocument: gov.nih.nlm.nls.metamap.document.QAKeyValueDocument


An example of using the new custom document format through a properties:

    -Dmetamaplite.document.inputtype=<name>

For example:

    -Dmetamaplite.document.inputtype=qadocument

On the command line use the ‘inputformat=<formatname>’:

    --inputformat=qadocument


## Adding custom result output formats

New result formatter class must conform to ResultFormatter interface.
One can add the result formatter to MetaMapLite by adding its class
file to MetaMapLite's classpath and then adding a reference to it as a
property:

Set as system property:

    -Dmetamaplite.result.formatter.<name>=<fully-specified class name>

For example creating a formatter with the name "bratsemtype":

    -Dmetamaplite.result.formatter.brat=examples.BratSemType

Or add it to config/metamaplite.properties:

     metamaplite.result.formatter.brat: examples.BratSemType

Source code for the BratSemType result formatter is provided in the directory
public\_mm_lite/src/main/java/examples/BratSemType.java.


## Adding MetaMapLite to a webapp (servlet).

#### WebApp Local Configuration

A extensive example of providing a servlet complete with data and
configuration files in the war (web archive) file is available on the
MetaMap website on the MetaMapLite web page
(https://metamap.nlm.nih.gov/MetaMapLite.shtml).

#### Alternate Configuration

Below is an alternate configuration for users who don't want to place
the configuration and data in webapp deployment archive file (war).

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
          this.properties.setProperty("metamaplite.index.directory","data/ivf/strict");
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


### Command Line Usage:

The file biocprocess.sh is a wrapper for the BioC to BioC pipeline.

    ./biocprocess.sh <bioc-xml-input-file> <bioc-xml-output-file>

Note that the class is missing the command line options handler that
is present in the gov.nih.nlm.nls.ner.MetaMapLite class, use the
config/metamaplite.properties file to set any custom properties or
place the properties in another custom file and use the system
property "metamaplite.propertyfile" to refer to the custom file.

### Files in which sources are not included

The following java archive files have sources available from NLM but
the sources are not provided by this distribution.

+ archive: https://metamap.nlm.nih.gov/maven2/gov/nih/nlm/nls/nlp/2.4.C/nlp-2.4.C.jar
+ archive: https://metamap.nlm.nih.gov/maven2/gov/nih/nlm/nls/lvg/lvgdist/2020.0/lvgdist-2020.0.jar
+ archive: https://metamap.nlm.nih.gov/maven2/gov/nih/nlm/nls/mps/medpostskr/1.0/medpostskr-1.0.jar

The sources are available at the following locations:

+  nlp-2.4.C.jar is available at MetaMapLite Download Page at:  https://metamap.nlm.nih.gov/download/old/nls-rel-2-4-C.tar.bz2
+ Sources for lvg-2020.0.jar are available from the Lexical Tools Page:
https://lexsrv3.nlm.nih.gov/LexSysGroup/Projects/lvg/current/web/index.html
+ Sources for MedPost/SKR tagger are available at: https://metamap.nlm.nih.gov/MedPostSKRTagger.shtml

## Future Work

+ Add support for composite phrases from chunked phrases
+ Create a pipeline using a full parser.
+ Add a mechanism to use custom user-supplied segmenters.



