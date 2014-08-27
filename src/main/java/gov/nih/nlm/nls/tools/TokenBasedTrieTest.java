
//
package gov.nih.nlm.nls.tools;

import gov.nih.nlm.nls.metamap.prefix.Tokenize;

/**
 *
 */

public class TokenBasedTrieTest {
  
  public static void main(String[] args) {
    TokenBasedTrie t = new TokenBasedTrie();
    t.insert(Tokenize.mmPosTokenize("d(2) dopamine receptor", 0));
    t.insert(Tokenize.mmPosTokenize("d2dr", 0));
    t.insert(Tokenize.mmPosTokenize("d2r", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine d1 receptor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine d2 receptor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine d3 receptor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine d4 receptor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine d5 receptor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine and camp-regulated neuronal phosphoprotein 32", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine beta-hydroxylase (dopamine beta-monooxygenase)", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine beta-hydroxylase", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine d2 receptor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d1 interacting protein", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d1", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d1b", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d2 isoform", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d2", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d3", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d4", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d5 pseudogene 1", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d5 pseudogene 2", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d5", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d2 isoform", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor d2", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 1", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 2", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 3", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 4", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein 5", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor interacting protein", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor regulating factor", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein 1", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein 3", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein 5", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine receptor-interacting protein of 78 kda", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine responsive protein", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine-oxygenase", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine-responsive gene 1 protein", 0));
    t.insert(Tokenize.mmPosTokenize("dopamine-specific sulfotransferase", 0));
    t.insert(Tokenize.mmPosTokenize("drd2", 0));
    t.insert(Tokenize.mmPosTokenize("seven transmembrane helix receptor", 0));
    
    System.out.println("dopamine beta-hydroxylase (dopamine beta-monooxygenase): " +
		       t.has(Tokenize.mmPosTokenize
			     ("dopamine beta-hydroxylase (dopamine beta-monooxygenase)", 0)));
    System.out.println("dopamine-responsive gene 1 protein: " +
		       t.has(Tokenize.mmPosTokenize("dopamine-responsive gene 1 protein", 0)));
    System.out.println("d2dr: " + t.has(Tokenize.mmPosTokenize("d2dr", 0)));
    System.out.println("aids: " + t.has(Tokenize.mmPosTokenize("aids", 0)));
    System.out.println("ai: " + t.hasPrefix(Tokenize.mmPosTokenize("ai", 0)));
    System.out.println("dopamine: " + t.hasPrefix(Tokenize.mmPosTokenize("dopamine", 0)));
    System.out.println("d: " + t.hasPrefix(Tokenize.mmPosTokenize("d", 0)));
    System.out.println("d(2): " + t.hasPrefix(Tokenize.mmPosTokenize("d(2)", 0)));
    System.out.println("d(2) dopamine: " + t.hasPrefix(Tokenize.mmPosTokenize("d(2) dopamine", 0)));
    System.out.println("dopamine responsive: " + t.hasPrefix(Tokenize.mmPosTokenize("dopamine responsive", 0)));
  }
}

