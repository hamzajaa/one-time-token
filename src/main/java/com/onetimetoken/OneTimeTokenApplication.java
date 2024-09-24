package com.onetimetoken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@SpringBootApplication
public class OneTimeTokenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneTimeTokenApplication.class, args);
    }

    @Autowired
    private JavaMailSender javaMailSender;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .oneTimeTokenLogin(configure -> configure
                        .generatedOneTimeTokenHandler((request, response, oneTimeToken) -> {
                            String token = oneTimeToken.getTokenValue();
                            var email = oneTimeToken.getUsername();

                            var message = "please go to http://localhost:8080/login/ott?token=" + token;
                            System.out.println(message);

                            response.setContentType(MediaType.TEXT_HTML_VALUE);
                            response.getWriter().write("""
                                    Please check your email for the one time token
                                    """);
                            // send email to the user
                            SimpleMailMessage mailMessage = new SimpleMailMessage();
                            mailMessage.setTo(email);
                            mailMessage.setSubject("One Time Token Test");
                            mailMessage.setText(message);
                            javaMailSender.send(mailMessage);

                        })
                );

        return http.build();
    }


    @Bean
    InMemoryUserDetailsManager userDetailsManager() {
        var hamza = User.withDefaultPasswordEncoder().username("hamzajaa2017@gmail.com").password("123").roles("ADMIN").build();
        return new InMemoryUserDetailsManager(hamza);
    }


}

@RestController
class TestController {
    @GetMapping("/")
    public Map<String, String> test(Principal principal) {
        return Map.of("Message", "Hello " + principal.getName());
    }
}