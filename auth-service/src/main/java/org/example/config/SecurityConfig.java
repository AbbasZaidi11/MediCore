package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// @Configuration is the marker that says "scan this class for @Bean methods."

@Configuration
public class SecurityConfig {

    @Bean
    //is for when you don't own the class and you want to create an instance of it.
    // Its's from a third party library and you can't annotate it with @Component or @Service. 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

// @Component
// this is the **generic version** of `@Service` / `@Repository` / `@Controller`.
// all of those are literally just `@Component` with a label:
// @Component        → generic managed bean
// @Service          → @Component + signals "business logic layer"
// @Repository       → @Component + signals "data access layer" + exception translation
// @Controller       → @Component + signals "web layer"
    // @RestController   → @Controller + @ResponseBody (for REST APIs)
