package nl.lambdatree.recipepicker.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "recipe_ingredient")
public class RecipeIngredient {
   @Id
   private long recipe;
   private long ingredient;
}
