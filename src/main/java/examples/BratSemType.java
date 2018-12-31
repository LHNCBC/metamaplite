package examples;

import bioc.BioCAnnotation;
import bioc.BioCDocument;
import bioc.BioCLocation;
import bioc.BioCPassage;
import bioc.BioCSentence;

import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.resultformats.ResultFormatter;
import gov.nih.nlm.nls.metamap.lite.resultformats.Brat;

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
 * Example of a User Defined Result Formatter -- an implementation of
 * Brat Standoff Format using semantic types as entities.
 * <p>
 * To use this Result Formatter add the following line to <tt>config/metamaplite.properties</tt>:
 *
 * <pre>
 * metamaplite.result.formatter.bratsemtype=examples.BratSemType
 * </pre>
 *
 * or set it when invoking metamaplite:
 * 
 * <pre>
 * ./metamaplite.sh --set_property=metamaplite.result.formatter.bratsemtype=examples.BratSemType \
 *                  --outputformat=bratsemtype \
 *                  --output_extension=.ann ...
 *                 
 * </pre>
 * or add it to the properties object passed to MetaMapLite constructor:
 *
 * <pre>
 * Properties myProperties = MetaMapLite.getDefaultConfiguration();
 * myProperties.setProperty("metamaplite.result.formatter.bratsemtype", "examples.BratSemType");
 * ...
 * MetaMapLite metaMapLiteInst = new MetaMapLite(myProperties);
 * </pre>
 */
public class BratSemType implements ResultFormatter
{

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
  }

 public static String annotationListToString(List<Entity> entityList) {
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
	for (Ev ev: entity.getEvList()) {
	  for (String semType: ev.getConceptInfo().getSemanticTypeSet()) {
	    TextBoundAnnotation textAnnot = new TextBoundAnnotation("T0",semType,start,end,term);
	    if (annotationMap.containsKey(textAnnot.genKey())) {
	      // textAnnot = annotationMap.get(textAnnot.genKey());
	      // textAnnot.addToReferenceList(BratSemType.generateReferenceList(entity));
	    } else {
	      textAnnot.setReferenceSet(Brat.generateReferenceSet(entity));
	      annotationMap.put(textAnnot.genKey(), textAnnot);
	    }
	  }
	}
      }
    }
    Set<TextBoundAnnotation> annotationSet = new HashSet<TextBoundAnnotation>(annotationMap.values());
    return annotationSetToString(annotationSet);
  } /* listEntities */

  @Override
  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    writer.println(entityListFormatToString(entityList));
  }

  @Override
  public String entityListFormatToString(List<Entity> entityList) {
    return annotationListToString(entityList);  
  }

  @Override
  public void initProperties(Properties properties) {
    
  }
}
