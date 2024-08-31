import java.util.Objects;

/**
 *  * A generic class to represent a pair of related values. Used to represent Port and protocol name in Log Parser.
 *  *
 *  * @param <X> The type of the first value in the pair.
 *  * @param <Y> The type of the second value in the pair.
 */
public class CustomPair<X, Y> {
  public final X x;
  public final Y y;

  public CustomPair(X x, Y y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CustomPair<?, ?> customPair = (CustomPair<?, ?>) o;
    return Objects.equals(x, customPair.x) && Objects.equals(y, customPair.y);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString(){
    return "(" + this.x + " , " + this.y + ")";
  }
}
