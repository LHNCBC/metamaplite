# MetaMap Lite: A lighter named-entity recognizer

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

 * --freetext:     Text with no markup.
 * --chemdner:     CHEMDNER document: tab separated fields: id \t title \t abstract
 * --chemdnerSLDI: CHEMDNER document: id with pipe followed by tab separated fields: id | title \t abstract


## Adding custom input document formats

New document loader class must conform to BioCDocumentLoader interface.


## Adding custom result output formats

New result formatter class must conform to ResultFormatter interface.

## Feature Requests

1. add support for exclusion of some cui/term combination (see MetaMap's special terms file)
2. support entity lookup using two separate dataset (UMLS and custom, etc.)

## Known bugs

1. Semantic Type filter does not always filter out some type not in list.
2. Abbreviation detector doesn't always work

## Packaging

Required files:

1. Java class and supporting jars
2. POM and ANT files
3. Open NLP sentence model files
4. Lucene indices

