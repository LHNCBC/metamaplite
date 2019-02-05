
//
package gov.nih.nlm.nls.metamap.lite.resultformats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCAnnotation;
import bioc.BioCRelation;
import bioc.BioCSentence;

import gov.nih.nlm.nls.metamap.lite.types.Entity;
/**
 * CEM Format
 * pmid\tfield\tstart\tend\tterm\tshape
 * field: T - Title, A - Abstract
 *
 * Example of CEM format:
 * <pre>
 * 22006095	A	285	294	serotonin	TRIVIAL
 * 22006095	A	498	512	venlafaxine XR	TRIVIAL
 * 22006095	A	622	636	venlafaxine XR	TRIVIAL
 * 22006095	T	0	9	Serotonin	TRIVIAL
 * 22006095	T	79	93	venlafaxine XR	TRIVIAL
 * 22056334	A	1002	1010	p,p'-DDE	SYSTEMATIC
 * 22056334	A	1016	1029	methylmercury	SYSTEMATIC
 * 22056334	A	1173	1203	polybrominated diphenyl ethers	FAMILY
 * </pre>
 */

public class CEMPlusFormat implements ResultFormatter {

  NumberFormat scoreFormat = NumberFormat.getInstance();

  public CEMPlusFormat() {
    scoreFormat.setMaximumFractionDigits(2);
  }

  void entityFormatToString(StringBuilder sb, Entity entity) {
    sb.append(entity.getDocid()).append("\t")
      .append(entity.getFieldId() != null ? entity.getFieldId() : "F" ).append("\t")
      .append(entity.getStart()).append("\t")
      .append(entity.getStart() + entity.getLength()).append("\t")
      .append(entity.getMatchedText()).append("\t")
      .append("MetaMapLite\t")
      .append(scoreFormat.format(entity.getScore())).append("\t")
      .append(entity.getEvList().stream().map(i -> i.getConceptInfo().getCUI()).collect(Collectors.joining(",")));
  }

  public void entityListFormatter(PrintWriter writer,
				  List<Entity> entityList) {
    for (Entity entity: entityList) {
      StringBuilder sb = new StringBuilder();
      entityFormatToString(sb, entity);
      writer.println(sb.toString());
    }
  }
  
  public String entityListFormatToString(List<Entity> entityList) {
    StringBuilder sb = new StringBuilder();
    for (Entity entity: entityList) {
      entityFormatToString(sb, entity);
      sb.append("\n");
    }
    return sb.toString();
  }
  public void initProperties(Properties properties) {
  }

  public static void writeCEMAnnotations(PrintWriter writer, BioCSentence sentence)
    throws IOException
  {
    writeCEMAnnotations(writer, sentence);
  }

  public static void writeCEMAnnotations(PrintWriter writer, BioCDocument document) {
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

  public static void writeCEMAnnotations(PrintStream stream, BioCDocument document) 
    throws IOException
  {
    writeCEMAnnotations(new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream))), document);
  }

  public static void writeCEMAnnotations(String filename, BioCDocument document) 
    throws IOException
  {
    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    writeCEMAnnotations(pw, document);
    pw.close();
  }
}
