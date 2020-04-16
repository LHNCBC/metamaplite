package gov.nih.nlm.nls.nlp.nlsstrings;

import java.util.*;

/**
 * Provide miscellaneous string manipulation routines.
 * <p>
 * An java implementation of nls_strings.pl.
 * </p>
 * Created: Fri Oct  5 16:13:28 2001
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version $Id: NLSStrings.java,v 1.3 2005/09/21 18:07:58 wrogers Exp $
 */

public final class NLSStrings {

  static final String[] left_parenthetical = { 
    "[X]", "[V]", "[D]", "[M]", "[EDTA]", "[SO]", "[Q]"
  };

  /** nos patterns, precedence of pattern is important. */
  static final String[] nos_string =  {
    ", nos", "; nos", " nos", " - (nos)", 
    " (nos)", "/nos", "_nos", ",nos", "-nos",
  };

  static final String[] nosStrings = {
    ", NOS",
    // "; NOS",
    " - NOS",
    " NOS",
    ".NOS",
    " - (NOS)",
    " (NOS)",
    "/NOS",
    "_NOS",
    ",NOS",
    "-NOS",
    ")NOS"
  };

  /** nos expansion patterns, precedence of pattern is important. */
  static final String[] nos_expansion_string =  {
    ", not otherwise specified", 
    "; not otherwise specified",
    ", but not otherwise specified",
    " but not otherwise specified",
    " not otherwise specified",
    ", not elsewhere specified",
    "; not elsewhere specified",
    " not elsewhere specified",
    "not elsewhere specified"
  };

  static final String[] abgn_forms = {
    "ANB-NOS", 
    "ANB NOS",
    "C NOS",
    "CL NOS",
    // is this right?
    // C0410315|ENG|s|L0753752|PF|S0970087|Oth.inf.+bone dis-NOS|  
    "NOS AB", 
    "NOS AB:ACNC:PT:SER^DONOR:ORD:AGGL", 
    "NOS-ABN", 
    "NOS ABN",
    "NOS-AG", 
    "NOS AG",
    "NOS ANB", 
    "NOS-ANTIBODY", 
    "NOS ANTIBODY",
    "NOS-ANTIGEN",
    "NOS ANTIGEN",
    "NOS GENE",
    "NOS NRM", 
    "NOS PROTEIN", 
    "NOS-RELATED ANTIGEN", 
    "NOS RELATED ANTIGEN",
    "NOS1 GENE PRODUCT",
    "NOS2 GENE PRODUCT",
    "NOS3 GENE PRODUCT",
    "NOS MARGN",
  };

  /** init preposition and conjunctions */
  static Map prepOrConj = new HashMap(102);

