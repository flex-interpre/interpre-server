package com.flex.interpre.global.config;

import com.flex.interpre.domain.auth.handler.CustomOAuth2SuccessHandler;
import com.flex.interpre.domain.auth.service.CustomOAuth2UserService;
import com.flex.interpre.global.security.authentication.CustomAuthenticationEntryPoint;
import com.flex.interpre.global.security.authentication.CustomOAuth2AuthorizationRequestResolver;
import com.flex.interpre.global.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2AuthorizationRequestResolver authorizationRequestResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        return http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth->auth
                                .baseUri("/api/oauth2/authorization")
                                .authorizationRequestResolver(authorizationRequestResolver))
                        .userInfoEndpoint(c->c.userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request.anyRequest().permitAll())
                .exceptionHandling(e->e.authenticationEntryPoint(customAuthenticationEntryPoint))
                .build();

    }

}
