package org.example.user_web_service.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.user_web_service.exception.ApiErrorResponse;
import org.example.user_web_service.services.JwtService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (
                StringUtils.isEmpty(authHeader) ||
                        !StringUtils.startsWith(authHeader, "Bearer ") ||
                        SecurityContextHolder.getContext().getAuthentication() != null
        ) {

            filterChain.doFilter(request, response);

            return;
        }

        final String jwt = authHeader.substring(7).trim();

        try {

            final String userEmail = jwtService.getEmail(jwt);

            if (StringUtils.isBlank(userEmail)) {
                writeUnauthorized(response, request, "Invalid token");

                return;
            }

            UserDetails userDetails;
            userDetails = userService.loadUserByUsername(userEmail);

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                writeUnauthorized(response, request, "Invalid or expired token");

                return;
            }

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // parsing/expired/signature errors typically land here
            log.debug("JWT error: {}", e.getMessage());

            SecurityContextHolder.clearContext();

            writeUnauthorized(response, request, "Invalid or expired token");
        } catch (Exception e) {
            // user not found, unexpected errors, etc.
            log.error("Unexpected auth error", e);

            SecurityContextHolder.clearContext();

            writeUnauthorized(response, request, "Authentication failed");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html");
    }

    private void writeUnauthorized (
            HttpServletResponse response,
            HttpServletRequest request,
            String message
    ) throws IOException {

        if (response.isCommitted()) {

            return;
        }

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = ApiErrorResponse.of(
                401,
                "Unauthorized",
                message,
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), body);
        response.flushBuffer();
    }
}