package org.poo.fileio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class Coordinates {
   @JsonProperty("x")
   private int x;
   @JsonProperty("y")
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
