package nl.lambdatree.recipepicker.infrastructure.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import nl.lambdatree.recipepicker.domain.Ingredient;
import nl.lambdatree.recipepicker.domain.RecipeCategory;
import nl.lambdatree.recipepicker.domain.RecipeRepository;
import nl.lambdatree.recipepicker.domain.RecipeService;
import nl.lambdatree.recipepicker.infrastructure.configuration.RecipePickerProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@OpenAPIDefinition(
   info = @Info(title = "Recipe Picker API",
      description = "Search and manage recipes with their ingredients."),
   security = {
      @SecurityRequirement(name = "BasicAuthentication")
   }
)
@SecurityScheme( name = "BasicAuthentication",
   type = SecuritySchemeType.HTTP,
   scheme = "basic")

@Validated
@RestController
@Transactional
@RequestMapping(path = "/v1/recipes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RecipeController {

   private final RecipeService recipeService;

   private final RecipeRepository recipeRepository;

   private final RecipePickerProperties recipePickerProperties;

   @PostMapping
   @Operation(description = "Create a new recipe with the selected attributes, returning its location in the " +
      "Location header. Note that only the ids of the ingredients are used, the name is discarded." +
      "Add new ingredients using the /v1/ingredients API before using them in a new recipe.")
   @ApiResponses({
      @ApiResponse(responseCode = "201", description = "successfully created",
         headers = {
            @Header (name = "Location", description = "The URI of the new recipe.")
      }),
      @ApiResponse(responseCode = "400", description = "Bad request, like an unknown ingredient.",
         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
   })
   public ResponseEntity<Void> createRecipe(@Valid @RequestBody Recipe recipe) {
      var saved = recipeRepository.save(recipe.toDomain());
      var location = UriComponentsBuilder.fromUri(recipePickerProperties.getBaseUrl())
                                         .path("/v1/recipes/{id}")
                                         .build(saved.getId());

      return ResponseEntity.created(location)
                           .build();
   }

   @RequestMapping(path = "/{id}", method = RequestMethod.GET)
   @Operation(description = "Retrieve the recipe at the supplied id.")
   @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the recipe."),
      @ApiResponse(responseCode = "404", description = "Recipe not found.",
         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
   })
   public ResponseEntity<Recipe> getRecipe(@NotNull @PathVariable Long id) {
      var found = recipeRepository.findById(id)
                                  .orElseThrow(); // Use the exception handler to construct a not-found response.
      return ResponseEntity.ok(Recipe.fromDomain(found));
   }

   @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
   @Operation(description = "Update the recipe at the supplied id with the new recipe in the request body.")
   @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Successfully updated the recipe."),
      @ApiResponse(responseCode = "404", description = "Recipe not found.",
         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
   })
   public ResponseEntity<Void> updateRecipe(@Valid @RequestBody Recipe recipe, @PathVariable Long id) {
      recipeService.update(id, recipe.toDomain());
      return ResponseEntity.noContent()
                           .build();
   }

   @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
   @Operation(description = "Delete the recipe at the supplied id.")
   @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Recipe successfully deleted."),
      @ApiResponse(responseCode = "404", description = "Recipe not found.",
         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
   })
   public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
      recipeService.delete(id);
      return ResponseEntity.noContent()
                           .build();
   }

   // TODO: add paging and sorting using the Pageable utilities from spring-data.
   @GetMapping
   @Operation(description = "Search for recipes with the specified criteria." +
      "Leave a criteria empty to not use it in the search. " +
      "Results are ordered by the recipe's name. " +
      "The include and exclude ingredient lists should not overlap.")
   @Parameter(name = "text", description = "The name or instructions should contain this string.")
   @Parameter(name = "category", description = "The recipe should have this category.")
   @Parameter(name = "nofServings", description = "The ingredient amounts should be for this many servings.")
   @Parameter(name = "includeIngredientIds", description = "The recipe should use *all* the ingredients from this " +
      "list of ingredient ids.")
   @Parameter(name = "excludeIngredientIds", description = "The recipe should *not* have at any of these ingredients " +
      "from this list of ingredient ids.")
   @ApiResponse(responseCode = "400", description = "Bad Request, usually a problem in one of the parameters of the " +
      "request body.",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
   public List<Recipe> search(
      @Nullable @RequestParam String text,
      @Nullable @RequestParam @Valid RecipeCategory recipeCategory,
      @Nullable @RequestParam Integer nofServings,
      @Nullable @RequestParam Long[] includeIngredientIds,
      @Nullable @RequestParam Long[] excludeIngredientIds) {

      // The domain layer expects non-null arguments and full ingredient objects in the include/exclude sets.
      return recipeService.search(text == null ? "" : text,
                                  recipeCategory == null ? RecipeCategory.ALL : recipeCategory,
                                  nofServings == null ? 0 : nofServings,
                                  toIngredients(includeIngredientIds),
                                  toIngredients(excludeIngredientIds))
                          .stream()
                          .map(Recipe::fromDomain)
                          .toList();
   }

   private Set<Ingredient> toIngredients(Long[] ids) {
      if (ids == null) {
         return Set.of();
      } else
         return Arrays.stream(ids)
                      .map(id -> Ingredient.builder().id(id).build())
                      .collect(Collectors.toSet());
   }
}