  static {
    // init preposition and conjunctions
    prepOrConj.put("aboard", "aboard");
    prepOrConj.put("about", "about");
    prepOrConj.put("across", "across");
    prepOrConj.put("after", "after");
    prepOrConj.put("against", "against");
    prepOrConj.put("aka", "aka");
    prepOrConj.put("albeit", "albeit");
    prepOrConj.put("along", "along");
    prepOrConj.put("alongside", "alongside");
    prepOrConj.put("although", "although");
    prepOrConj.put("amid", "amid");
    prepOrConj.put("amidst", "amidst");
    prepOrConj.put("among", "among");
    prepOrConj.put("amongst", "amongst");
    prepOrConj.put("and", "and");
    // prepOrConj.put("anti", "anti");
    prepOrConj.put("around", "around");
    prepOrConj.put("as", "as");
    prepOrConj.put("astride", "astride");
    prepOrConj.put("at", "at");
    prepOrConj.put("atop", "atop");
    // prepOrConj.put("bar", "bar");
    prepOrConj.put("because", "because");
    prepOrConj.put("before", "before");
    prepOrConj.put("beneath", "beneath");
    prepOrConj.put("beside", "beside");
    prepOrConj.put("besides", "besides");
    prepOrConj.put("between", "between");
    prepOrConj.put("but", "but");
    prepOrConj.put("by", "by");
    prepOrConj.put("circa", "circa");
    prepOrConj.put("contra", "contra");
    prepOrConj.put("despite", "despite");
    // prepOrConj.put("down", "down");
    prepOrConj.put("during", "during");
    prepOrConj.put("ex", "ex");
    prepOrConj.put("except", "except");
    prepOrConj.put("excluding", "excluding");
    prepOrConj.put("failing", "failing");
    prepOrConj.put("following", "following");
    prepOrConj.put("for", "for");
    prepOrConj.put("from", "from");
    prepOrConj.put("given", "given");
    prepOrConj.put("if", "if");
    prepOrConj.put("in", "in");
    prepOrConj.put("inside", "inside");
    prepOrConj.put("into", "into");
    prepOrConj.put("less", "less");
    prepOrConj.put("lest", "lest");
    // prepOrConj.put("like", "like");
    // prepOrConj.put("mid", "mid");
    prepOrConj.put("minus", "minus");
    // prepOrConj.put("near", "near");
    prepOrConj.put("nearby", "nearby");
    prepOrConj.put("neath", "neath");
    prepOrConj.put("nor", "nor");
    prepOrConj.put("notwithstanding", "notwithstanding");
    prepOrConj.put("of", "of");
    // prepOrConj.put("off", "off");
    prepOrConj.put("on", "on");
    prepOrConj.put("once", "once");
    // prepOrConj.put("only", "only");
    prepOrConj.put("onto", "onto");
    prepOrConj.put("or", "or");
    // prepOrConj.put("out", "out");
    // prepOrConj.put("past", "past");
    prepOrConj.put("pending", "pending");
    prepOrConj.put("per", "per");
    // prepOrConj.put("plus", "plus");
    prepOrConj.put("provided", "provided");
    prepOrConj.put("providing", "providing");
    prepOrConj.put("regarding", "regarding");
    prepOrConj.put("respecting", "respecting");
    // prepOrConj.put("round", "round");
    prepOrConj.put("sans", "sans");
    prepOrConj.put("sensu", "sensu");
    prepOrConj.put("since", "since");
    prepOrConj.put("so", "so");
    prepOrConj.put("suppose", "suppose");
    prepOrConj.put("supposing", "supposing");
    prepOrConj.put("than", "than");
    prepOrConj.put("though", "though");
    prepOrConj.put("throughout", "throughout");
    prepOrConj.put("to", "to");
    prepOrConj.put("toward", "toward");
    prepOrConj.put("towards", "towards");
    prepOrConj.put("under", "under");
    prepOrConj.put("underneath", "underneath");
    prepOrConj.put("unless", "unless");
    prepOrConj.put("unlike", "unlike");
    prepOrConj.put("until", "until");
    prepOrConj.put("unto", "unto");
    prepOrConj.put("upon", "upon");
    prepOrConj.put("upside", "upside");
    prepOrConj.put("versus", "versus");
    prepOrConj.put("vs", "vs");
    prepOrConj.put("w", "w");
    prepOrConj.put("wanting", "wanting");
    prepOrConj.put("when", "when");
    prepOrConj.put("whenever", "whenever");
    prepOrConj.put("where", "where");
    prepOrConj.put("whereas", "whereas");
    prepOrConj.put("wherein", "wherein");
    prepOrConj.put("whereof", "whereof");
    prepOrConj.put("whereupon", "whereupon");
    prepOrConj.put("wherever", "wherever");
    prepOrConj.put("whether", "whether");
    prepOrConj.put("while", "while");
    prepOrConj.put("whilst", "whilst");
    prepOrConj.put("with", "with");
    prepOrConj.put("within", "within");
    prepOrConj.put("without", "without");
    // prepOrConj.put("worth", "worth");
    prepOrConj.put("yet", "yet");
  }

  /**
   * removes all left parentheticals. 
   * @param string source string.
   * @return string with left parentheticals removed.
   */
  public static String removeLeftParentheticals(String string)
  {
    for (int i = 0; i < left_parenthetical.length; i++) {
      if (string.indexOf(left_parenthetical[i]) == 0) {
	return string.substring(left_parenthetical[i].length(), 
				string.length());
      }
    }
    return removeExtraBlanks(string);
  }
  
