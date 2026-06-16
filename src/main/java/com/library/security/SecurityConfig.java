package com.library.security;

import com.library.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter; // REMOVED in Spring Security 5.7+ / 6.x
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 *
 * BREAKS on Spring Security 5.7+ / Spring Boot 3.x:
 * - WebSecurityConfigurerAdapter is REMOVED — must use SecurityFilterChain bean
 * - configure(HttpSecurity) → SecurityFilterChain @Bean
 * - configure(WebSecurity) → WebSecurityCustomizer @Bean
 * - configure(AuthenticationManagerBuilder) → different approach
 * - @EnableGlobalMethodSecurity(prePostEnabled=true) → @EnableMethodSecurity
 * - authenticationManagerBean() → inject AuthenticationManager differently
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)  // Deprecated: use @EnableMethodSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {  // ← REMOVED in Spring Security 6

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // This override approach is removed — must use AuthenticationManagerBuilder differently
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // authenticationManagerBean() removed from WebSecurityConfigurerAdapter in Spring 6
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) {
        // WebSecurity.ignoring() is deprecated — use requestMatchers in HttpSecurity
        web.ignoring()
           .antMatchers("/h2-console/**")           // antMatchers() removed in Spring 6
           .antMatchers("/swagger-ui.html")
           .antMatchers("/swagger-resources/**")
           .antMatchers("/v2/api-docs")
           .antMatchers("/webjars/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
