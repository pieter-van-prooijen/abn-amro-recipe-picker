package nl.lambdatree.recipepicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RecipePickerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipePickerApplication.class, args);
	}

}
