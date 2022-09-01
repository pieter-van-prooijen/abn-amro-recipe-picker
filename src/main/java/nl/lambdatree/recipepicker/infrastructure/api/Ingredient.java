package nl.lambdatree.recipepicker.infrastructure.api;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static nl.lambdatree.recipepicker.domain.Ingredient.NAME_LENGTH;

/*
 * DTO for Ingredients.
 */
@Data
@Builder
public class Ingredient {
   private Long id;
   @NotBlank
   @Size(max = NAME_LENGTH)
   private String name;
   public static Ingredient fromDomain(nl.lambdatree.recipepicker.domain.Ingredient ingredient) {
      return Ingredient.builder()
                       .id(ingredient.getId())
                       .name(ingredient.getName())
                       .build();
   }

   public nl.lambdatree.recipepicker.domain.Ingredient toDomain() {
      return nl.lambdatree.recipepicker.domain.Ingredient.builder()
         .id(id)
         .name(name)
         .build();
   }
}
