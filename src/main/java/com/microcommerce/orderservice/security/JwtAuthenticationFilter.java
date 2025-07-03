package com.microcommerce.orderservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtre JWT pour le Order Service
 * Intercepte les requêtes et valide les tokens JWT
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        // Vérifier si le header Authorization contient un token Bearer
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Enlever "Bearer "
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Erreur lors de l'extraction du username du token JWT", e);
            }
        }
        
        // Si on a un username et qu'aucune authentification n'est déjà en place
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Valider le token
            if (jwtUtil.validateToken(jwt)) {
                // Extraire les informations du token
                String userId = jwtUtil.extractUserId(jwt);
                String role = jwtUtil.extractRole(jwt);
                
                // Créer l'authentification
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        username, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                
                // Ajouter les détails de la requête
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Définir l'authentification dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Ajouter les informations utilisateur dans les attributs de la requête
                request.setAttribute("userId", userId);
                request.setAttribute("userRole", role);
                request.setAttribute("username", username);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Ne pas filtrer les endpoints publics
        return path.startsWith("/actuator/") ||
               path.equals("/api/orders/health");
    }
}