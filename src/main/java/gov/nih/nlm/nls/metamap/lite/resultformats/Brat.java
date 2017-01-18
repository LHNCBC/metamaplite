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

import opennlp.tools.dictionary.serializer.Entry;

/**
 *
 */

public class Brat implements ResultFormatter {

  public static String bratEntityTypeName =
    System.getProperty("metamaplite.brat.typename", "MMLite");
  String textLabel = bratEntityTypeName;
  
  static class TextBoundAnnotation {
    String id;
    String type;
    int startOffset;
    int endOffset;
    String text;     
    Set<NormalizationAnnotation> referenceSet = null;

    public TextBoundAnnotation(String id, String type, int start, int end, String text) {
      
       this.id = id; 
       this.type = type;
       this.startOffset = start;
       this.endOffset = end;
       this.text = text.replaceAll("\n","\\n");
    }
    public TextBoundAnnotation(String id, String type, int start, int end, String text, 
			       Set<NormalizationAnnotation> referenceSet) {
       this.id = id; 
       this.type = type;
       this.startOffset = start;
       this.endOffset = end;
       this.text = text.replaceAll("\n","\\n");
       this.referenceSet = referenceSet;
    }
    public void setId(String id) { this.id = id; }
    public void setReferenceSet(Set<NormalizationAnnotation> referenceSet) { 
      this.referenceSet = referenceSet;
    }
    public void addToReferenceSet(Set<NormalizationAnnotation> referenceSet) { 
      this.referenceSet.addAll(referenceSet);
    }
    public Set<NormalizationAnnotation> getReferenceSet() { return this.referenceSet; } 
    public String toString() {
      return this.id + "\t" + this.type + " " + this.startOffset + " " + this.endOffset + "\t" + this.text;
    }
    public int hashCode() { return this.type.hashCode() + this.startOffset + this.endOffset + this.text.hashCode(); }
    public String genKey() { return this.type.hashCode() + ":" + this.startOffset + ":" + this.endOffset + "|" + this.text.hashCode(); }
  }
  static class NormalizationAnnotation {
    String id;
    String target;
    String rid;
    String eid;
    String text;
    NormalizationAnnotation(String id, String target, String rid, String eid, String text) {
      this.id = id.trim(); 
      this.target = target.trim();
      this.rid = rid.trim();
      this.eid = eid.trim();
      this.text = text.trim();
    }
    public boolean equals(Object obj) {
      return  (this.rid.equals(((NormalizationAnnotation)obj).rid) &&
	       this.eid.equals(((NormalizationAnnotation)obj).eid) &&
	       this.text.equals(((NormalizationAnnotation)obj).text));
    }
    public int hashCode() {
      return (this.rid + this.eid + this.text).hashCode();
    }
    public void setId(String id) { this.id = id; }
    public void setTarget(String target) { this.target = target; }
    public String getRid()  { return this.rid; }
    public String getEid()  { return this.eid; }
    public String getText() { return this.text; }
    public String toString() {
      return this.id + "\tReference " + this.target + " " + this.rid + ":" + this.eid + "\t" + this.text;
    }
  }

  static class RelationAnnotation {
    String id;
    String type;
    String arg1;
    String arg2;
    RelationAnnotation(String id, String type, String arg1, String arg2) {
	this.id = id;
	this.type = type;
	this.arg1 = arg1;
	this.arg2 = arg2;
      }
    public void setId(String id) { this.id = id; }
    public void setArg1(String target) { this.arg1 = target; }
    public void setArg2(String target) { this.arg2 = target; }
  }

