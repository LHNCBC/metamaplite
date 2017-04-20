
//
package gov.nih.nlm.nls.metamap.lite.types;

/**
 *
 */

public class PositionImpl implements Position
{
  int start;
  int end;
  public PositionImpl(int start, int end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public int getX() {
    return this.start;
  }

  @Override
  public int getY() {
    return this.end;
  }
  @Override
  public int getStart() { return this.start; }
  @Override
  public int getLength() { return this.end - this.start; } 
  public String toString() {
    return this.start + "/" + this.end;
  }
  public String toStringStartLength() {
    return this.start + "/" + (this.end - this.start);
  }
  public boolean equals(Object obj) {
    return (this.start == ((Position)obj).getStart()) &&
      (this.end == ((Position)obj).getY());
  }
}
