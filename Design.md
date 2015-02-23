# MetaMap Lite 

## Feature Requests

1. add support for exclusion of some cui/term combination (see MetaMap's special terms file)
2. support entity lookup using two separate dataset (UMLS and custom, etc.)

## Known bugs

1. Semantic Type filter does not always filter out all excluded types
2. Abbreviation detector doesn't always work

## Packaging

### Required files

1. Java class and supporting jars
2. POM and ANT files
3. Open NLP sentence model files
4. Lucene indices
5. Local libraries which are not available through Maven.


### Organization

     metamaplite -+- src 
                  +- config
                  +- data -+- models
                  |        +- lucenedb -+- strict -+- cui_sourceinfo 
                  |                                +- cui_st
                  +- logs
                  +- scripts
                  +- target

## Possible implementations

### Trie based

 Trie based NER using a lexically enhanced trie dictionary.

### Lucene index based

Current implementation uses Lucene indexes

### IRUtils multi-key index

Multi-key Inverted File index (Implementation in process).

## Optimizations

Cache references to entities in Map to avoid unnecessary lookups, this
is in current implementation.

## Input Formats

### Current

+ FreeText
+ Single Line Input (SLI)
+ Single Line Delimited Input (SLDI)
+ ChemDNER
+ ChemDNERSLDI
+ NCBICorpus

### Future

+ Medline 
+ XML (Medline, user-specified)
+ JSON 
+ EDN

# Pipeline

# Pipeline Definition

# Pipeline Protocols

object based

# Pipeline Wrapper Modules


# Plugins

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


# Generating datafiles

## Generating cuisourceinfoindex (cui_sourceinfo)

Use extract\_mrconso\_sources.perl:

    http://indlx1.nlm.nih.gov:8000/cgi-bin/cgit.cgi/public_mm/tree/bin/extract_mrconso_sources.perl

## Generating cuisemantictypeindex (cui_st)