  /**
   * does string contains prepostion or conjunction?
   * @param string source string.
   * @return true if string contains prepostion or conjunction.
   */
  public static boolean containsPrepOrConj(String string)
  {
    StringTokenizer st =
      new StringTokenizer(string, MetamapTokenization.UTTER_TOKEN_DELIMITERS);
    while (st.hasMoreTokens())
      {
	String token = (String)st.nextToken();
	if (prepOrConj.containsKey(token.toLowerCase()))
	  {
	    return true;
	  }
      }
    return false;
  }

  /**
   * invert strings of the form "word1, word2" to "word2 word1" 
   * if no prepositions or conjunction are present.
   * @param string source string.
   * @return uninverted string.
   */
  public static String syntacticUninvertString(String string)
  {
    if (containsPrepOrConj(string))
      {
	return string;
      }
    else 
      {
	return Lex.uninvert(string);
      }
  }

  /**
   * 
   * @param string source string.
   * @return normalized version of uninverted string.
   */
  public static String normalizedSyntacticUninvertString(String string)
  {
    String NormString = normalizeString(string);
    String NormSUninvString = syntacticUninvertString(NormString);
    return NormSUninvString;
  }

  /**
   * Normalize string. Elminate multiple meaning designators and "NOS" strings.
   * @param string source string.
   * @return normalized version of string.
   */
  public static String normalizeString(String string)
  {    
    String string1 = eliminateMultipleMeaningDesignatorString(string);
    String normString = eliminateNosString(string1);
    if (normString.trim().length() != 0)
      {
	return normString;
      }
    else 
      {
	return string;
      }
  }

  public static String stripPossessives(String string)
  {
    StringBuffer sb = new StringBuffer();
    StringTokenizer st = 
      new StringTokenizer(string, MetamapTokenization.TOKEN_DELIMITERS);
    if (st.hasMoreTokens())
      {
	String token = st.nextToken();
	sb.append(MetamapTokenization.removePossessives(token));
      }
    while (st.hasMoreTokens())
      {
	String token = st.nextToken();
	sb.append(" ").append(MetamapTokenization.removePossessives(token));
      }
    return sb.toString();
  }

  public static boolean alldigits(String string)
  {
    for (int i = 0; i < string.length(); i++)
      {
	if (! Character.isDigit(string.charAt(i))) {
	  return false;
	}
      }
    return true;
  }

  static String readMultipleMeaningDesignator(StringTokenizer st, 
				   String initialToken, 
				   String endToken)
  {
    StringBuffer sb = new StringBuffer(initialToken);
    if (st.hasMoreTokens())
      {
	String token = st.nextToken();
	sb.append(token);
	if (alldigits(token)) 
	  {
	    if (st.hasMoreTokens())
	      {
		token = st.nextToken();
		sb.append(token);
		if (token.equals(endToken)) {
		  return "";
		}
	      }
	  }
      }
    return sb.toString();
  }

  /**
   * Remove multiple meaning designators; method removes an
   * expression of the form <n> where n is an integer from string.
   * The modified string is returned.
   * @param string string to be evaluated.
   * @return string, modified if designator was present.
   */
  public static String eliminateMultipleMeaningDesignatorString(String string)
  {
    StringBuffer sb = new StringBuffer();
    StringTokenizer st = new StringTokenizer(string, " <>", true);
    while (st.hasMoreTokens())
      {
	String token = st.nextToken();
	if (token.equals("<"))
	  {
	    sb.append(readMultipleMeaningDesignator(st, token, ">"));
	  }
	else 
	  {
	    sb.append(token);
	  }
      }
    return sb.toString();
  }

  static boolean validPattern(String string, int start, String pattern)
  {
    if (pattern.length() + start == string.length() ||
	(pattern.length() + start < string.length() && 
	 Character.isWhitespace(string.charAt(pattern.length() + start)) ) )
    {
      return true;
    }
    return false;
  }

  /**
   * Eliminate NOS String if present.
   *
   * @param string string to be evaluated.
   * @return string, modified if designator was present.
   */
  public static String eliminateNosString(String string)
  {

    String normString0 = eliminateNosAcros(string);
    return eliminateNosExpansion(normString0).toLowerCase();
  }

