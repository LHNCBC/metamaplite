//
package gov.nih.nlm.nls.metamap.lite.resultformats;

import bioc.BioCAnnotation;
import bioc.BioCDocument;
import bioc.BioCLocation;
import bioc.BioCPassage;
import bioc.BioCSentence;

import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;

import gov.nih.nlm.nls.tools.standoff.NormalizationAnnotation;
import gov.nih.nlm.nls.tools.standoff.TextBoundAnnotation;

import java.io.PrintWriter;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Properties;

/**
 * Implementation of Brat Standoff Format.
 */

public class Brat implements ResultFormatter {

  public static String bratEntityTypeName =
    System.getProperty("metamaplite.brat.typename", "MMLite");
  String textLabel = bratEntityTypeName;
  
  /**
   * Generate reference set for entity as NormalizationAnnotation
   * instances.
   *
   * @param entity Entity to convert to Normalization annotations
   * @return set of normalization annotations
   */
  public static Set<NormalizationAnnotation> generateReferenceSet(Entity entity) {
    Set<NormalizationAnnotation> referenceSet = new HashSet<NormalizationAnnotation>();
    if (entity.getScore() > 0.0) {
      referenceSet.add(new NormalizationAnnotation("N0","T0", "Score",
						   Double.toString(entity.getScore()),
						   Double.toString(entity.getScore())));
    }
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      String preferredName = ev.getConceptInfo().getPreferredName();
      if ((cui == null) || (preferredName == null)) {
	System.out.println("cui or preferred name is null for entity: " + entity);
      } else {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "ConceptId", cui, preferredName));
      }
      for (String semtype: ev.getConceptInfo().getSemanticTypeSet()) {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "SemanticType", cui + ":" + semtype, semtype));
      }
      for (String source: ev.getConceptInfo().getSourceSet()) {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "Source", cui + ":" + source, source));
      }
      if (entity.isNegated()) {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "Negated", ev.getMatchedText(), ev.getMatchedText()));
      }
      if (entity.getTemporality().trim().length() > 0) {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "Temporality", entity.getTemporality(), entity.getTemporality()));
      }
    }
    return referenceSet;
  }

  /**
   * Write text bound annotations and associated reference annotations
   * into string in Standoff annotation format.
   * 
   * @param annotationSet set of entity annotations with references.
   * @return string of rendered entity and reference annotations.
   */
  public static String annotationSetToString(Set<TextBoundAnnotation> annotationSet) {
    StringBuilder sb = new StringBuilder();
    int cindex = 0;
    int nindex = 0;
    for (TextBoundAnnotation annotation: annotationSet) {
      cindex++;
      String tid = "T" + cindex;
      annotation.setId(tid);
      sb.append(annotation.toString()).append("\n");
      if (annotation.getReferenceSet() != null) {
	for (NormalizationAnnotation nAnnotation: annotation.getReferenceSet()) {
	  nindex++;
	  nAnnotation.setId("N" + nindex);
	  nAnnotation.setTarget(tid);
	  sb.append(nAnnotation.toString()).append("\n");
	}
      }
    }
    return sb.toString();
  } /* annotationSetToString */

  /**
   * Write entities to string in Standoff annotation format.
   * 
   * @param recognizerName name to use for entity annotation typename 
   * @param entityList list of entities to be converted to annotations
   * @return string of rendered entity and reference annotations.
   */
  public static String annotationListToString(String recognizerName,
					      List<Entity> entityList) {
    Map<String,List<Entity>> locationMap = new HashMap<String,List<Entity>>();
    for (Entity entity: entityList) {
      String location = entity.getStart() + ":" + entity.getLength();
      if (locationMap.containsKey(location)) {
	locationMap.get(location).add(entity);
      } else {
	List<Entity> annotationList = new ArrayList<Entity>();
	annotationList.add(entity);
	locationMap.put(location, annotationList);
      } /*if*/
    } /*for*/
    Map<String,TextBoundAnnotation> annotationMap = new HashMap<String,TextBoundAnnotation>();
    for (Map.Entry<String,List<Entity>> entry: locationMap.entrySet()) {
      for (Entity entity: entry.getValue()) {
        int start = entity.getStart();
	int end = start + entity.getLength();
	String term = entity.getText();	  
	TextBoundAnnotation textAnnot = new TextBoundAnnotation("T0",recognizerName,start,end,term);
	if (annotationMap.containsKey(textAnnot.genKey())) {
	  // textAnnot = annotationMap.get(textAnnot.genKey());
	  // textAnnot.addToReferenceList(Brat.generateReferenceList(entity));
	} else {
	  textAnnot.setReferenceSet(Brat.generateReferenceSet(entity));
	  annotationMap.put(textAnnot.genKey(), textAnnot);
	}
      }
    }
    Set<TextBoundAnnotation> annotationSet = new HashSet<TextBoundAnnotation>(annotationMap.values());
    return annotationSetToString(annotationSet);
  } /* annotationListToString */

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    writer.print(annotationListToString(this.textLabel, entityList));
  }

  public String entityListFormatToString(List<Entity> entityList) {
    return annotationListToString(this.textLabel, entityList);
  }

  public void initProperties(Properties properties) {
    if (properties.containsKey("metamaplite.brat.typename")) {
      this.textLabel = properties.getProperty("metamaplite.brat.typename");
    }
  }
}
