package nl.lambdatree.recipepicker.infrastructure;

import nl.lambdatree.recipepicker.BaseInfrastructureTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryTest extends BaseInfrastructureTest {

   @Test
   public void testRecipeCreationAndDeletion() {
      assertThat(recipeRepository.findById(spaghettiBolognese.getId())).hasValue(spaghettiBolognese);
      assertThat(recipeIngredientRepository.count()).isEqualTo(6); // Two recipes with three ingredients each.

      // Deletion of a recipe should not delete its ingredients.
      recipeRepository.delete(spaghettiBolognese);
      assertThat(recipeIngredientRepository.count()).isEqualTo(3); // One recipe with three ingredients.
      assertThat(ingredientRepository.count()).isEqualTo(6);
   }
}
