
//
package gov.nih.nlm.nls.metamap.lite;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nlm.nls.types.Annotation;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.TokenListAnnotation;
import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;

import bioc.BioCAnnotation;
import bioc.BioCSentence;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

/**
 *
 */

public class SentenceAnnotator {

  /** apply analyzetext to tokenize sentence and then add tokenlist annotation to sentence. */
  public static BioCSentence tokenizeSentence(BioCSentence sentence) {
    sentence.addAnnotation
      (new TokenListAnnotation("",
			       sentence.getText(),
			       Scanner.analyzeText(sentence)));
    return sentence;
  }

  /** precondition: sentence must contain a TokenListAnnotation. */
  public static BioCSentence addEntities(BioCSentence sentence)
    throws IOException, ParseException
  {
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      if (annotation instanceof TokenListAnnotation) {
	Set<BioCAnnotation> entitySet = EntityLookup.generateBioCEntitySet((List<? extends Token>)((TokenListAnnotation)annotation).getTokenList());
	sentence.setAnnotations(new ArrayList<BioCAnnotation>(entitySet));
      }
    }
    return sentence;    
  }
}
