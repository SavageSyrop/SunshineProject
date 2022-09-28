package com.shevtsov.sunshine.main.configuration;


import com.shevtsov.sunshine.common.SecurityConstants;
import com.shevtsov.sunshine.exceptions.SecurityConfigurationException;
import com.shevtsov.sunshine.exceptions.exceptionHandlers.CustomAccessDeniedHandler;
import com.shevtsov.sunshine.exceptions.exceptionHandlers.DelegatedAuthenticationEntryPoint;
import com.shevtsov.sunshine.main.filters.JWTAuthenticationFilter;
import com.shevtsov.sunshine.main.filters.JWTAuthorizationFilter;
import com.shevtsov.sunshine.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    private DelegatedAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        try {
            http.csrf().disable()
                    .cors().and()
                    .authorizeRequests()
                    .antMatchers(SecurityConstants.SIGN_UP_URL, "/login", "/forgot_password", "/reset_password/*", "/activate/*", "/search").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin().permitAll()
                    .and()
                    .logout().permitAll()
                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler())
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .and()
                    .addFilter(new JWTAuthenticationFilter(authenticationManager(), accessDeniedHandler, userService))
                    .addFilter(new JWTAuthorizationFilter(authenticationManager(), accessDeniedHandler, userService))
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new SecurityConfigurationException(exception.getMessage());
        }

    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        try {
            auth.userDetailsService(userService).passwordEncoder(encoder);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new SecurityConfigurationException(exception.getMessage());
        }
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}