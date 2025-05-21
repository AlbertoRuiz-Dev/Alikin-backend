package com.backendalikin.security.filter;

import com.backendalikin.security.JwtTokenProvider;
import com.backendalikin.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        System.out.println("🔍 shouldNotFilter: " + request.getRequestURI());
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/swagger-ui/index.html")||
                path.startsWith("/uploads/"); // 🔥 Añadir esta línea

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // 🛑 Evitar interceptar imágenes u otros recursos públicos
        if (path.startsWith("/uploads/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        // Obtener el token JWT del encabezado de autorización
        String jwt = getJwtFromRequest(request);
        
        // Validar el token
        if (StringUtils.hasText(jwt)) {
            try {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // Cargar detalles del usuario
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validar token
                if (tokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Establecer el usuario autenticado en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("No se pudo establecer la autenticación del usuario", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}