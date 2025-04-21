package com.example.web_pixiv.middleware;

import com.example.web_pixiv.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.web_pixiv.logger.RequestLogger;

@RestController
@Component
public class UserLogin {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String identifier = loginRequest.get("identifier");
        String password = loginRequest.get("password");

        // SQL查询用户
        String sql = "SELECT * FROM users WHERE " +
                "(id::text = ? OR username = ? OR email = ? OR phone = ?) " +
                "AND password = ?";

        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    sql, identifier, identifier, identifier, identifier, password);
            //System.out.println(users);

            if (users.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 401);
                response.put("msg", "用户名或密码错误");
                RequestLogger.log(RequestLogger.Level.WARN, "用户名或密码错误");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Map<String, Object> user = users.get(0);

            // 检查用户状态
            if (user.get("user_status") != null &&
                    Integer.parseInt(user.get("user_status").toString()) == 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("msg", "账户已被封禁");
                RequestLogger.log(RequestLogger.Level.WARN, "账户已被封禁");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 创建token claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.get("id"));
            claims.put("username", user.get("username"));
            claims.put("sex", user.get("sex"));
            claims.put("age", user.get("age"));
            claims.put("register_time", user.get("register_time"));
            claims.put("avatar", user.get("avatar"));
            claims.put("email", user.get("email"));
            claims.put("phone", user.get("phone"));
            claims.put("background", user.get("background"));
            claims.put("user_status", user.get("user_status"));
            claims.put("role", user.get("role"));
            claims.put("is_vip", user.get("is_vip"));
            claims.put("birthday", user.get("birthday"));
            claims.put("vip_last_update", user.get("vip_last_update"));

            String token = jwtUtil.generateToken(claims);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "登录成功");
            response.put("token", token);

            RequestLogger.log(RequestLogger.Level.INFO, "用户登录成功");
            //System.out.println(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "服务器内部错误: " + e.getMessage());
            RequestLogger.log(RequestLogger.Level.ERROR, "服务器内部错误: " + e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}