
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

import opennlp.tools.dictionary.serializer.Entry;

/**
 *
 */

public class Brat implements ResultFormatter {

  static class TextBoundAnnotation {
    String id;
    String type;
    int startOffset;
    int endOffset;
    String text;     
    List<NormalizationAnnotation> referenceList = null;

    public TextBoundAnnotation(String id, String type, int start, int end, String text) {
       this.id = id; 
       this.type = type;
       this.startOffset = start;
       this.endOffset = end;
       this.text = text;
    }
    public TextBoundAnnotation(String id, String type, int start, int end, String text, 
			       List<NormalizationAnnotation> referenceList) {
       this.id = id; 
       this.type = type;
       this.startOffset = start;
       this.endOffset = end;
       this.text = text;
       this.referenceList = referenceList;
    }
    public void setId(String id) { this.id = id; }
    public void setReferenceList(List<NormalizationAnnotation> referenceList) { 
      this.referenceList = referenceList;
    }
    public void addToReferenceList(List<NormalizationAnnotation> referenceList) { 
      this.referenceList.addAll(referenceList);
    }
    public List<NormalizationAnnotation> getReferenceList() { return this.referenceList; } 
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
    List<NormalizationAnnotation> referenceList = null;
    NormalizationAnnotation(String id, String target, String rid, String eid, String text) {
      this.id = id; 
      this.target = target;
      this.rid = rid;
      this.eid = eid;
      this.text = text;
    }
    NormalizationAnnotation(String id, String target, String rid, String eid, String text,
			    List<NormalizationAnnotation> referenceList) {
      this.id = id; 
      this.target = target;
      this.rid = rid;
      this.eid = eid;
      this.text = text;
       this.referenceList = referenceList;
    }
    public void setId(String id) { this.id = id; }
    public void setTarget(String target) { this.target = target; }
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

  public static List<NormalizationAnnotation> generateReferenceList(Entity entity) {
    List<NormalizationAnnotation> referenceList = new ArrayList<NormalizationAnnotation>();
    for (Ev ev: entity.getEvList()) {
      String cui = ev.getConceptInfo().getCUI();
      String preferredName = ev.getConceptInfo().getPreferredName();
      referenceList.add(new NormalizationAnnotation("N0","T0", "ConceptId", cui, preferredName));
      for (String semtype: ev.getConceptInfo().getSemanticTypeSet()) {
	referenceList.add(new NormalizationAnnotation("N0","T0", "SemanticType", semtype, semtype));
      }
    }
    return referenceList;
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
      System.out.println(annotation.toString());
      writer.println(annotation.toString());
      if (annotation.getReferenceList() != null) {
	for (NormalizationAnnotation nAnnotation: annotation.getReferenceList()) {
	  nindex++;
	  nAnnotation.setId("N" + nindex);
	  nAnnotation.setTarget(tid);
	  System.out.println(nAnnotation.toString());
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
      System.out.println(annotation.toString());
      writer.println(annotation.toString());
      if (annotation.getReferenceList() != null) {
	for (NormalizationAnnotation nAnnotation: annotation.getReferenceList()) {
	  nindex++;
	  nAnnotation.setId("N" + nindex);
	  nAnnotation.setTarget(tid);
	  System.out.println(nAnnotation.toString());
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
	    annotationSet.add(new TextBoundAnnotation("T0",recognizerName,start,end,term,Brat.generateReferenceList(entity)));
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
    String recognizerName = "debug";
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
	      textAnnot.setReferenceList(Brat.generateReferenceList(entity));
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
	  textAnnot.setReferenceList(Brat.generateReferenceList(entity));
	  annotationMap.put(textAnnot.genKey(), textAnnot);
	}
      }
    }
    Set<TextBoundAnnotation> annotationSet = new HashSet<TextBoundAnnotation>(annotationMap.values());
    writeAnnotationSet(recognizerName, writer, annotationSet);
  } /* listEntities */

  public void entityListFormatter(PrintWriter writer,
			   List<Entity> entityList) {
    writeAnnotationList("MMLite", writer, entityList);
  }

}
