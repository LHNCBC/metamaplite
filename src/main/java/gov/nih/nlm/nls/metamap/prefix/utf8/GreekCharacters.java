package gov.nih.nlm.nls.metamap.prefix.utf8;

import java.util.Map;
import java.util.HashMap;

/**
 * Maps for converting UTF-8 Greek characters to expanded equivalents in ASCII.
 *
 *
 * Created: Thu Jan  3 15:14:04 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class GreekCharacters {

  // UTF-8 Greek Charactors
  // ΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυφχψωϊϋόύώϏϐϑϒϓϔϕϖϗϘϙϚϛϜϝϞϟϠϡϢϣϤϥϦϧϨϩϪϫϬϭϮϯϰϱϲϳϴϵ϶ϷϸϹϺϻϼϽϾϿ
  static Map<Character,String> characterToStringMap = new HashMap<Character,String>();
  // encodings are actually in UTF-16, Java's native internal character encoding formatv
  static {
    characterToStringMap.put('\u0370',"HETA"); // GREEK CAPITAL LETTER HETA
    characterToStringMap.put('\u0371',"heta"); // GREEK SMALL LETTER HETA
    characterToStringMap.put('\u0372',"SAMPI"); // GREEK CAPITAL LETTER ARCHAIC SAMPI
    characterToStringMap.put('\u0373',"SAMPI"); // GREEK SMALL LETTER ARCHAIC SAMPI
    characterToStringMap.put('\u0374',"GREEK NUMERAL SIGN"); // GREEK NUMERAL SIGN
    characterToStringMap.put('\u0375',"GREEK LOWER NUMERAL SIGN"); // GREEK LOWER NUMERAL SIGN
    characterToStringMap.put('\u0376',"GREEK CAPITAL LETTER PAMPHYLIAN DIGAMMA"); // GREEK CAPITAL LETTER PAMPHYLIAN DIGAMMA
    characterToStringMap.put('\u0377',"GREEK SMALL LETTER PAMPHYLIAN DIGAMMA"); // GREEK SMALL LETTER PAMPHYLIAN DIGAMMA
    characterToStringMap.put('\u037A',"GREEK YPOGEGRAMMENI"); // GREEK YPOGEGRAMMENI
    characterToStringMap.put('\u037B',"GREEK SMALL REVERSED LUNATE SIGMA SYMBOL"); // GREEK SMALL REVERSED LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u037C',"GREEK SMALL DOTTED LUNATE SIGMA SYMBOL"); // GREEK SMALL DOTTED LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u037D',"GREEK SMALL REVERSED DOTTED LUNATE SIGMA SYMBOL"); // GREEK SMALL REVERSED DOTTED LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u037E',"GREEK QUESTION MARK"); // GREEK QUESTION MARK
    characterToStringMap.put('\u0384',"GREEK TONOS"); // GREEK TONOS
    characterToStringMap.put('\u0385',"GREEK DIALYTIKA TONOS"); // GREEK DIALYTIKA TONOS
    characterToStringMap.put('\u0386',"GREEK CAPITAL LETTER ALPHA WITH TONOS"); // GREEK CAPITAL LETTER ALPHA WITH TONOS
    characterToStringMap.put('\u0387',"GREEK ANO TELEIA"); // GREEK ANO TELEIA
    characterToStringMap.put('\u0388',"GREEK CAPITAL LETTER EPSILON WITH TONOS"); // GREEK CAPITAL LETTER EPSILON WITH TONOS
    characterToStringMap.put('\u0389',"GREEK CAPITAL LETTER ETA WITH TONOS"); // GREEK CAPITAL LETTER ETA WITH TONOS
    characterToStringMap.put('\u038A',"GREEK CAPITAL LETTER IOTA WITH TONOS"); // GREEK CAPITAL LETTER IOTA WITH TONOS
    characterToStringMap.put('\u038C',"GREEK CAPITAL LETTER OMICRON WITH TONOS"); // GREEK CAPITAL LETTER OMICRON WITH TONOS
    characterToStringMap.put('\u038E',"GREEK CAPITAL LETTER UPSILON WITH TONOS"); // GREEK CAPITAL LETTER UPSILON WITH TONOS
    characterToStringMap.put('\u038F',"GREEK CAPITAL LETTER OMEGA WITH TONOS"); // GREEK CAPITAL LETTER OMEGA WITH TONOS
    characterToStringMap.put('\u0390',"GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS"); // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
    characterToStringMap.put('\u0391',"Alpha"); // GREEK CAPITAL LETTER ALPHA
    characterToStringMap.put('\u0392',"Beta"); // GREEK CAPITAL LETTER BETA
    characterToStringMap.put('\u0393',"Gamma"); // GREEK CAPITAL LETTER GAMMA
    characterToStringMap.put('\u0394',"Delta"); // GREEK CAPITAL LETTER DELTA
    characterToStringMap.put('\u0395',"Epsilon"); // GREEK CAPITAL LETTER EPSILON
    characterToStringMap.put('\u0396',"Zeta"); // GREEK CAPITAL LETTER ZETA
    characterToStringMap.put('\u0397',"Eta"); // GREEK CAPITAL LETTER ETA
    characterToStringMap.put('\u0398',"Theta"); // GREEK CAPITAL LETTER THETA
    characterToStringMap.put('\u0399',"Iota"); // GREEK CAPITAL LETTER IOTA
    characterToStringMap.put('\u039A',"Kappa"); // GREEK CAPITAL LETTER KAPPA
    characterToStringMap.put('\u039B',"Lambda"); // GREEK CAPITAL LETTER LAMBDA
    characterToStringMap.put('\u039C',"Mu"); // GREEK CAPITAL LETTER MU
    characterToStringMap.put('\u039D',"Nu"); // GREEK CAPITAL LETTER NU
    characterToStringMap.put('\u039E',"Xi"); // GREEK CAPITAL LETTER XI
    characterToStringMap.put('\u039F',"Omicron"); // GREEK CAPITAL LETTER OMICRON
    characterToStringMap.put('\u03A0',"Pi"); // GREEK CAPITAL LETTER PI
    characterToStringMap.put('\u03A1',"Rho"); // GREEK CAPITAL LETTER RHO
    characterToStringMap.put('\u03A3',"Sigma"); // GREEK CAPITAL LETTER SIGMA
    characterToStringMap.put('\u03A4',"Tau"); // GREEK CAPITAL LETTER TAU
    characterToStringMap.put('\u03A5',"Upsilon"); // GREEK CAPITAL LETTER UPSILON
    characterToStringMap.put('\u03A6',"Phi"); // GREEK CAPITAL LETTER PHI
    characterToStringMap.put('\u03A7',"Chi"); // GREEK CAPITAL LETTER CHI
    characterToStringMap.put('\u03A8',"Psi"); // GREEK CAPITAL LETTER PSI
    characterToStringMap.put('\u03A9',"Omega"); // GREEK CAPITAL LETTER OMEGA
    characterToStringMap.put('\u03AA',"GREEK CAPITAL LETTER IOTA WITH DIALYTIKA"); // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
    characterToStringMap.put('\u03AB',"GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA"); // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
    characterToStringMap.put('\u03AC',"GREEK SMALL LETTER ALPHA WITH TONOS"); // GREEK SMALL LETTER ALPHA WITH TONOS
    characterToStringMap.put('\u03AD',"GREEK SMALL LETTER EPSILON WITH TONOS"); // GREEK SMALL LETTER EPSILON WITH TONOS
    characterToStringMap.put('\u03AE',"GREEK SMALL LETTER ETA WITH TONOS"); // GREEK SMALL LETTER ETA WITH TONOS
    characterToStringMap.put('\u03AF',"GREEK SMALL LETTER IOTA WITH TONOS"); // GREEK SMALL LETTER IOTA WITH TONOS
    characterToStringMap.put('\u03B0',"GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS"); // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
    characterToStringMap.put('\u03B1',"alpha"); // GREEK SMALL LETTER ALPHA
    characterToStringMap.put('\u03B2',"beta"); // GREEK SMALL LETTER BETA
    characterToStringMap.put('\u03B3',"gamma"); // GREEK SMALL LETTER GAMMA
    characterToStringMap.put('\u03B4',"delta"); // GREEK SMALL LETTER DELTA
    characterToStringMap.put('\u03B5',"epsilon"); // GREEK SMALL LETTER EPSILON
    characterToStringMap.put('\u03B6',"zeta"); // GREEK SMALL LETTER ZETA
    characterToStringMap.put('\u03B7',"eta"); // GREEK SMALL LETTER ETA
    characterToStringMap.put('\u03B8',"theta"); // GREEK SMALL LETTER THETA
    characterToStringMap.put('\u03B9',"iota"); // GREEK SMALL LETTER IOTA
    characterToStringMap.put('\u03BA',"kappa"); // GREEK SMALL LETTER KAPPA
    characterToStringMap.put('\u03BB',"lambda"); // GREEK SMALL LETTER LAMBDA
    characterToStringMap.put('\u03BC',"mu"); // GREEK SMALL LETTER MU
    characterToStringMap.put('\u03BD',"nu"); // GREEK SMALL LETTER NU
    characterToStringMap.put('\u03BE',"xi"); // GREEK SMALL LETTER XI
    characterToStringMap.put('\u03BF',"omicron"); // GREEK SMALL LETTER OMICRON
    characterToStringMap.put('\u03C0',"pi"); // GREEK SMALL LETTER PI
    characterToStringMap.put('\u03C1',"rho"); // GREEK SMALL LETTER RHO
    characterToStringMap.put('\u03C2',"sigmaf"); // GREEK SMALL LETTER FINAL SIGMA
    characterToStringMap.put('\u03C3',"sigma"); // GREEK SMALL LETTER SIGMA
    characterToStringMap.put('\u03C4',"tau"); // GREEK SMALL LETTER TAU
    characterToStringMap.put('\u03C5',"upsilon"); // GREEK SMALL LETTER UPSILON
    characterToStringMap.put('\u03C6',"phi"); // GREEK SMALL LETTER PHI
    characterToStringMap.put('\u03C7',"chi"); // GREEK SMALL LETTER CHI
    characterToStringMap.put('\u03C8',"psi"); // GREEK SMALL LETTER PSI
    characterToStringMap.put('\u03C9',"omega"); // GREEK SMALL LETTER OMEGA
    characterToStringMap.put('\u03CA',"GREEK SMALL LETTER IOTA WITH DIALYTIKA"); // GREEK SMALL LETTER IOTA WITH DIALYTIKA
    characterToStringMap.put('\u03CB',"GREEK SMALL LETTER UPSILON WITH DIALYTIKA"); // GREEK SMALL LETTER UPSILON WITH DIALYTIKA
    characterToStringMap.put('\u03CC',"GREEK SMALL LETTER OMICRON WITH TONOS"); // GREEK SMALL LETTER OMICRON WITH TONOS
    characterToStringMap.put('\u03CD',"GREEK SMALL LETTER UPSILON WITH TONOS"); // GREEK SMALL LETTER UPSILON WITH TONOS
    characterToStringMap.put('\u03CE',"GREEK SMALL LETTER OMEGA WITH TONOS"); // GREEK SMALL LETTER OMEGA WITH TONOS
    characterToStringMap.put('\u03CF',"GREEK CAPITAL KAI SYMBOL"); // GREEK CAPITAL KAI SYMBOL
    characterToStringMap.put('\u03D0',"GREEK BETA SYMBOL"); // GREEK BETA SYMBOL
    characterToStringMap.put('\u03D1',"thetasym"); // GREEK THETA SYMBOL
    characterToStringMap.put('\u03D2',"upsih"); // GREEK UPSILON WITH HOOK SYMBOL
    characterToStringMap.put('\u03D3',"GREEK UPSILON WITH ACUTE AND HOOK SYMBOL"); // GREEK UPSILON WITH ACUTE AND HOOK SYMBOL
    characterToStringMap.put('\u03D4',"GREEK UPSILON WITH DIAERESIS AND HOOK SYMBOL"); // GREEK UPSILON WITH DIAERESIS AND HOOK SYMBOL
    characterToStringMap.put('\u03D5',"straightphi"); // GREEK PHI SYMBOL
    characterToStringMap.put('\u03D6',"piv"); // GREEK PI SYMBOL
    characterToStringMap.put('\u03D7',"GREEK KAI SYMBOL"); // GREEK KAI SYMBOL
    characterToStringMap.put('\u03D8',"GREEK LETTER ARCHAIC KOPPA"); // GREEK LETTER ARCHAIC KOPPA
    characterToStringMap.put('\u03D9',"GREEK SMALL LETTER ARCHAIC KOPPA"); // GREEK SMALL LETTER ARCHAIC KOPPA
    characterToStringMap.put('\u03DA',"GREEK LETTER STIGMA"); // GREEK LETTER STIGMA
    characterToStringMap.put('\u03DB',"GREEK SMALL LETTER STIGMA"); // GREEK SMALL LETTER STIGMA
    characterToStringMap.put('\u03DC',"Gammad"); // GREEK LETTER DIGAMMA
    characterToStringMap.put('\u03DD',"gammad"); // GREEK SMALL LETTER DIGAMMA
    characterToStringMap.put('\u03DE',"GREEK LETTER KOPPA"); // GREEK LETTER KOPPA
    characterToStringMap.put('\u03DF',"GREEK SMALL LETTER KOPPA"); // GREEK SMALL LETTER KOPPA
    characterToStringMap.put('\u03E0',"GREEK LETTER SAMPI"); // GREEK LETTER SAMPI
    characterToStringMap.put('\u03E1',"GREEK SMALL LETTER SAMPI"); // GREEK SMALL LETTER SAMPI
    characterToStringMap.put('\u03E2',"COPTIC CAPITAL LETTER SHEI"); // COPTIC CAPITAL LETTER SHEI
    characterToStringMap.put('\u03E3',"COPTIC SMALL LETTER SHEI"); // COPTIC SMALL LETTER SHEI
    characterToStringMap.put('\u03E4',"COPTIC CAPITAL LETTER FEI"); // COPTIC CAPITAL LETTER FEI
    characterToStringMap.put('\u03E5',"COPTIC SMALL LETTER FEI"); // COPTIC SMALL LETTER FEI
    characterToStringMap.put('\u03E6',"COPTIC CAPITAL LETTER KHEI"); // COPTIC CAPITAL LETTER KHEI
    characterToStringMap.put('\u03E7',"COPTIC SMALL LETTER KHEI"); // COPTIC SMALL LETTER KHEI
    characterToStringMap.put('\u03E8',"COPTIC CAPITAL LETTER HORI"); // COPTIC CAPITAL LETTER HORI
    characterToStringMap.put('\u03E9',"COPTIC SMALL LETTER HORI"); // COPTIC SMALL LETTER HORI
    characterToStringMap.put('\u03EA',"COPTIC CAPITAL LETTER GANGIA"); // COPTIC CAPITAL LETTER GANGIA
    characterToStringMap.put('\u03EB',"COPTIC SMALL LETTER GANGIA"); // COPTIC SMALL LETTER GANGIA
    characterToStringMap.put('\u03EC',"COPTIC CAPITAL LETTER SHIMA"); // COPTIC CAPITAL LETTER SHIMA
    characterToStringMap.put('\u03ED',"COPTIC SMALL LETTER SHIMA"); // COPTIC SMALL LETTER SHIMA
    characterToStringMap.put('\u03EE',"COPTIC CAPITAL LETTER DEI"); // COPTIC CAPITAL LETTER DEI
    characterToStringMap.put('\u03EF',"COPTIC SMALL LETTER DEI"); // COPTIC SMALL LETTER DEI
    characterToStringMap.put('\u03F0',"varkappa"); // GREEK KAPPA SYMBOL
    characterToStringMap.put('\u03F1',"varrho"); // GREEK RHO SYMBOL
    characterToStringMap.put('\u03F2',"GREEK LUNATE SIGMA SYMBOL"); // GREEK LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u03F3',"GREEK LETTER YOT"); // GREEK LETTER YOT
    characterToStringMap.put('\u03F4',"GREEK CAPITAL THETA SYMBOL"); // GREEK CAPITAL THETA SYMBOL
    characterToStringMap.put('\u03F5',"straightepsilon"); // GREEK LUNATE EPSILON SYMBOL
    characterToStringMap.put('\u03F6',"backepsilon"); // GREEK REVERSED LUNATE EPSILON SYMBOL
    characterToStringMap.put('\u03F7',"GREEK CAPITAL LETTER SHO"); // GREEK CAPITAL LETTER SHO
    characterToStringMap.put('\u03F8',"GREEK SMALL LETTER SHO"); // GREEK SMALL LETTER SHO
    characterToStringMap.put('\u03F9',"GREEK CAPITAL LUNATE SIGMA SYMBOL"); // GREEK CAPITAL LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u03FA',"GREEK CAPITAL LETTER SAN"); // GREEK CAPITAL LETTER SAN
    characterToStringMap.put('\u03FB',"GREEK SMALL LETTER SAN"); // GREEK SMALL LETTER SAN
    characterToStringMap.put('\u03FC',"GREEK RHO WITH STROKE SYMBOL"); // GREEK RHO WITH STROKE SYMBOL
    characterToStringMap.put('\u03FD',"GREEK CAPITAL REVERSED LUNATE SIGMA SYMBOL"); // GREEK CAPITAL REVERSED LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u03FE',"GREEK CAPITAL DOTTED LUNATE SIGMA SYMBOL"); // GREEK CAPITAL DOTTED LUNATE SIGMA SYMBOL
    characterToStringMap.put('\u03FF',"Greek CAPITAL REVERSED DOTTED LUNATE SIGMA SYMBOL"); // GREEK CAPITAL REVERSED DOTTED LUNATE SIGMA SYMBOL
  }

  public static boolean isGreek(Character ch) {
    return characterToStringMap.containsKey(ch);
  }
  public static String getExpansion(Character ch) {
    return characterToStringMap.get(ch);
  }
}
