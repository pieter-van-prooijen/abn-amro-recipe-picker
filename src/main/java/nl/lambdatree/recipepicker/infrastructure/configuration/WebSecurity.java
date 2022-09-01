package nl.lambdatree.recipepicker.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

   private final RecipePickerProperties recipePickerProperties;

   @Bean
   /*
    * Configure with http basic auth for now, for APIs a JWT based scheme is more suitable.
    */
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.NEVER).disable()
          .csrf().disable()
          .authorizeRequests(r -> r.antMatchers("/v1/**").hasRole("USER"))
          .httpBasic(Customizer.withDefaults());

      return http.build();
   }

   @Bean
   public UserDetailsService userDetailsService() {
      @SuppressWarnings("deprecation") UserDetails user = User.withDefaultPasswordEncoder() // Not for production.
                                                              .username(recipePickerProperties.getUser())
                                                              .password(recipePickerProperties.getPassword())
                                                              .roles("USER")
                                                              .build();
      return new InMemoryUserDetailsManager(user);
   }
}
