package nl.lambdatree.recipepicker;

import nl.lambdatree.recipepicker.domain.*;
import nl.lambdatree.recipepicker.infrastructure.api.RecipeController;
import nl.lambdatree.recipepicker.infrastructure.configuration.RecipePickerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Base class for testing the full infrastructure, with a running web server and database.
 *
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseInfrastructureTest {

   @Autowired
   protected RecipeRepository recipeRepository;

   @Autowired
   protected IngredientRepository ingredientRepository;

   @Autowired
   protected RecipeIngredientRepository recipeIngredientRepository;

   @Autowired
   protected RecipeService recipeService;

   @Autowired
   protected RecipeController recipeController;

   @Autowired
   private RecipePickerProperties recipePickerProperties;

   @Autowired
   protected TestRestTemplate restTemplate;

   protected Ingredient mincedBeef, cannedTomatoes, onions, chickPeas, milk, flour;
   protected Recipe spaghettiBolognese, curry;

   @BeforeEach
   public void cleanRepositories() {
      recipeRepository.deleteAll();
      ingredientRepository.deleteAll();
      recipeIngredientRepository.deleteAll();

      mincedBeef = ingredientRepository.save(FixtureFactory.mincedBeef());
      cannedTomatoes = ingredientRepository.save(FixtureFactory.cannedTomatoes());
      onions = ingredientRepository.save(FixtureFactory.onions());
      chickPeas = ingredientRepository.save(FixtureFactory.chickPeas());
      milk = ingredientRepository.save(FixtureFactory.milk());
      flour = ingredientRepository.save(FixtureFactory.flour());

      spaghettiBolognese = FixtureFactory.spaghettiBolognese(mincedBeef, cannedTomatoes, onions);
      spaghettiBolognese = recipeRepository.save(spaghettiBolognese);

      curry = recipeRepository.save(FixtureFactory.curry(chickPeas, cannedTomatoes, onions));
   }

   public HttpHeaders requestHeaders() {
      var headers = new HttpHeaders();
      headers.add(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE);
      headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);

      var credentials = String.format("%s:%s", recipePickerProperties.getUser(),
                                      recipePickerProperties.getPassword());
      var bytes = credentials.getBytes(StandardCharsets.ISO_8859_1);
      headers.add(HttpHeaders.AUTHORIZATION,
                  "Basic " + Base64.getEncoder().encodeToString(bytes));
      return headers;
   }

}
