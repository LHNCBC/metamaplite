package gov.nih.nlm.nls.metamap.lite;

/**
 *
 */

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.OutputStreamWriter;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import bioc.BioCSentence;
import bioc.BioCAnnotation;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCLocation;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.BioCEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
/**
 *
 */
public class EntityAnnotation {
  private static final Logger logger = LoggerFactory.getLogger(EntityAnnotation.class);

  public static void displayEntitySet(Set<Entity> entitySet) {
    logger.debug("displayEntitySet");
    for (Entity entity: entitySet) {
      System.out.println(entity);
    }
  }

  public static BioCSentence displayEntitySet(BioCSentence sentence) {
    for (BioCAnnotation annotation: sentence.getAnnotations()) {
      if (annotation instanceof BioCEntity) {
	System.out.print(((BioCEntity)annotation).getEntitySet().toString());
	for (Map.Entry<String,String> entry: annotation.getInfons().entrySet()) {
	  System.out.print(entry.getKey() + ":" + entry.getValue() + "|");
	}
	System.out.println();
      } else {
	System.out.println(annotation);
      }
    }
    return sentence;
  }

  public static void writeEntities(PrintWriter writer, BioCDocument document) {
    int rindex = 0;
    for (BioCPassage passage: document.getPassages()) {
      for (BioCSentence sentence: passage.getSentences()) {
	for (BioCAnnotation annotation: sentence.getAnnotations()) {
	  writer.println(annotation.getText());
	  for (BioCLocation location: annotation.getLocations()) {
	    writer.println(	"|" + location );
	  }
	  if (annotation instanceof BioCEntity) {
	    BioCEntity bioCEntity = (BioCEntity)annotation;
	    for (Entity entity: bioCEntity.getEntitySet()) {
	      System.out.print(entity.toString());
	      writer.print(entity.toString());
	      for (Map.Entry<String,String> entry: annotation.getInfons().entrySet()) {
		System.out.print(entry.getKey() + ":" + entry.getValue() + "|");
		writer.print(entry.getKey() + ":" + entry.getValue() + "|");
	      }
	      System.out.println();
	      writer.println();
	    }
	  } else {
	    System.out.println(annotation);
	    writer.println(annotation);
	  }
	}
      }
    }    
  }

  public static void writeEntities(PrintStream stream, BioCDocument document)
  {
    writeEntities(new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream))), document);
  }

  public static void writeEntities(String filename, BioCDocument document) 
    throws IOException
  {
    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    writeEntities(pw, document);
    pw.close();
  }

  public static void writeBcEvaluateAnnotations(PrintWriter writer, BioCSentence sentence)
    throws IOException
  {
    writeBcEvaluateAnnotations(writer, sentence);
  }

  public static void writeBcEvaluateAnnotations(PrintWriter writer, BioCDocument document) {
    Set<String> termSet = new HashSet<String>();
    for (BioCPassage passage: document.getPassages()) {
      for (BioCSentence sentence: passage.getSentences()) {
	for (BioCAnnotation annotation: sentence.getAnnotations()) {
	  termSet.add(annotation.getText());
	}
      }
    }
    int rindex = 1;
    for (String term: termSet) {
      System.out.println(document.getID() + "\t" +
			 term + "\t" +
			 rindex + "\t" +
			 0.9);
      writer.println(document.getID() + "\t" +
		     term + "\t");
		     // rindex + "\t" +
		     // 0.9);
      rindex++;
    }
  }

  public static void writeBcEvaluateAnnotations(PrintStream stream, BioCDocument document) 
    throws IOException
  {
    writeBcEvaluateAnnotations(new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream))), document);
  }

  public static void writeBcEvaluateAnnotations(String filename, BioCDocument document) 
    throws IOException
  {
    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    writeBcEvaluateAnnotations(pw, document);
    pw.close();
  }


}