  /**
   * Determine if a pattern of the form: "NOS ANTIBODY", "NOS AB", etc.
   * exists.  if so return true.
   */
  public static boolean abgn_form(String str)
  {
    for (int i = 0; i < abgn_forms.length; i++) {
      if (abgn_forms[i].equals(str.substring
			       (0,Math.min(str.length(),abgn_forms[i].length()))))
	return true;
    }
    return false;
  }

  public static String eliminateNosAcros(String string)
  {
    // split_string_backtrack(String,"NOS",Left,Right),
    int charindex = string.indexOf("NOS");
    if (charindex >= 0) {
      String left = string.substring(0, Math.max(charindex, 0));
      String right = string.substring(charindex+3, 
				      Math.max(charindex+3, string.length()));
      if (((right.length() != 0) && Character.isLetterOrDigit(right.charAt(0))) ||
	  ((left.length() != 0) && Character.isLetter(left.charAt(left.length()-1)))) {
	charindex = string.indexOf("NOS", charindex+1);
	if (charindex == -1) {
	  return string;
	}
      }
    }
    for (int i = 0; i < nosStrings.length; i++) {
      charindex = string.indexOf(nosStrings[i]);
      if (charindex >= 0) {
	

	String left2 = string.substring(0, Math.max(charindex, 0));
	String right2 = string.substring(charindex+nosStrings[i].length(), 
					 Math.max(charindex+nosStrings[i].length(),
						  string.length()));

	if (nosStrings[i].equals(")NOS")) {
	  return eliminateNosAcros(left2 + ")" + right2);
	} else if (nosStrings[i].equals(".NOS")) {
	  return eliminateNosAcros(left2 + "." + right2);
	} else if (nosStrings[i].equals(" NOS")) {
	  if (! abgn_form(string.substring(charindex+1)))
	    return eliminateNosAcros(left2 + right2);
	} else if (nosStrings[i].equals("-NOS")) {
	    return eliminateNosAcros(left2 + right2);
	} else {
	  return eliminateNosAcros(left2 + right2);
	}
      }
    }
    return string;
  }
  
  /**
   * Eliminate any expansions of NOS 
   * 
   */
  public static String eliminateNosExpansion(String string)
  {
    String lcString = string.toLowerCase();
    for (int i = 0; i < nos_expansion_string.length; i++) {
      int charindex = lcString.indexOf(nos_expansion_string[i]);
      if (charindex == 0) {
	return eliminateNosExpansion
	  (lcString.substring(nos_expansion_string[i].length()));
      } else if (charindex > 0) {
	return eliminateNosExpansion
	  (lcString.substring(0, charindex) + 
	   lcString.substring(charindex + nos_expansion_string[i].length()));
      }
    }
    return string;
  }

  public static String removeExtraBlanks(String string)
  {
    StringTokenizer st = new StringTokenizer(string, " ");
    StringBuffer sb = new StringBuffer();
    while (st.hasMoreTokens()) {
      sb.append(st.nextToken()).append(" ");
    }
    return sb.toString();
  }

  public static String removeHyphens(String string)
  {
    String noHyphenString = string.replace('-', ' ');
    String modifiedString = removeExtraBlanks(noHyphenString);
    return modifiedString;
  }
  public static void main(String[] args)
  {
    if (args.length > 0)
      {
	StringBuffer testSB = new StringBuffer(args[0]);
	for (int i=1; i < args.length; i++) {
	  testSB.append(" ").append(args[i]);
	}
	String testString = testSB.toString();

	// should have a JUnit test suite here. 
	String result = eliminateMultipleMeaningDesignatorString(testString);
	System.out.println(testString + " -> eMMDS -> " + result);

	result = eliminateNosString(testString);
	System.out.println(testString + " -> eNosS -> " + result);

	result = syntacticUninvertString(testString);
	System.out.println(testString + " -> sUS -> " + result);

	result = normalizedSyntacticUninvertString(testString);
	System.out.println(testString + " -> nSUS -> " + result);
      }
  }

}// NLSStrings
