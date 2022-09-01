package nl.lambdatree.recipepicker.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "recipes")
public class Recipe {

   // Shared constraints between domain and the api
   public static final int NAME_LENGTH = 255;
   public static final int INSTRUCTIONS_LENGTH = 2048;
   public static final int MAX_NOF_SERVINGS = 16;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @NotBlank
   @Size(min = 1, max = NAME_LENGTH)
   private String name;

   @NotBlank
   @Size(min = 1, max = INSTRUCTIONS_LENGTH)
   private String instructions;

   @NotNull
   private RecipeCategory category;

   @Min(1)
   @Max(MAX_NOF_SERVINGS)
   private int nofServings;

   @NotEmpty
   @ManyToMany(fetch = FetchType.EAGER)
   @JoinTable(name = "recipe_ingredient",
      joinColumns = @JoinColumn(name = "recipe"),
      inverseJoinColumns = @JoinColumn(name = "ingredient"))
   private Set<Ingredient> ingredients;
}