  public static Set<NormalizationAnnotation> generateReferenceSet(Entity entity) {
    Set<NormalizationAnnotation> referenceSet = new HashSet<NormalizationAnnotation>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      String preferredName = ev.getConceptInfo().getPreferredName();
      if ((cui == null) || (preferredName == null)) {
	System.out.println("cui or preferred name is null for entity: " + entity);
      } else {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "ConceptId", cui, preferredName));
      }
      for (String semtype: ev.getConceptInfo().getSemanticTypeSet()) {
	referenceSet.add(new NormalizationAnnotation("N0","T0", "SemanticType", semtype, semtype));
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

  public static void writeAnnotationSet(String recognizerName,
			   PrintWriter writer,
			   Set<TextBoundAnnotation> annotationSet) {
    int cindex = 0;
    int nindex = 0;
    for (TextBoundAnnotation annotation: annotationSet) {
      cindex++;
      String tid = "T" + cindex;
      annotation.setId(tid);
      writer.println(annotation.toString());
      if (annotation.getReferenceSet() != null) {
	for (NormalizationAnnotation nAnnotation: annotation.getReferenceSet()) {
	  nindex++;
	  nAnnotation.setId("N" + nindex);
	  nAnnotation.setTarget(tid);
	  writer.println(nAnnotation.toString());
	}
      }
    }
  }

  public static void writeAnnotationSet(String recognizerName,
			   PrintStream writer,
			   Set<TextBoundAnnotation> annotationSet) {
    int cindex = 0;
    int nindex = 0;
    for (TextBoundAnnotation annotation: annotationSet) {
      cindex++;
      String tid = "T" + cindex;
      annotation.setId(tid);
      writer.println(annotation.toString());
      if (annotation.getReferenceSet() != null) {
	for (NormalizationAnnotation nAnnotation: annotation.getReferenceSet()) {
	  nindex++;
	  nAnnotation.setId("N" + nindex);
	  nAnnotation.setTarget(tid);
	  writer.println(nAnnotation.toString());
	}
      }
    }
  }

  public static void writeBratAnnotations(String recognizerName,
					  PrintWriter writer,
					  BioCDocument document) {
    Map<BioCLocation,List<BioCAnnotation>> locationMap = new HashMap<BioCLocation,List<BioCAnnotation>>();
    for (BioCPassage passage: document.getPassages()) {
      for (BioCSentence sentence: passage.getSentences()) {
	for (BioCAnnotation annotation: sentence.getAnnotations()) {
	  for (BioCLocation location: annotation.getLocations()) {
	    if (locationMap.containsKey(location)) {
	      locationMap.get(location).add(annotation);
	    } else {
	      List<BioCAnnotation> annotationList = new ArrayList<BioCAnnotation>();
	      annotationList.add(annotation);
	      locationMap.put(location, annotationList);
	    } /*if*/
	  } /*for*/
	} /*for*/
      } /*for*/
    } /*for*/

    Set<TextBoundAnnotation> annotationSet = new HashSet<TextBoundAnnotation>();
    for (Map.Entry<BioCLocation,List<BioCAnnotation>> entry: locationMap.entrySet()) {
      BioCLocation location = entry.getKey();
      for (BioCAnnotation annotation: entry.getValue()) {
        int start = location.getOffset();
	int end = start + location.getLength();
	String term = annotation.getText();	  
	if (annotation instanceof BioCEntity) {
	  BioCEntity entityAnnotation = (BioCEntity)annotation;
	  for (Entity entity: entityAnnotation.getEntitySet()) {
	    annotationSet.add(new TextBoundAnnotation("T0",recognizerName,start,end,term,Brat.generateReferenceSet(entity)));
	  }
	} else {
	  annotationSet.add(new TextBoundAnnotation("T0",recognizerName,start,end,term));
	}
      }
    }
    writeAnnotationSet(recognizerName, writer, annotationSet);
  } /* writeBratAnnotations */


  public static void listEntities(BioCSentence sentence) {
    PrintStream writer = System.out;
    String recognizerName = bratEntityTypeName;
    Map<BioCLocation,List<BioCAnnotation>> locationMap = new HashMap<BioCLocation,List<BioCAnnotation>>();
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      for (BioCLocation location: annotation.getLocations()) {
	if (locationMap.containsKey(location)) {
	  locationMap.get(location).add(annotation);
	} else {
	  List<BioCAnnotation> annotationList = new ArrayList<BioCAnnotation>();
	  annotationList.add(annotation);
	  locationMap.put(location, annotationList);
	} /*if*/
      } /*for*/
    } /*for*/

    Map<String,TextBoundAnnotation> annotationMap = new HashMap<String,TextBoundAnnotation>();
    for (Map.Entry<BioCLocation,List<BioCAnnotation>> entry: locationMap.entrySet()) {
      BioCLocation location = entry.getKey();
      for (BioCAnnotation annotation: entry.getValue()) {
        int start = location.getOffset();
	int end = location.getOffset() + location.getLength();
	String term = annotation.getText();	  
	TextBoundAnnotation textAnnot = new TextBoundAnnotation("T0",recognizerName,start,end,term);
	if (annotation instanceof BioCEntity) {
	  BioCEntity entityAnnotation = (BioCEntity)annotation;
	  for (Entity entity: entityAnnotation.getEntitySet()) {
	    if (annotationMap.containsKey(textAnnot.genKey())) {
	      // textAnnot = annotationMap.get(textAnnot.genKey());
	      // textAnnot.addToReferenceList(Brat.generateReferenceList(entity));
	    } else {
	      textAnnot.setReferenceSet(Brat.generateReferenceSet(entity));
	      annotationMap.put(textAnnot.genKey(), textAnnot);
	    }
	  }
	} else {
	  annotationMap.put(textAnnot.genKey(), textAnnot);
	}
      }
    }
    Set<TextBoundAnnotation> annotationSet = new HashSet<TextBoundAnnotation>(annotationMap.values());
    writeAnnotationSet(recognizerName, writer, annotationSet);
  } /* listEntities */

  public static void writeAnnotationList(String recognizerName,
					 PrintWriter writer,
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
    writeAnnotationSet(recognizerName, writer, annotationSet);
  } /* listEntities */

  public void setTextLabel(String newLabel) {
    this.textLabel = newLabel;
  }

  public void entityListFormatter(PrintWriter writer,
			   List<Entity> entityList) {
    writeAnnotationList(this.textLabel, writer, entityList);
  }

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList,
				  String annotationTypeName) {
    writeAnnotationList(annotationTypeName, writer, entityList);
  }

  
  public static String annotationSetToString(String recognizerName,
					   Set<TextBoundAnnotation> annotationSet) {
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
    return annotationSetToString(recognizerName, annotationSet);
  } /* listEntities */
  

  public String entityListFormatToString(List<Entity> entityList) {
    return annotationListToString(this.textLabel, entityList);
  }

  public String entityListFormatToString(List<Entity> entityList,
					 String annotationTypeName) {
    return annotationListToString(annotationTypeName, entityList);
  }


  public void initProperties(Properties properties) {
       if (properties.containsKey("metamaplite.brat.typename")) {
      bratEntityTypeName = properties.getProperty("metamaplite.brat.typename");
    }
  }
}
