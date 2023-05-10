//
package gov.nih.nlm.nls.metamap.prefix;

import java.util.List;
import java.util.ArrayList;
import gov.nih.nlm.nls.metamap.lite.types.Entity;

/**
 *
 */

public class ERTokenImpl extends PosTokenImpl implements Token, PosToken, ClassifiedToken, ERToken
{
  String tokenClass;
  String partOfSpeech;
  List<Entity> entityList = new ArrayList<Entity>();

  public ERTokenImpl(String tokenText, int offset, String tokenClass) {
    super(tokenText, offset);
    this.tokenClass = tokenClass;
    this.partOfSpeech = "";
  }

  public ERTokenImpl(String tokenText, int offset, String tokenClass, String partOfSpeech) {
    super(tokenText, offset);
    this.tokenClass = tokenClass;
    this.partOfSpeech = partOfSpeech;
  }

  public ERTokenImpl(String tokenText, int offset, String tokenClass, List<Entity> entityList) {
    super(tokenText, offset);
    this.tokenClass = tokenClass;
    this.entityList = entityList;
    this.partOfSpeech = "";
  }

  @Override
  public String getTokenClass() {
    return this.tokenClass;
  }

  @Override
  public String getPartOfSpeech() {
    return this.partOfSpeech;
  }

  @Override
  public List<Entity> getEntityList() {
    return this.entityList;
  }

  public void setEntityList(List<Entity> entityList) {
    this.entityList = entityList;
  }
  public void addEntity(Entity entity) {
    this.entityList.add(entity);
  }
  public void setPartOfSpeech(String partOfSpeech) {
    this.partOfSpeech = partOfSpeech;
  }
  
  public String toString() {
    return this.tokenText + "|" + this.tokenClass + "|" + this.offset + "|" + this.partOfSpeech;
  }

  /**
   * Overriding the equality check defined in PosTokenImpl, since some of the uses of equals()
   * for ERToken need to worry about more than just the token text. See, for example, mapToTokenList()
   * in EntityLookup5 - it uses .indexOf() to find where a given token (including its offset, etc.)
   * originally occurred in a differently-processed list of tokens. If we only look at tokenText,
   * and the token in question occurred multiple times in the token list, then we will essentially
   * lop off everything that happened after the first occurrence of the token.
   *
   * This is a particularly big issue with punctuation, as it is quite common for periods etc. to occur
   * multiple times in a given input.
   *
   * If we've done phrase chunking, this is <i>less</i> of a risk but definitely can still happen.
   *
   * @param anotherToken token to compare with
   * @return true if the two tokens share the same text, class, offset, and PoS
   */
  public boolean equals(Object anotherToken)
  {
    assert(anotherToken instanceof  ERTokenImpl);
    // note that we start with a simple int comparison that is likely to fail,
    // so we can short-circuit this call early. This is important because there are
    // a few places where .equals() (or, rather, stuff that calls .equals()) is
    // at the heart of an inner loop.
    return this.offset == ((ERTokenImpl)anotherToken).offset &&
            this.tokenText.equals(((ERTokenImpl)anotherToken).tokenText) &&
            this.tokenClass.equals(((ERTokenImpl)anotherToken).tokenClass) &&
            this.partOfSpeech.equals(((ERTokenImpl)anotherToken).partOfSpeech);
  }
}
