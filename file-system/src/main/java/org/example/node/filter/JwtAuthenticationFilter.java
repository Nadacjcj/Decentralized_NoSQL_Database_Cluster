package org.example.node.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.node.util.loginutil.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.startsWith("/api/signup") || path.equals("/api/login") || path.startsWith("/actuator") || path.startsWith("/api/signup-forward")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            Map<String, Object> claims = jwtUtil.validateAndExtract(token);

            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String userId = (String) claims.get("id");
                String username = (String) claims.get("username");
                UserPrincipal principal = new UserPrincipal(userId, username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    public static class UserPrincipal {
        private final String id;
        private final String username;

        public UserPrincipal(String id, String username) {
            this.id = id;
            this.username = username;
        }

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }

}
