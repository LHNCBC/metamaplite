# MetaMap Lite: A lighter named-entity recognizer

## Installation


You'll need the model file for the sentence extractor "en-sent.bin"
which can be downloaded from the opennlp project at
http://opennlp.sourceforge.net/models-1.5

Set the system property "en-sent.bin.path":

    en-sent.bin.path=<location of en-sent.bin>

Modify metamaplite.sh to set the location of the model file. To run
the test application use the script metamaplite.sh


## Usage


    ./metamaplite.sh [options] <input file>

Current options are:

 * --freetext:     Text with no markup.
 * --chemdner:     CHEMDNER document: tab separated fields: id \t title \t abstract
 * --chemdnerSLDI: CHEMDNER document: id with pipe followed by tab separated fields: id | title \t abstract

The application currently only outputs to standard output. (See
method: gov.nih.nlm.nls.metamap.lite.EntityLookup.displayEntitySet)


The old program:

    $ ./run.sh -Den-sent.bin.path=/usr/local/pub/nlp/opennlp/models/en-sent.bin \
		       -Den-token.bin.path=/usr/local/pub/nlp/opennlp/models/en-token.bin \
               -Den-pos-maxent.bin.path=/usr/local/pub/nlp/opennlp/models/en-pos-maxent.bin \
	gov.nih.nlm.nls.metamap.lite.Main filename

  $ ./run.sh gov.nih.nlm.nls.metamap.lite.Main ~/queries/chemdner_abs_sample.txt


