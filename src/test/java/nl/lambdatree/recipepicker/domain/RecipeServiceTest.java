package nl.lambdatree.recipepicker.domain;

import nl.lambdatree.recipepicker.FixtureFactory;
import org.junit.jupiter.api.Test;

import javax.validation.ValidationException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Unit test(s) for the RecipeService domain service.
 */
public class RecipeServiceTest {

   @Test
   public void shouldThrowExceptionForOverlappingIncludeExclude() {
      var include = Set.of(FixtureFactory.mincedBeef(), FixtureFactory.onions());
      var exclude = Set.of(FixtureFactory.cannedTomatoes(), FixtureFactory.onions());

      var service = new RecipeService(null, null);
      service.checkForOverlap(Set.of(), Set.of()); // empty sets shouldn't give an error

      assertThatThrownBy(() -> service.checkForOverlap(include, exclude))
         .isInstanceOf(ValidationException.class)
         .hasMessageContaining("Onions");
   }
}
