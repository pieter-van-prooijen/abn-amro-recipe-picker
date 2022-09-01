package nl.lambdatree.recipepicker;

import nl.lambdatree.recipepicker.domain.Ingredient;
import nl.lambdatree.recipepicker.domain.Recipe;
import nl.lambdatree.recipepicker.domain.RecipeCategory;

import java.util.Set;

/* Create various domain objects for testing */
public class FixtureFactory {

   public static Ingredient mincedBeef() {
      return Ingredient.builder()
                       .id(1L)
                       .name("Minced Beef")
                       .build();
   }

   public static Ingredient cannedTomatoes() {
      return Ingredient.builder()
                       .id(2L)
                       .name("Canned Tomatoes")
                       .build();
   }

   public static Ingredient onions() {
      return Ingredient.builder()
                       .id(3L)
                       .name("Onions")
                       .build();
   }

   public static Ingredient chickPeas() {
      return Ingredient.builder()
                       .id(4L)
                       .name("Chick Peas")
                       .build();
   }

   public static Ingredient milk() {
      return Ingredient.builder()
                       .id(5L)
                       .name("Milk")
                       .build();
   }

   public static Ingredient flour() {
      return Ingredient.builder()
                       .id(6L)
                       .name("Wheat Flour")
                       .build();
   }

   public static Recipe spaghettiBolognese(Ingredient... ingredients) {
      return Recipe.builder()
                   .name("Spaghetti Bolognese")
                   .instructions("Boil salted water, brown the minced meat ..")
                   .category(RecipeCategory.MEAT)
                   .ingredients(Set.of(ingredients))
                   .nofServings(4)
                   .build();
   }

   public static Recipe curry(Ingredient... ingredients) {
      return Recipe.builder()
                   .name("Curry")
                   .instructions("Soak the peas in salted water, glaze the onions ...")
                   .category(RecipeCategory.VEGAN)
                   .ingredients(Set.of(ingredients))
                   .nofServings(6)
                   .build();
   }

}
