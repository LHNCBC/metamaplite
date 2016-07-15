
//
package gov.nih.nlm.nls.metamap.dfbuilder;

import java.util.Map;
import java.util.HashMap;

/**
 * A default mapping of Semantic Types Names in the UMLS to NLM's
 * semantic type abbreviations originally suggested by Tom Rindflesch.
 */

public class DefaultSemanticTypesRaw {

  static final Map<String,String> semtypeToStAbbrev = new HashMap<String,String>();
  static {
    semtypeToStAbbrev.put("Acquired Abnormality","acab");
    semtypeToStAbbrev.put("Activity","acty");
    semtypeToStAbbrev.put("Age Group","aggp");
    semtypeToStAbbrev.put("Amino Acid Sequence","amas");
    semtypeToStAbbrev.put("Amino Acid, Peptide, or Protein","aapp");
    semtypeToStAbbrev.put("Amphibian","amph");
    semtypeToStAbbrev.put("Anatomical Abnormality","anab");
    semtypeToStAbbrev.put("Anatomical Structure","anst");
    semtypeToStAbbrev.put("Animal","anim");
    semtypeToStAbbrev.put("Antibiotic","antb");
    semtypeToStAbbrev.put("Archaeon","arch");
    semtypeToStAbbrev.put("Bacterium","bact");
    semtypeToStAbbrev.put("Behavior","bhvr");
    semtypeToStAbbrev.put("Biologic Function","biof");
    semtypeToStAbbrev.put("Biologically Active Substance","bacs");
    semtypeToStAbbrev.put("Biomedical Occupation or Discipline","bmod");
    semtypeToStAbbrev.put("Biomedical or Dental Material","bodm");
    semtypeToStAbbrev.put("Bird","bird");
    semtypeToStAbbrev.put("Body Location or Region","blor");
    semtypeToStAbbrev.put("Body Part, Organ, or Organ Component","bpoc");
    semtypeToStAbbrev.put("Body Space or Junction","bsoj");
    semtypeToStAbbrev.put("Body Substance","bdsu");
    semtypeToStAbbrev.put("Body System","bdsy");
    semtypeToStAbbrev.put("Carbohydrate Sequence","crbs");
    semtypeToStAbbrev.put("Carbohydrate","carb");
    semtypeToStAbbrev.put("Cell Component","celc");
    semtypeToStAbbrev.put("Cell Function","celf");
    semtypeToStAbbrev.put("Cell or Molecular Dysfunction","comd");
    semtypeToStAbbrev.put("Cell","cell");
    semtypeToStAbbrev.put("Chemical Viewed Functionally","chvf");
    semtypeToStAbbrev.put("Chemical Viewed Structurally","chvs");
    semtypeToStAbbrev.put("Chemical","chem");
    semtypeToStAbbrev.put("Classification","clas");
    semtypeToStAbbrev.put("Clinical Attribute","clna");
    semtypeToStAbbrev.put("Clinical Drug","clnd");
    semtypeToStAbbrev.put("Conceptual Entity","cnce");
    semtypeToStAbbrev.put("Congenital Abnormality","cgab");
    semtypeToStAbbrev.put("Daily or Recreational Activity","dora");
    semtypeToStAbbrev.put("Diagnostic Procedure","diap");
    semtypeToStAbbrev.put("Disease or Syndrome","dsyn");
    semtypeToStAbbrev.put("Drug Delivery Device","drdd");
    semtypeToStAbbrev.put("Educational Activity","edac");
    semtypeToStAbbrev.put("Eicosanoid","eico");
    semtypeToStAbbrev.put("Element, Ion, or Isotope","elii");
    semtypeToStAbbrev.put("Embryonic Structure","emst");
    semtypeToStAbbrev.put("Entity","enty");
    semtypeToStAbbrev.put("Environmental Effect of Humans","eehu");
    semtypeToStAbbrev.put("Enzyme","enzy");
    semtypeToStAbbrev.put("Eukaryote","euka");
    semtypeToStAbbrev.put("Event","evnt");
    semtypeToStAbbrev.put("Experimental Model of Disease","emod");
    semtypeToStAbbrev.put("Family Group","famg");
    semtypeToStAbbrev.put("Finding","fndg");
    semtypeToStAbbrev.put("Fish","fish");
    semtypeToStAbbrev.put("Food","food");
    semtypeToStAbbrev.put("Fully Formed Anatomical Structure","ffas");
    semtypeToStAbbrev.put("Functional Concept","ftcn");
    semtypeToStAbbrev.put("Fungus","fngs");
    semtypeToStAbbrev.put("Gene or Gene Product","gngp");
    semtypeToStAbbrev.put("Gene or Genome","gngm");
    semtypeToStAbbrev.put("Genetic Function","genf");
    semtypeToStAbbrev.put("Geographic Area","geoa");
    semtypeToStAbbrev.put("Governmental or Regulatory Activity","gora");
    semtypeToStAbbrev.put("Group Attribute","grpa");
    semtypeToStAbbrev.put("Group","grup");
    semtypeToStAbbrev.put("Hazardous or Poisonous Substance","hops");
    semtypeToStAbbrev.put("Health Care Activity","hlca");
    semtypeToStAbbrev.put("Health Care Related Organization","hcro");
    semtypeToStAbbrev.put("Hormone","horm");
    semtypeToStAbbrev.put("Human","humn");
    semtypeToStAbbrev.put("Human-caused Phenomenon or Process","hcpp");
    semtypeToStAbbrev.put("Idea or Concept","idcn");
    semtypeToStAbbrev.put("Immunologic Factor","imft");
    semtypeToStAbbrev.put("Indicator, Reagent, or Diagnostic Aid","irda");
    semtypeToStAbbrev.put("Individual Behavior","inbe");
    semtypeToStAbbrev.put("Injury or Poisoning","inpo");
    semtypeToStAbbrev.put("Inorganic Chemical","inch");
    semtypeToStAbbrev.put("Intellectual Product","inpr");
    semtypeToStAbbrev.put("Laboratory Procedure","lbpr");
    semtypeToStAbbrev.put("Laboratory or Test Result","lbtr");
    semtypeToStAbbrev.put("Language","lang");
    semtypeToStAbbrev.put("Lipid","lipd");
    semtypeToStAbbrev.put("Machine Activity","mcha");
    semtypeToStAbbrev.put("Mammal","mamm");
    semtypeToStAbbrev.put("Manufactured Object","mnob");
    semtypeToStAbbrev.put("Medical Device","medd");
    semtypeToStAbbrev.put("Mental Process","menp");
    semtypeToStAbbrev.put("Mental or Behavioral Dysfunction","mobd");
    semtypeToStAbbrev.put("Molecular Biology Research Technique","mbrt");
    semtypeToStAbbrev.put("Molecular Function","moft");
    semtypeToStAbbrev.put("Molecular Sequence","mosq");
    semtypeToStAbbrev.put("Natural Phenomenon or Process","npop");
    semtypeToStAbbrev.put("Neoplastic Process","neop");
    semtypeToStAbbrev.put("Neuroreactive Substance or Biogenic Amine","nsba");
    semtypeToStAbbrev.put("Nucleic Acid, Nucleoside, or Nucleotide","nnon");
    semtypeToStAbbrev.put("Nucleotide Sequence","nusq");
    semtypeToStAbbrev.put("Object","objt");
    semtypeToStAbbrev.put("Occupation or Discipline","ocdi");
    semtypeToStAbbrev.put("Occupational Activity","ocac");
    semtypeToStAbbrev.put("Organ or Tissue Function","ortf");
    semtypeToStAbbrev.put("Organic Chemical","orch");
    semtypeToStAbbrev.put("Organism Attribute","orga");
    semtypeToStAbbrev.put("Organism Function","orgf");
    semtypeToStAbbrev.put("Organism","orgm");
    semtypeToStAbbrev.put("Organization","orgt");
    semtypeToStAbbrev.put("Organophosphorus Compound","opco");
    semtypeToStAbbrev.put("Pathologic Function","patf");
    semtypeToStAbbrev.put("Patient or Disabled Group","podg");
    semtypeToStAbbrev.put("Pharmacologic Substance","phsu");
    semtypeToStAbbrev.put("Phenomenon or Process","phpr");
    semtypeToStAbbrev.put("Physical Object","phob");
    semtypeToStAbbrev.put("Physiologic Function","phsf");
    semtypeToStAbbrev.put("Plant","plnt");
    semtypeToStAbbrev.put("Population Group","popg");
    semtypeToStAbbrev.put("Professional Society","pros");
    semtypeToStAbbrev.put("Professional or Occupational Group","prog");
    semtypeToStAbbrev.put("Qualitative Concept","qlco");
    semtypeToStAbbrev.put("Quantitative Concept","qnco");
    semtypeToStAbbrev.put("Receptor","rcpt");
    semtypeToStAbbrev.put("Regulation or Law","rnlw");
    semtypeToStAbbrev.put("Reptile","rept");
    semtypeToStAbbrev.put("Research Activity","resa");
    semtypeToStAbbrev.put("Research Device","resd");
    semtypeToStAbbrev.put("Self-help or Relief Organization","shro");
    semtypeToStAbbrev.put("Sign or Symptom","sosy");
    semtypeToStAbbrev.put("Social Behavior","socb");
    semtypeToStAbbrev.put("Spatial Concept","spco");
    semtypeToStAbbrev.put("Steroid","strd");
    semtypeToStAbbrev.put("Substance","sbst");
    semtypeToStAbbrev.put("Temporal Concept","tmco");
    semtypeToStAbbrev.put("Therapeutic or Preventive Procedure","topp");
    semtypeToStAbbrev.put("Tissue","tisu");
    semtypeToStAbbrev.put("Vertebrate","vtbt");
    semtypeToStAbbrev.put("Virus","virs");
    semtypeToStAbbrev.put("Vitamin","vita");
  }

  public static Map<String,String> getSemTypeToStAbbrevMap() {
    return semtypeToStAbbrev;
  }

}
