package nl.lambdatree.recipepicker.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "ingredients")
public class Ingredient {

   public static final int NAME_LENGTH = 255;
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @NotBlank
   @Size(min = 1, max = NAME_LENGTH)
   private String name;

   // Only the id matters for equality and hashing
   @Override
   public int hashCode() {
      return id.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof Ingredient && id != null) {
         return id.equals(((Ingredient) o).getId());
      }
      return false;
   }

   @Override
   public String toString() {
      return String.format("Ingredient id: %d name: '%s'", id, name);
   }
}
