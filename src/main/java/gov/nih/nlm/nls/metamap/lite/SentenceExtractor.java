
//
package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import java.util.Map;

import gov.nih.nlm.nls.types.Sentence;
import gov.nih.nlm.nls.types.Annotation;
import bioc.BioCPassage;
import bioc.BioCSentence;

/**
 *
 */

public interface SentenceExtractor 
{
  List<Sentence> createSentenceList(String text);
  List<Sentence> createSentenceList(String text, int offset);
  int addBioCSentence(BioCPassage passage, String sentenceText, int offset, Map<String,String> infoNS);
  BioCPassage createSentences(BioCPassage passage); 
}
