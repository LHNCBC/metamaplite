# Trie based NER using a lexically enhanced trie dictionary.

# Optimizations

Cache references to entities in Map to avoid unnecessary lookups.

# input formats

Medline 
XML (Medline, user-specified)
JSON 
EDN

# pipeline

# pipeline definition

# pipeline protocols

object based

# pipeline wrapper modules


# plugins

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


