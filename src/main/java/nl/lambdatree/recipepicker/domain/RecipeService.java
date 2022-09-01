package nl.lambdatree.recipepicker.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * Service to orchestrate recipe actions on the recipe repository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

   private final EntityManager entityManager;

   public Recipe create(Recipe recipe) {
      var created = recipeRepository.save(recipe);
      log.info("Created recipe {} with id {}", created.getName(), created.getId());
      return created;
   }

   private final RecipeRepository recipeRepository;
   public void update(long id, Recipe recipe) {
      recipeRepository.findById(id).orElseThrow(); // Just check if the recipe is present.
      recipe.setId(id);
      recipeRepository.save(recipe);
      log.info("Updated recipe {} with id {}", recipe.getName(), id);
   }

   public void delete(Long id) {
      var found = recipeRepository.findById(id).orElseThrow(); // Just check if the recipe is present.
      recipeRepository.deleteById(id);
      log.info("Deleted recipe {} with id {}", found.getName(), id);
   }

   @SuppressWarnings("unchecked")
   public List<Recipe> search(String text,
      RecipeCategory category,
      int nofServings,
      Set<Ingredient> includeIngredients,
      Set<Ingredient> excludeIngredients) {

      checkForOverlap(includeIngredients, excludeIngredients);

      // Exclude missing search parameters from the where clause.
      var clauses = new ArrayList<String>(6);
      var parameters = new ArrayList<Pair<String, Object>>(6);

      // TODO: use the full-text search of h2 / sqlite?
      if (StringUtils.isNotBlank(text)) {
         clauses.add("(lower(r.name) like lower(:text) or lower(r.instructions) like lower(:text)) ");
         parameters.add(Pair.of("text", "%" + text + "%"));
      }
      if (category != RecipeCategory.ALL) {
         clauses.add("r.category = :category");
         parameters.add(Pair.of("category", category));
      }
      if (nofServings > 0) {
         clauses.add("r.nofServings = :nofServings");
         parameters.add(Pair.of("nofServings", nofServings));
      }
      // Include is an "and" operation, a recipe must have at least all the ingredients from the set.
      if (!includeIngredients.isEmpty()) {
         clauses.add("r.id in (select recipe from recipe_ingredient ri_inc where " +
                        "(select count(*) from recipe_ingredient ri_inc where ri.recipe = ri_inc.recipe " +
                        "and ri_inc.ingredient in :includeIngredients) = :includeIngredientsSize)");
         parameters.add(Pair.of("includeIngredients", extractIds(includeIngredients)));
         parameters.add(Pair.of("includeIngredientsSize", (long) includeIngredients.size()));
      }
      // Exclude is an "or" operation, a recipe is discarded if it contains just one ingredient from the set.
      if (!excludeIngredients.isEmpty()) {
         clauses.add("r.id not in " +
                        "(select ri_exc.recipe from recipe_ingredient ri_exc where ri_exc.ingredient in :excludeIngredients)");
         parameters.add(Pair.of("excludeIngredients", extractIds(excludeIngredients)));
      }

      var whereClause = clauses.isEmpty() ? "" : " where ";
      var query = entityManager.createQuery(
         "select distinct r from recipes r " +
            " inner join recipe_ingredient  ri on r.id = ri.recipe " +
            whereClause +
            String.join(" and ", clauses) +
            " order by r.name asc");
      parameters.forEach(p -> query.setParameter(p.getKey(), p.getValue()));

      return query.getResultList();
   }

   // Throw an exception if the two ingredient sets have overlapping members, which would give an empty result.
   public void checkForOverlap(Set<Ingredient> includeIngredients, Set<Ingredient> excludeIngredients) {
      // Can't use Set::retainAll() here as include/exclude might be unmodifiable.
      var overlappingIngredient = includeIngredients.stream()
                                                    .filter(excludeIngredients::contains)
                                                    .findFirst();
      if (overlappingIngredient.isPresent()) {
         throw new ValidationException(String.format("Ingredient %s is in both include and exclude sets",
                                                     overlappingIngredient.get()));
      }
   }

   private List<Long> extractIds(Set<Ingredient> ingredients) {
      return ingredients.stream()
                        .map(Ingredient::getId)
                        .toList();
   }
}
