package com.shevtsov.sunshine.main.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.common.SecurityConstants;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.shevtsov.sunshine.common.SecurityConstants.TOKEN_PREFIX;


@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private UserServiceImpl userService;
    private AccessDeniedHandler accessDeniedHandler;

    public JWTAuthorizationFilter(AuthenticationManager authManager, AccessDeniedHandler accessDeniedHandler, UserServiceImpl userService) {
        super(authManager);
        this.userService = userService;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            try {
                chain.doFilter(req, res);
            } catch (IOException | ServletException exception) {
                log.error(exception.getMessage());
                throw new AuthorizationErrorException(exception.getMessage());
            }
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        if (authentication == null) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("JWT token stores invalid data! Please login again and receive new token!");
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return;
        }

        User user = userService.loadUserByUsername(authentication.getName());

        if (user.getIsBanned()) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("You are banned from Sunshine!");
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return;
        }

        if (user.getActivationCode() != null) {
            AccessDeniedException accessDeniedException = new AccessDeniedException("Please activate your account! We have send a letter at " + user.getUserInfo().getEmail());
            log.error(accessDeniedException.getMessage());
            try {
                accessDeniedHandler.handle(req, res, accessDeniedException);
            } catch (IOException | ServletException ioException) {
                log.error(ioException.getMessage());
            }
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            chain.doFilter(req, res);
        } catch (IOException | ServletException exception) {
            log.error(exception.getMessage());
            throw new AuthorizationErrorException(exception.getMessage());
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);

        if (token != null) {
            try {
                String user = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()))
                        .build()
                        .verify(token.replace(TOKEN_PREFIX, ""))
                        .getSubject();
                if (user != null) {
                    User userPrincipal = (User) userService.loadUserByUsername(user);
                    if (userPrincipal == null) {
                        return null;
                    }
                    return new UsernamePasswordAuthenticationToken(user, null, userPrincipal.getAuthorities());
                }
            } catch (TokenExpiredException tokenExpiredException) {
                return null;
            }
            return null;
        }

        return null;
    }
}
