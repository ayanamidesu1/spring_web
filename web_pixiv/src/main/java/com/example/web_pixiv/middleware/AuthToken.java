package com.example.web_pixiv.middleware;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.web_pixiv.logger.RequestLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
public class AuthToken extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public AuthToken(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头获取token
        String token = extractToken(request);
        //System.out.printf("token: %s\n", token);
        Map<String, Object> userInfo = new HashMap<>();
        boolean isLogin = false;
        // 2. 验证token
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                Claims claims = jwtUtil.getClaimsFromToken(token);

                // 3. 构建用户信息
                userInfo.put("id", claims.get("id"));
                userInfo.put("username", claims.get("username"));
                userInfo.put("email", claims.get("email"));
                userInfo.put("role", claims.get("role"));
                userInfo.put("is_vip", claims.get("is_vip"));
                userInfo.put("user_status",claims.get("user_status"));
                // 添加其他需要的字段...

                isLogin = true;
                RequestLogger.log(RequestLogger.Level.INFO, "token验证成功");

                // 4. 检查token是否即将过期（可选）
                long expTime = claims.getExpiration().getTime();
                long currentTime = System.currentTimeMillis();
                if (expTime - currentTime < 24 * 60 * 60 * 1000) { // 剩余时间小于1天
                    response.setHeader("X-Token-Refresh", "true");
                }
            } catch (Exception e) {
                // token解析失败
                System.out.printf("token解析失败，失败原因：%s\n",e.getMessage());
                RequestLogger.log(RequestLogger.Level.ERROR, "token解析失败");
            }
        }
        else {
            System.out.printf("token验证失败，失败原因：%s，token可能失效\n",token);
            RequestLogger.log(RequestLogger.Level.WARN, "token验证失败");
        }

        // 5. 将用户信息附加到请求中
        request.setAttribute("user", userInfo);
        request.setAttribute("is_login", isLogin);

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        final String prefix = "token ";
        if (authHeader != null && authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length());
        }
        return null;
    }
}