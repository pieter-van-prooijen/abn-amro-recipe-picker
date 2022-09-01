package nl.lambdatree.recipepicker.infrastructure;

import nl.lambdatree.recipepicker.BaseInfrastructureTest;
import nl.lambdatree.recipepicker.infrastructure.api.ErrorCode;
import nl.lambdatree.recipepicker.infrastructure.api.ErrorResponse;
import nl.lambdatree.recipepicker.infrastructure.api.Ingredient;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IngredientControllerTest extends BaseInfrastructureTest {

   @Test
   public void shouldListAllIngredients() {
      var ingredients = listIngredients();
      assertThat(ingredients).hasSize(6);
      assertThat(ingredients).contains(Ingredient.fromDomain(mincedBeef));
   }

   @Test
   public void shouldCreateIngredient() {
      var uri = UriComponentsBuilder.fromPath("/v1/ingredients")
                                    .build()
                                    .toUri();

      var ingredient = Ingredient.builder()
                                 .name("Sugar")
                                 .build();
      var request = new RequestEntity<>(ingredient, requestHeaders(), HttpMethod.POST, uri);

      var response = restTemplate.exchange(request, Void.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNull();
      assertThat(response.getHeaders().getLocation()).isNotNull();

      var ingredients = listIngredients();
      assertThat(ingredients).hasSize(7);
      assertThat(ingredients.stream().map(Ingredient::getName)).contains("Sugar");
   }

   @Test
   public void shouldRefuseIngredientWithExistingName() {
      var uri = UriComponentsBuilder.fromPath("/v1/ingredients")
                                    .build()
                                    .toUri();

      var ingredient = Ingredient.builder()
                                 .name(chickPeas.getName())
                                 .build();
      var request = new RequestEntity<>(ingredient, requestHeaders(), HttpMethod.POST, uri);

      var response = restTemplate.exchange(request, ErrorResponse.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
   }

   @Test
   public void shouldRefuseDeleteIfIngredientInUse() {
      var uri = UriComponentsBuilder.fromPath("/v1/ingredients/{id}")
                                    .build(mincedBeef.getId());

      var request = new RequestEntity<>(null, requestHeaders(), HttpMethod.DELETE, uri);

      var error = restTemplate.exchange(request, ErrorResponse.class);
      assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(error.getBody()).isNotNull();
      assertThat(error.getBody().getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
   }

   private List<Ingredient> listIngredients() {
      var uri = UriComponentsBuilder.fromPath("/v1/ingredients")
                                    .build()
                                    .toUri();
      var request = new RequestEntity<>(null, requestHeaders(), HttpMethod.GET, uri);

      var response = restTemplate.exchange(request,
                                           new ParameterizedTypeReference<List<Ingredient>>() {});
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      return response.getBody();
   }
}
