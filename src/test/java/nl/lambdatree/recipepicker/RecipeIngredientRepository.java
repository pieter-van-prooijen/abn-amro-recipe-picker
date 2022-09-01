package nl.lambdatree.recipepicker;

import nl.lambdatree.recipepicker.domain.RecipeIngredient;
import org.springframework.data.repository.CrudRepository;

/*
 * Join table repository, for testing associations when creating / deleting recipes etc.
 */
public interface RecipeIngredientRepository extends CrudRepository<RecipeIngredient, Long> {
}
