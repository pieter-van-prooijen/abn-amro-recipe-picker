package nl.lambdatree.recipepicker.infrastructure.api;

import lombok.Builder;
import lombok.Data;
import nl.lambdatree.recipepicker.domain.Ingredient;
import nl.lambdatree.recipepicker.domain.RecipeCategory;
import org.springframework.lang.Nullable;

import javax.validation.constraints.*;
import java.util.Set;

import static nl.lambdatree.recipepicker.domain.Ingredient.NAME_LENGTH;
import static nl.lambdatree.recipepicker.domain.Recipe.INSTRUCTIONS_LENGTH;
import static nl.lambdatree.recipepicker.domain.Recipe.MAX_NOF_SERVINGS;

@Data
@Builder
public class Recipe {

   @Nullable
   private Long id;

   @NotBlank
   @Size(min = 1, max = NAME_LENGTH)
   private String name;
   @NotBlank
   @Size(min = 1, max = INSTRUCTIONS_LENGTH)
   private String instructions;
   @NotNull
   private RecipeCategory category;
   @Min(1)
   @Max(MAX_NOF_SERVINGS)
   private int nofServings;
   @NotEmpty
   private Set<Ingredient> ingredients;

   public static Recipe fromDomain(nl.lambdatree.recipepicker.domain.Recipe from) {
      return Recipe.builder()
                   .id(from.getId())
                   .name(from.getName())
                   .instructions(from.getInstructions())
                   .category(from.getCategory())
                   .nofServings(from.getNofServings())
                   .ingredients(from.getIngredients())
                   .build();
   }

   public nl.lambdatree.recipepicker.domain.Recipe toDomain() {
      // Leave out the id, as this should be supplied as a separate argument in update and delete.
      return nl.lambdatree.recipepicker.domain.Recipe.builder()
                                                     .name(getName())
                                                     .instructions(getInstructions())
                                                     .category(getCategory())
                                                     .nofServings(getNofServings())
                                                     .ingredients(getIngredients())
                                                     .build();
   }
}
