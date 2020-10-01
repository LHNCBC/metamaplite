package gov.nih.nlm.nls.metamap.lite.mapdb;

import org.mapdb.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.stream.Collectors;

import gov.nih.nlm.nls.metamap.prefix.Token;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.TermInfo;
import gov.nih.nlm.nls.metamap.lite.TermInfoImpl;
import gov.nih.nlm.nls.metamap.lite.SpecialTerms;
import gov.nih.nlm.nls.metamap.lite.dictionary.MMLDictionaryLookup;
import gov.nih.nlm.nls.metamap.lite.NormalizedStringCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describe class MapDbDictionaryLookup here.
 *
 *
 * Created: Wed May 22 14:34:24 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class MapDbLookup implements MMLDictionaryLookup<TermInfo> {
  private static final Logger logger = LoggerFactory.getLogger(MapDbLookup.class);
  
  HTreeMap cuiConceptCuiMap;
  HTreeMap cuiSourceInfoStrMap;
  HTreeMap cuiSourceInfoCuiMap;
  HTreeMap cuiSemanticTypeCuiMap;
  HTreeMap variantMap;
  HTreeMap treecodeMap;
  SpecialTerms excludedTerms;

  public final String defaultDbFilename = "data/mapdb/strict.db";

  // constructor
    public MapDbLookup() {
      
    }
  public MapDbLookup(DB db) {
    this.cuiConceptCuiMap = db.hashMap("cuiconcept.cui").open();
    this.cuiSourceInfoStrMap = db.hashMap("cuisourceinfo.str").open();
    this.cuiSourceInfoCuiMap = db.hashMap("cuisourceinfo.cui").open();
    this.cuiSemanticTypeCuiMap = db.hashMap("cuisemantictype.cui").open();
    this.excludedTerms = new SpecialTerms();
  }

  public void init(Properties properties) {
    String indexdir = properties.getProperty("metamaplite.index.directory", ".");
    String dbFilename = indexdir + "/thedataset.db";
    DB db = DBMaker.fileDB(dbFilename).readOnly().make();
    this.cuiConceptCuiMap = db.hashMap("cuiconcept.cui").open();
    this.cuiSourceInfoStrMap = db.hashMap("cuisourceinfo.str").open();
    this.cuiSourceInfoCuiMap = db.hashMap("cuisourceinfo.cui").open();
    this.cuiSemanticTypeCuiMap = db.hashMap("cuisemantictype.cui").open();
    this.excludedTerms = new SpecialTerms();
    try {
      if (properties.containsKey("metamaplite.excluded.termsfile")) {
	this.excludedTerms.addTerms(properties.getProperty("metamaplite.excluded.termsfile"));
      } else if (System.getProperty("metamaplite.excluded.termsfile") != null) {
	this.excludedTerms.addTerms(System.getProperty("metamaplite.excluded.termsfile"));
      }
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public Set<ConceptInfo> lookupTermConceptInfo(String originalTerm, String normTerm)
  {
    Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
    // for (String doc: this.cuiSourceInfoCuiMap.get(normTerm)) {
    //   String[] fields = doc.split("\\|");
    //   String cui = fields[0];
    //   String docStr = fields[3];
      
    // If term is not in excluded term list and term or
    // normalized form of term matches lookup string or
    // normalized form of lookup string then get
    // information about lookup string.
    // if (! excludedTerms.isExcluded(cui,normTerm)) {
    // 	ConceptInfo conceptInfo = new ConceptInfo(cui,
    // 						  this.cuiPreferredNameCache.findPreferredName(cui),
    // 						  docStr,
    // 						  this.cuiSourceSetIndex.getSourceSet(cui),
    // 						  this.cuiSemanticTypeSetIndex.getSemanticTypeSet(cui));
    // 	conceptInfoSet.add(conceptInfo);
    // }
    //    }
    return conceptInfoSet;
  }

  public String getPreferredName(String cui) {
    for (String[] fields: (List<String[]>)this.cuiConceptCuiMap.get(cui)) {
      return fields[1];
    }
    return null;
  }

  public Set<String> getSourceSet(String cui) {
    Set<String> sourceSet = new HashSet<String>();
    for (String[] fields: (List<String[]>)this.cuiSourceInfoCuiMap.get(cui)) {
      sourceSet.add(fields[4]);
    }
    return sourceSet;
  }
  
  public Set<String> getSemanticTypeSet(String cui) {
    Set<String> semTypeSet = new HashSet<String>();
    for (String[] fields: (List<String[]>)this.cuiSemanticTypeCuiMap.get(cui)) {
      semTypeSet.add(fields[4]);
    }
    return semTypeSet;
  }

  public int lookupVariant(String term, String word) {
    /* lookup term variants */
    /* if word is in variant list return varlevel (column 4)*/
    int variance = 9;		// maximum variance (should this value be larger?)
    for (String[] varFields: (List<String[]>)this.variantMap.get(term.toLowerCase())) {
      if ((varFields[2].toLowerCase().equals(word.toLowerCase())) ||
	  (varFields[2].toLowerCase().equals(term.toLowerCase()))) {
	variance = Integer.parseInt(varFields[4]); // use varlevel field
	logger.debug("*varFields: " + Arrays.stream(varFields).map(i -> i.toString()).collect(Collectors.joining("|")));
      } else {
	logger.debug(" varFields: " + Arrays.stream(varFields).map(i -> i.toString()).collect(Collectors.joining("|")));
      }
    }
    return variance;
  }

  public int lookupVariant(String term) {
    int variance = 9;		// maximum variance (should this value be larger?)
    logger.debug("term: " + term);
    for (String[] varFields: (List<String[]>)this.variantMap.get(term)) {
      if ((varFields[2].toLowerCase().equals(term.toLowerCase()))) {
	variance = Integer.parseInt(varFields[4]); // use varlevel field
	logger.debug("*varFields: " + Arrays.stream(varFields).map(i -> i.toString()).collect(Collectors.joining("|")));
      } else {
	logger.debug(" varFields: " + Arrays.stream(varFields).map(i -> i.toString()).collect(Collectors.joining("|")));
      }
    }
    return variance;
  }

  public List<String> lookupTreecodes(String term) {
    List<String> treecodeList = new ArrayList<String>();
    for (String[] fields: (List<String[]>)this.treecodeMap.get(term)) {
      treecodeList.add(fields[1]);
    }
    return treecodeList;
  }

  // Implementation of gov.nih.nlm.nls.metamap.lite.DictionaryLookup
  public TermInfoImpl lookup(String originalTerm) {
    String normTerm = NormalizedStringCache.normalizeString(originalTerm);
    Set<ConceptInfo> conceptInfoSet = new HashSet<ConceptInfo>();
    if (this.cuiSourceInfoStrMap.containsKey(originalTerm)) {
      for (String[] record: (List<String[]>)this.cuiSourceInfoStrMap.get(originalTerm) ) {
	conceptInfoSet.add(new ConceptInfo(record[0],
					    "",
					    record[3],
					    new HashSet<String>(),
					    new HashSet<String>()));
      }
    }
    
    if (conceptInfoSet != null) {
      return new TermInfoImpl<List<ConceptInfo>>(originalTerm,
						 normTerm,
						 new ArrayList<ConceptInfo>(conceptInfoSet));
    } 
    return null;
  }

  public static void main(String[] args)
    throws FileNotFoundException, IOException {
    String dbDir =
      "/net/lhcdevfiler/vol/cgsb5/ind/II_Group_WorkArea/wjrogers/Projects/metamaplite/data/mapdb/pubchem-mesh";
    String dbFilename = dbDir + "/thedataset.db";
    DB db = DBMaker.fileDB(dbFilename).readOnly().make();
    MapDbLookup inst = new MapDbLookup(db);
    System.out.println(inst.lookup("isofregenedol"));
    System.out.println(inst.lookup("1-((5-bromo-2-benzofuranyl)phenylmethyl)-1H-imidazole"));
    System.out.println(inst.lookup("2-hydroxy-N-(4-nitrophenyl)naphthalene-1-carboxamide"));
    System.out.println(inst.lookup("ag 1879"));
    if (args.length > 0) {
      String input = String.join(" ", args);
      System.out.println(inst.lookup(input));
    }
    db.close();      
  }

  public boolean verifyImplementation(String directoryPath) {
    if (new File(directoryPath + "/thedataset.db").exists()) {
      // should check for presence of cuisourceinfo, cuiconcept, and cuist maps
      return true;
    } else {
      return false;
    }
  }
}

  
