package gov.nih.nlm.nls.metamap.dfbuilder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import gov.nih.nlm.nls.metamap.dfbuilder.Mrconso;

/**
 * Generates a term to treecodes table for MetaMapLite
 * inputs MRCONSO.RRF, MRSAT.RRF
 *
 * Load MRSAT table into map keyed by CUI, keeping only MeSH records
 * with ATN field with value "MN".
 *
 * Create treecode file from MRCONSO table using previously generated
 * CUI to MRSAT dictionary map to create records of form:
 * <pre>
 * (131)I-Macroaggregated Albumin|x.x.x.x
 * (131)I-MAA|x.x.x.x
 * 1,2-Dipalmitoylphosphatidylcholine|D10.570.755.375.760.400.800.224
 * 1,2 Dipalmitoylphosphatidylcholine|D10.570.755.375.760.400.800.224
 * 1,2-Dihexadecyl-sn-Glycerophosphocholine|D10.570.755.375.760.400.800.224
 * 1,2 Dihexadecyl sn Glycerophosphocholine|D10.570.755.375.760.400.800.224
 * 1,2-Dipalmitoyl-Glycerophosphocholine|D10.570.755.375.760.400.800.224
 * 1,2 Dipalmitoyl Glycerophosphocholine|D10.570.755.375.760.400.800.224
 * Dipalmitoylphosphatidylcholine|D10.570.755.375.760.400.800.224
 * Dipalmitoylglycerophosphocholine|D10.570.755.375.760.400.800.224
 * ...
 * </pre>
 *
 * Created: Wed Nov 27 10:51:29 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class ExtractTreecodes {

  /**
   * Given list of Mesh MRSAT records with ATN field = "MN", create a
   * map of treecodes (in ATV field) by cui
   * @param mrsatFilename
   * @return Map of treecodelists by cui
   */
  Map<String,List<String>> generateCuiToTreecodeMap(String mrsatFilename) {
    int mrsatRecordCnt = 0;
    Map<String,List<String>> cuiToAtvMap = new HashMap<String,List<String>>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(mrsatFilename));
      String line;
      while ((line = br.readLine()) != null) {
	String[] fieldlist = line.split("\\|");
	if (fieldlist.length > 11) {
	  Mrsat record = new Mrsat(fieldlist);
	  if (record.getAtn().equals("MN")) {
	    mrsatRecordCnt++;
	    if (cuiToAtvMap.containsKey(record.getCui())) {
	      cuiToAtvMap.get(record.getCui()).add(record.getAtv());
	    } else {
	      List<String> treecodeList = new ArrayList();
	      treecodeList.add(record.getAtv());
	      cuiToAtvMap.put(record.getCui(), treecodeList);
	    }
	  }
	}
      }
    System.out.println("size of MeSH MRSAT list: " + mrsatRecordCnt);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return cuiToAtvMap;
  }

  /**
   * Write term|treecode list to file.
   * @param filename name of term|treecode output file
   * @param mrconsoFilename name of UMLS MRCONSO file 
   * @param cuiTreecodeMap cui to MeSH Treecode Map
   */
  public void writeTermTreecodeListToFile(String treecodeFilename, 
					  String mrconsoFilename,
					  Map<String,List<String>> cuiTreecodeMap)
  {
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(treecodeFilename));
      BufferedReader br = new BufferedReader(new FileReader(mrconsoFilename));
      int meshMrconsoRecordCnt = 0;
      int termTreecodeRecordCnt = 0;
      String line;
      while ((line = br.readLine()) != null) {
	Mrconso mrconso = new Mrconso(line.split("\\|"));
	if (mrconso.getSab().equals("MSH")) {
	  meshMrconsoRecordCnt++;
	  if (cuiTreecodeMap.containsKey(mrconso.getCui())) {
	    for (String treecode: cuiTreecodeMap.get(mrconso.getCui())) {
	      String entry = mrconso.getStr() + "|" + treecode;
	      pw.println(entry);
	      termTreecodeRecordCnt++;
	    }
	  } else {
	    String entry = mrconso.getStr() + "|" + "x.x.x.x";
	    pw.println(entry);
	    termTreecodeRecordCnt++;
	  }
	}
      }
      br.close();
      pw.close();
      System.out.println("size of MeSH MRCONSO list: " + meshMrconsoRecordCnt);
      System.out.println("size of term -> treecode list: " + termTreecodeRecordCnt);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Read MRCONSO and MRSAT files and write generated Term/Treecode file.
   * @param mrconsoFilename UMLS MRCONSO table filename
   * @param mrsatFilename UMLS MRSAT table filename
   * @param treecodeFilename name of term|treecode output file
   */
  public static void process(String mrconsoFilename,
			     String mrsatFilename,
			     String treecodeFilename)
    
  {
    System.out.println("Processing " +
		       mrconsoFilename + " + " +
		       mrsatFilename + " --> " +
		       treecodeFilename + ".");

    ExtractTreecodes inst = new ExtractTreecodes();
    Map<String,List<String>> cuiTreecodeMap = inst.generateCuiToTreecodeMap(mrsatFilename);
    System.out.println("size of cui -> treecode dictionary: " + cuiTreecodeMap.size());
    inst.writeTermTreecodeListToFile(treecodeFilename, mrconsoFilename, cuiTreecodeMap);
  }

  
  /**
   * Main - handle command line arguments and run process.
   *
   * @param args command line arguments, a <code>String</code> value
   */
  public static final void main(final String[] args) {
    if (args.length > 2) {
      String mrconsofn = args[0];
      String mrsatfn = args[1];
      String treecodefn = args[2];
      process(mrconsofn, mrsatfn, treecodefn);
    } else {
      System.out.println("usage: gov.nih.nlm.nls.metamap.dfbuilder.ExtractTreecodes mrconso mrsat treecodetable");
    }
  }

}
