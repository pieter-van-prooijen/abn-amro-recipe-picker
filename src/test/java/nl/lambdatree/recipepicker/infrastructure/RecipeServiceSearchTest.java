package nl.lambdatree.recipepicker.infrastructure;

import nl.lambdatree.recipepicker.BaseInfrastructureTest;
import nl.lambdatree.recipepicker.domain.RecipeCategory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipeServiceSearchTest extends BaseInfrastructureTest {

   @Test
   public void testRecipeTextSearch() {
      // Search should be case-insensitive
      var result = recipeService.search("boil", RecipeCategory.ALL, 0,
                                        Set.of(), Set.of());
      assertThat(result).containsExactly(spaghettiBolognese);

      // Should also search the name of a recipe to improve the relevance
      result = recipeService.search("bolognese", RecipeCategory.ALL, 0,
                                        Set.of(), Set.of());
      assertThat(result).containsExactly(spaghettiBolognese);

      // Should pick the correct recipe depending on what's in the description
      result = recipeService.search("glaze", RecipeCategory.ALL, 0, Set.of(), Set.of());
      assertThat(result).containsExactly(curry);
   }

   @Test
   public void testRecipeCategorySearch() {
      var result = recipeService.search("", RecipeCategory.ALL, 0,
                                        Set.of(), Set.of());
      // Any category, ordered by name.
      assertThat(result).containsExactly(curry, spaghettiBolognese);

      // Specific category
      result = recipeService.search("", RecipeCategory.MEAT, 0, Set.of(), Set.of());
      assertThat(result).containsExactly(spaghettiBolognese);
   }

   @Test
   public void testRecipeNofServingsSearch() {
      var result = recipeService.search("", RecipeCategory.ALL, 6,
                                        Set.of(), Set.of());
      // Specific servings
      assertThat(result).containsExactly(curry);
   }

   @Test
   public void testRecipeIngredientSearch() {
      var result = recipeService.search("", RecipeCategory.ALL, 0,
                                        Set.of(mincedBeef, cannedTomatoes), Set.of());
      assertThat(result).containsExactly(spaghettiBolognese);

      // List of included ingredients is "and"
      result = recipeService.search("", RecipeCategory.ALL, 0,
                                        Set.of(mincedBeef, chickPeas), Set.of());
      assertThat(result).isEmpty();


      result = recipeService.search("", RecipeCategory.ALL, 0, Set.of(), Set.of(mincedBeef));
      assertThat(result).containsExactly(curry);

      // List of excluded ingredients is "or"
      result = recipeService.search("", RecipeCategory.ALL, 0, Set.of(), Set.of(mincedBeef, chickPeas));
      assertThat(result).isEmpty();
   }
}
