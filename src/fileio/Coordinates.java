package fileio;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class Coordinates implements SerializeHandler {
   @SerializeField(label = "x")
   private int x;
   @SerializeField(label = "y")
   private int y;

   public Coordinates() { }

    @Override
   public String toString() {
      return "Coordinates{"
              + "x="
              + x
              + ", y="
              + y
              + '}';
   }
}
