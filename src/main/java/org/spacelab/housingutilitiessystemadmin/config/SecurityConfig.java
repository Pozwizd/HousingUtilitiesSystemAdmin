package org.spacelab.housingutilitiessystemadmin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.repository.MongoTokenRepository;
import org.spacelab.housingutilitiessystemadmin.security.AdminUserDetailsService;
import org.spacelab.housingutilitiessystemadmin.security.CustomOidcUser;
import org.spacelab.housingutilitiessystemadmin.security.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;
    private final MongoTokenRepository mongoTokenRepository;
    private final CustomOidcUserService customOidcUserService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login", "/assets/**", "/forgotPassword", "/confirmation", "/oauth2/**", "/user-info", "/current-user", "/debug/**").permitAll()
                        .requestMatchers("/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login((oauth2) -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint((userInfo) -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            try {
                                log.info("=== OAuth2 SUCCESS HANDLER ===");
                                log.info("Authentication: {}", authentication.getClass().getSimpleName());
                                log.info("Principal: {}", authentication.getPrincipal().getClass().getSimpleName());
                                if (authentication.getPrincipal() instanceof CustomOidcUser customUser) {
                                    log.info("CustomOidcUser email: {}", customUser.getEmail());
                                    log.info("Admin ID: {}", customUser.getAdmin().getId());
                                    log.info("Admin Role: {}", customUser.getAdmin().getRole());
                                }
                                response.sendRedirect("/");
                            } catch (IOException e) {
                                log.error("Error in OAuth2 success handler", e);
                            }
                        })
                        .defaultSuccessUrl("/", true)
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .rememberMe((rm) -> rm
                        .tokenRepository(mongoTokenRepository)
                        .tokenValiditySeconds(1209600)
                        .userDetailsService(adminUserDetailsService)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember-me")
                        .key("uniqueSecretKey")
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(adminUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
