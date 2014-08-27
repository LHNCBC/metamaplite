
//
package gov.nih.nlm.nls.tools;

import gov.nih.nlm.nls.metamap.prefix.Tokenize;

/**
 *
 */

public class TokenBasedReferenceTrieTest {
    public static void main(String[] args) {
    TokenBasedReferenceTrie t = new TokenBasedReferenceTrie();
    t.insert(Tokenize.mmPosTokenize("d(2) dopamine receptor", 0), new Integer(1813));
    t.insert(Tokenize.mmPosTokenize("d2dr", 0), new Integer(1813));
    t.insert(Tokenize.mmPosTokenize("d2r", 0), new Integer(33007));
    t.insert(Tokenize.mmPosTokenize("Fas antigen", 0), new Integer(355));
    t.insert(Tokenize.mmPosTokenize("fas antigen", 0), new Integer(355));
    t.insert(Tokenize.mmPosTokenize("Fas ligand",  0), new Integer(356));
    t.insert(Tokenize.mmPosTokenize("fas ligand",  0), new Integer(356));
    t.insert(Tokenize.mmPosTokenize("dopamine d1 receptor", 0), new Integer(33007));
    // t.insert(Tokenize.mmPosTokenize("dopamine d2 receptor", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine d3 receptor", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine d4 receptor", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine d5 receptor", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine and camp-regulated neuronal phosphoprotein 32", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine beta-hydroxylase (dopamine beta-monooxygenase)", 0), new Integer(1621));
    // t.insert(Tokenize.mmPosTokenize("dopamine beta-hydroxylase", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine d2 receptor", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d1 interacting protein", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d1", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d1b", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d2 isoform", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d2", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d3", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d4", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d5 pseudogene 1", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d5 pseudogene 2", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d5", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d2 isoform", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor d2", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 1", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 2", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 3", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 4", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 5", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor regulating factor", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein 1", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein 3", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein 5", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein of 78 kda", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine responsive protein", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine-oxygenase", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine-responsive gene 1 protein", 0));
    // t.insert(Tokenize.mmPosTokenize("dopamine-specific sulfotransferase", 0));
    // t.insert(Tokenize.mmPosTokenize("drd2", 0));
    // t.insert(Tokenize.mmPosTokenize("seven transmembrane helix receptor", 0));
    
    System.out.println("dopamine beta-hydroxylase (dopamine beta-monooxygenase): " +
		       t.has(Tokenize.mmPosTokenize
			     ("dopamine beta-hydroxylase (dopamine beta-monooxygenase)", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize
							("dopamine beta-hydroxylase (dopamine beta-monooxygenase)", 0)));
    System.out.println("dopamine-responsive gene 1 protein: " +
		       t.has(Tokenize.mmPosTokenize("dopamine-responsive gene 1 protein", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("dopamine-responsive gene 1 protein", 0)));
    System.out.println("d2dr: " + t.has(Tokenize.mmPosTokenize("d2dr", 0)) + 
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("d2dr", 0)));
    System.out.println("aids: " + t.has(Tokenize.mmPosTokenize("aids", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("aids", 0)));
    System.out.println("fas ligand: " + t.has(Tokenize.mmPosTokenize("fas ligand", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("fas ligand", 0)));
    System.out.println("Fas ligand: " + t.has(Tokenize.mmPosTokenize("Fas ligand", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("Fas ligand", 0)));
    System.out.println("cytotoxicity of fas ligand: " + t.has(Tokenize.mmPosTokenize("cytotoxicity of fas ligand", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("cytotoxicity of fas ligand", 0)));
    System.out.println("Cytotoxicity of Fas ligand: " + t.has(Tokenize.mmPosTokenize("Cytotoxicity of Fas ligand", 0)) +
		       ", reference: " + t.hasReference(Tokenize.mmPosTokenize("Cytotoxicity of Fas ligand", 0)));
    System.out.println("of Fas ligand: " + t.has(Tokenize.mmPosTokenize("of Fas ligand", 0)));
    System.out.println("of Fas: " + t.has(Tokenize.mmPosTokenize("of Fas", 0)));

    System.out.println("has prefix for Fas: " + t.hasPrefix(Tokenize.mmPosTokenize("Fas", 0)));    
    System.out.println("has prefix for Fas ligand: " + t.hasPrefix(Tokenize.mmPosTokenize("Fas ligand", 0)));    
    System.out.println("has prefix for carbon: " + t.hasPrefix(Tokenize.mmPosTokenize("carbon", 0)));    
  }

}
