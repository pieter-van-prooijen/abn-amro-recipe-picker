package nl.lambdatree.recipepicker.infrastructure;

import nl.lambdatree.recipepicker.BaseInfrastructureTest;
import nl.lambdatree.recipepicker.domain.Ingredient;
import nl.lambdatree.recipepicker.domain.RecipeCategory;
import nl.lambdatree.recipepicker.infrastructure.api.ErrorCode;
import nl.lambdatree.recipepicker.infrastructure.api.ErrorResponse;
import nl.lambdatree.recipepicker.infrastructure.api.Recipe;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipeControllerTest extends BaseInfrastructureTest {

   @Test
   public void wildCardSearchTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes")
                                    .build()
                                    .toUri();
      var request = new RequestEntity<>(null, requestHeaders(), HttpMethod.GET, uri);

      var response = restTemplate.exchange(request,
                                           new ParameterizedTypeReference<List<Recipe>>() {
                                           });
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      var result = search("");
      assertThat(result).containsExactly(Recipe.fromDomain(curry), Recipe.fromDomain(spaghettiBolognese));
   }

   @Test
   public void ingredientSearchTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes")
                                    .queryParam("includeIngredientIds", chickPeas.getId())
                                    .build()
                                    .toUri();
      var request = new RequestEntity<>(null, requestHeaders(), HttpMethod.GET, uri);

      var response = restTemplate.exchange(request,
                                           new ParameterizedTypeReference<List<Recipe>>() {
                                           });
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().stream()).containsExactly(Recipe.fromDomain(curry));
   }

   @Test
   public void createRecipeTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes")
                                    .build()
                                    .toUri();

      var recipe = Recipe.builder()
         .name("pancakes")
         .instructions("Mix milk and flour ...")
         .ingredients(Set.of(milk, flour))
         .nofServings(2)
         .category(RecipeCategory.VEGETARIAN)
         .build();
      var request = new RequestEntity<>(recipe, requestHeaders(), HttpMethod.POST, uri);
      var response = restTemplate.exchange(request, Void.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation()).isNotNull();
      assertThat(response.getHeaders().getLocation().toString())
         .matches(Pattern.compile("http://localhost:\\d+/v1/recipes/\\d+"));

      // Fetch the new recipe, which now has an id.
      var location = response.getHeaders().getLocation();
      var fetchUri = UriComponentsBuilder.fromPath(location.getPath())
                                         .build()
                                         .toUri();
      var slash = location.getPath().lastIndexOf('/');
      var id = Long.parseLong(location.getPath().substring(slash + 1));
      var fetchRequest = new RequestEntity<>(null, requestHeaders(), HttpMethod.GET, fetchUri);
      var fetchResponse = restTemplate.exchange(fetchRequest, Recipe.class);
      assertThat(fetchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(fetchResponse.getBody()).isNotNull();
      recipe.setId(id); // the retrieved recipe will have an id.
      assertThat(fetchResponse.getBody()).isEqualTo(recipe);
   }

   @Test
   public void createRecipeWithUnknownIngredientTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes")
                                    .build()
                                    .toUri();

      var unknown = Set.of(Ingredient.builder()
                                     .id(42L)
                                     .name("unknown ingredient")
                                     .build());
      var recipe = Recipe.builder()
                         .name("pancakes")
                         .instructions("Mix milk and flour ...")
                         .ingredients(unknown)
                         .nofServings(2)
                         .category(RecipeCategory.VEGETARIAN)
                         .build();
      var request = new RequestEntity<>(recipe, requestHeaders(), HttpMethod.POST, uri);
      var response = restTemplate.exchange(request, ErrorResponse.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
      assertThat(response.getBody().getDetails()).contains("constraint");
   }

   @Test
   public void updateRecipeTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes/{id}")
                                    .build(spaghettiBolognese.getId());

      var recipe = Recipe.builder()
                         .name("Even better spaghetti bolognese")
                         .instructions(spaghettiBolognese.getInstructions() + " and add double cream at the end.")
                         .ingredients(spaghettiBolognese.getIngredients())
                         .nofServings(spaghettiBolognese.getNofServings())
                         .category(spaghettiBolognese.getCategory())
                         .build();
      var request = new RequestEntity<>(recipe, requestHeaders(), HttpMethod.PUT, uri);
      var response = restTemplate.exchange(request, Void.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      // Check that the update happened
      var result = search("even better");
      recipe.setId(spaghettiBolognese.getId()); // The search result will contain this id.
      assertThat(result).containsExactly(recipe);
   }

   @Test
   public void deleteRecipeTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes/{id}")
                                    .build(spaghettiBolognese.getId());

      var request = new RequestEntity<>(null, requestHeaders(), HttpMethod.DELETE, uri);
      var response = restTemplate.exchange(request, Void.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      // Deleting it again should fail
      var notFound = restTemplate.exchange(request, ErrorResponse.class);
      assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(notFound.getBody()).isNotNull();
      assertThat(notFound.getBody().getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
   }

   @Test
   public void validationTest() {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes")
                                    .build()
                                    .toUri();

      var recipe = Recipe.builder()
                         .name("pancakes")
                         .instructions("Mix milk and flour ...")
                         .ingredients(Set.of(milk, flour))
                         .nofServings(42) // Larger than 16, should be rejected.
                         .category(RecipeCategory.VEGETARIAN)
                         .build();
      var request = new RequestEntity<>(recipe, requestHeaders(), HttpMethod.POST, uri);
      var response = restTemplate.exchange(request, ErrorResponse.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getDetails()).contains("nofServings");
   }

   private List<Recipe> search(String text) {
      var uri = UriComponentsBuilder.fromPath("/v1/recipes")
                                    .queryParam("text", text)
                                    .build()
                                    .toUri();
      var request = new RequestEntity<>(null, requestHeaders(), HttpMethod.GET, uri);

      var response = restTemplate.exchange(request,
                                           new ParameterizedTypeReference<List<Recipe>>() {
                                           });
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();

      return response.getBody();
   }
}
