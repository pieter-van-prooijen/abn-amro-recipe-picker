package nl.lambdatree.recipepicker.infrastructure.api;

/*
 * Simple CRUD controller to manage ingredients, to enforce consistent use and naming
 */

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.lambdatree.recipepicker.domain.IngredientRepository;
import nl.lambdatree.recipepicker.infrastructure.configuration.RecipePickerProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.StreamSupport;

@Validated
@RestController
@Transactional
@RequestMapping(path = "/v1/ingredients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class IngredientController {

   private final IngredientRepository ingredientRepository;
   private final RecipePickerProperties recipePickerProperties;

   @GetMapping
   @Operation(description = "List all ingredients available for creating a recipe.")
   public List<Ingredient> getAll() {
      return StreamSupport.stream(ingredientRepository.findAll().spliterator(), false)
                          .map(Ingredient::fromDomain)
                          .toList();
   }

   @Operation(description = "Fetch an ingredient by its id.")
   @ApiResponses( {
      @ApiResponse(responseCode = "200", description = "Successful retrieval."),
      @ApiResponse(responseCode = "404", description = "Ingredient not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
   })
   @GetMapping(path = "/{id}")
   public ResponseEntity<Ingredient> get(@NotNull @PathVariable Long id) {
      var found = ingredientRepository.findById(id)
                                      .orElseThrow(); // Use the exception handler to construct the response.
      return ResponseEntity.ok(Ingredient.fromDomain(found));
   }

   @PostMapping
   @Operation(description = "Create a new ingredient, returning its location in the Location header.")
   @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Successful creation of the ingredient.",
      headers = {
         @Header(name = "Location", description = "The URI of the new ingredient.")
      }),
      @ApiResponse(responseCode = "400", description = "Bad request, for example if an existing ingredient has the " +
         "same name.")
   })
   public ResponseEntity<Void> add(@NotNull @Valid @RequestBody Ingredient ingredient) {
      var saved = ingredientRepository.save(ingredient.toDomain());
      var location = UriComponentsBuilder.fromUri(recipePickerProperties.getBaseUrl())
                                         .path("/v1/ingredients/{id}")
                                         .build(saved.getId());
      log.info("Created ingredient {}", saved);
      return ResponseEntity.created(location)
                           .build();
   }

   @DeleteMapping(path = "/{id}")
   @Operation(description = "Delete an ingredient by its id.")
   @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Ingredient successfully deleted."),
      @ApiResponse(responseCode = "400", description = "Bad request, for example if a recipe still uses the " +
         "ingredient.")
     }
   )
   public ResponseEntity<Void> delete(@NotNull @PathVariable Long id) {
      ingredientRepository.deleteById(id);
      log.info("Deleted ingredient {}", id);
      return ResponseEntity.noContent().build();
   }
}
