package org.example.mall_tiny01.component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.mall_tiny01.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtConfig jwtConfig;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        if (method.equals("OPTIONS")) {
            return true;
        }
        
        if (uri.equals("/") || uri.contains("/admin/login") || uri.contains("/login")) {
            return true;
        }
        
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("Authorization");
        }

        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Jwts.parser()
                    .setSigningKey(jwtConfig.secret)
                    .parseClaimsJws(token);
            
            String username = jwtConfig.getUserName(token);
            UserContext.setUsername(username);
            
            return true;
        } catch (Exception e) {
            System.out.println("JWT验证失败，token: " + token);
            System.out.println("错误信息: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登陆验证失败: " + e.getMessage() + "\"}");
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
