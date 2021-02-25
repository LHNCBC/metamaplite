package examples;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.BioCSentence;

import gov.nih.nlm.nls.metamap.document.BioCDocumentLoader;
import gov.nih.nlm.nls.metamap.document.FreeText;
import gov.nih.nlm.nls.metamap.lite.EntityLookup5;
import gov.nih.nlm.nls.metamap.lite.EntityLookup;
import gov.nih.nlm.nls.metamap.lite.SentenceAnnotator;
import gov.nih.nlm.nls.metamap.lite.SentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPSentenceExtractor;
import gov.nih.nlm.nls.metamap.lite.OpenNLPPoSTagger;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;
import gov.nih.nlm.nls.metamap.prefix.ERToken;
import gov.nih.nlm.nls.metamap.prefix.Scanner;
import gov.nih.nlm.nls.ner.MetaMapLite;

/**
 * Describe class EntityLookup5Client here.
 *
 *
 * Created: Thu Mar 23 09:18:12 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class EntityLookup5Client {

  Properties myProperties;
  SentenceAnnotator sentenceAnnotator;
  SentenceExtractor sentenceExtractor;
  /** the current value in installdir expects the program to be run
   * from the public_mm_lite directory. */
  String installdir = ".";

  /**
   * Creates a new <code>EntityLookup5Client</code> instance.
   *
   */
  public EntityLookup5Client() {
    try {
      this.myProperties = MetaMapLite.getDefaultConfiguration();
      MetaMapLite.expandModelsDir(myProperties, installdir + "/data/models");
      MetaMapLite.expandIndexDir(myProperties, installdir + "/data/ivf/2017AA/USAbase/strict");
      myProperties.setProperty("metamaplite.excluded.termsfile",
			       installdir + "/data/specialterms.txt");
      FileReader fr = new FileReader(installdir + "/config/metamaplite.properties");
      myProperties.load(fr);
      fr.close();
      myProperties.list(System.out);
      this.sentenceAnnotator = new OpenNLPPoSTagger(myProperties);
      this.sentenceExtractor = new OpenNLPSentenceExtractor(myProperties);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }


  Set<Entity> explore5(BioCDocument document, Properties properties)
    throws IOException
  {
    String docid = "000000.tx";
    Set<String> semTypeRestrictSet = new HashSet();
    Set<String> sourceRestrictSet = new HashSet();
    semTypeRestrictSet.add("all");
    sourceRestrictSet.add("all");
    Set<Entity> entitySet = new HashSet<Entity>();
    EntityLookup5 entityLookup = new EntityLookup5(properties);
    for (BioCPassage passage: document.getPassages()) {
      this.sentenceExtractor.createSentences(passage);
      List<Entity> entityList = 
	entityLookup.processPassage(docid, passage, false,
				    semTypeRestrictSet, sourceRestrictSet);
      entitySet.addAll(entityList);

    }
    return entitySet;
  }

  /**
   * 
   *
   * @param args command line arguments
   * @throws IOException I/O exception
   */
  public static final void main(final String[] args)
    throws IOException
  {
    EntityLookup5Client inst = new EntityLookup5Client();
    BioCDocumentLoader docLoader = new FreeText();
    Set<Entity> entitySet = new HashSet<Entity>();
    
    BioCDocument document = FreeText.instantiateBioCDocument("FDA has strengthened the warning for the type 2 diabetes medicine canagliflozin (Invokana, Invokamet) related to the increased risk of bone fractures, and added new information about decreased bone mineral density. To address these safety concerns, FDA added a new Warning and Precaution and revised the Adverse Reactions section of the Invokana and Invokamet drug labels.");
    List<BioCDocument> documentList = new ArrayList<BioCDocument>();
    documentList.add(document);
    for (BioCDocument doc: documentList) {
      entitySet.addAll(inst.explore5(doc, inst.myProperties));
    }
    for (Entity entity: entitySet) {
      System.out.println(entity);

    }
  }
}
