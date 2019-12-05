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
   * Load MRCONSO table, keeping only MeSH records
   * @param mrconsoFilename name of UMLS MRCONSO file 
   * @return list of MeSH MRCONSO records
   */
  List<Mrconso> loadMeshMrconsoRecords(String mrconsoFilename) {
    List<Mrconso> mrconsoList = new ArrayList<Mrconso>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(mrconsoFilename));
      String line;
      while ((line = br.readLine()) != null) {
	Mrconso record = new Mrconso(line.split("\\|"));
	if (record.getSab().equals("MSH")) {
	  mrconsoList.add(record);
	}
      }
      br.close();
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return mrconsoList;
  }
  
  /**
   * Load MRSAT table, keeping only MeSH records with ATN field with value "MN"
   * @param mrsatFilename name of UMLS MRSAT file 
   * @return list of MeSH MRSAT records
   */
  List<Mrsat> loadMeshMnRecords(String mrsatFilename) {
    List<Mrsat> mrsatList = new ArrayList<Mrsat>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(mrsatFilename));
      String line;
      while ((line = br.readLine()) != null) {
	String[] fieldlist = line.split("\\|");
	if (fieldlist.length > 11) {
	  Mrsat record = new Mrsat(fieldlist);
	  if (record.getSab().equals("MSH") /*&& record.getAtn().equals("MN")*/) {
	    mrsatList.add(record);
	  }
	}
      }
      br.close();
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return mrsatList;
  }

  /**
   * Given list of Mesh MRSAT records with ATN field = "MN", create a
   * map of treecodes (in ATV field) by cui
   * @param mrsatRecordlist list of mrost records
   * @return Map of treecodelists by cui
   */
  Map<String,List<String>> generateCuiToTreecodeMap(List<Mrsat> mrsatRecordlist) {
    Map<String,List<String>> cuiToAtvMap = new HashMap<String,List<String>>();
    for (Mrsat record: mrsatRecordlist) {
      if (record.getAtn().equals("MN")) {
	if (cuiToAtvMap.containsKey(record.getCui())) {
	  cuiToAtvMap.get(record.getCui()).add(record.getAtv());
	} else {
	  List<String> treecodeList = new ArrayList();
	  treecodeList.add(record.getAtv());
	  cuiToAtvMap.put(record.getCui(), treecodeList);
	}
      }
    }
    return cuiToAtvMap;
  }

  /** 
   * Generate strings of form: "term|treecode", mesh terms without
   * treecodes have treecode value "x.x.x.x".
   * @param meshMrconsoRecords list of MeSH MRCONSO records
   * @param cuiTreecodeMap cui to MeSH Treecode Map
   * @return list of term 
   */
  List<String> generateTermToTreecodeList(List<Mrconso> meshMrconsoRecords,
					  Map<String,List<String>> cuiTreecodeMap) {
    List<String> termTreecodeList = new ArrayList<String>();
    for (Mrconso mrconso: meshMrconsoRecords) {
      if (cuiTreecodeMap.containsKey(mrconso.getCui())) {
	for (String treecode: cuiTreecodeMap.get(mrconso.getCui())) {
	  termTreecodeList.add(mrconso.getStr() + "|" + treecode);
	}
      } else {
	termTreecodeList.add(mrconso.getStr() + "|" + "x.x.x.x");
      }
    }
    return termTreecodeList;
  }

  /**
   * Write term|treecode list to file.
   * @param filename name of term|treecode output file
   * @param termTreecodeList term|treecode list 
   */
  void writeTermTreecodeListToFile(String filename, List<String> termTreecodeList) {
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(filename));
      for (String entry: termTreecodeList) {
	pw.println(entry);
      }
      pw.close();
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
    ExtractTreecodes inst = new ExtractTreecodes();
    List<Mrconso> meshMrconsoRecords = inst.loadMeshMrconsoRecords(mrconsoFilename);
    System.out.println("size of MeSH MRCONSO list: " + meshMrconsoRecords.size());
    List<Mrsat> mrsatRecords = inst.loadMeshMnRecords(mrsatFilename);
    System.out.println("size of MeSH MRSAT list: " + mrsatRecords.size());
    System.out.flush();
    Map<String,List<String>> cuiTreecodeMap = inst.generateCuiToTreecodeMap(mrsatRecords);
    System.out.println("size of cui -> treecode dictionary: " + cuiTreecodeMap.size());
    List<String> termTreecodeList =
      inst.generateTermToTreecodeList(meshMrconsoRecords, cuiTreecodeMap);
    System.out.println("size of term -> treecode list: " + termTreecodeList.size());
    inst.writeTermTreecodeListToFile(treecodeFilename, termTreecodeList);
  }
  
  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String</code> value
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
