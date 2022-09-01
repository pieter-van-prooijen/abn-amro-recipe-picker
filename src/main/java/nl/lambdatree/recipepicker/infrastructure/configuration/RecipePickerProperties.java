package nl.lambdatree.recipepicker.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Data
@ConfigurationProperties(prefix = "recipe-picker")
public class RecipePickerProperties {
   private URI baseUrl;
   private String user;
   private String password;
}
