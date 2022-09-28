package com.shevtsov.sunshine.main.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.common.SecurityConstants;
import com.shevtsov.sunshine.exceptions.AuthenticationCredentialsReadingException;
import com.shevtsov.sunshine.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    private AccessDeniedHandler accessDeniedHandler;


    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, AccessDeniedHandler accessDeniedHandler, UserService userService) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/login");
        this.userService = userService;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        UserInfo credentials = null;
        try {
            credentials = new ObjectMapper().readValue(req.getInputStream(), UserInfo.class);
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new AuthenticationCredentialsReadingException();
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.getUsername(),
                        credentials.getPassword(),
                        new ArrayList<>()));
        User logUser = (User) userService.loadUserByUsername(authentication.getName());

        if (logUser.getIsBanned()) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("You are banned from Sunshine!");
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return null;
        }


        if (logUser.getActivationCode() == null) {                      // не позволяет залогиниться без активированного аккаунта
            return authentication;
        } else {
            AccessDeniedException accessDeniedException = new AccessDeniedException("Please activate your account! We have send a letter at " + logUser.getUserInfo().getEmail());
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return null;
        }


    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException {
        String token = JWT.create()
                .withSubject(((User) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));


        String body = "SUCCESSFULLY LOGGED IN:  " + ((User) auth.getPrincipal()).getUsername() + "\nTOKEN PREFIX: " + SecurityConstants.TOKEN_PREFIX + "\nTOKEN: " + token;

        res.getWriter().write(body);
        res.getWriter().flush();
    }
}