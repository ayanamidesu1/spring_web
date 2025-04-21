package com.example.web_pixiv.middleware;

import com.example.web_pixiv.config.Config;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    // 使用安全的秘钥生成方式
    private final SecretKey key;

    // 使用Duration避免时间计算溢出
    private static final long EXPIRATION_MS = 30L * 24 * 60 * 60 * 1000; // 30天

    public JwtUtil() {
        // 验证秘钥长度并转换为安全秘钥
        byte[] keyBytes = Config.getJwtSecretBytes();
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "HS256需要至少256位秘钥，当前为" + (keyBytes.length*8) + "位");
        }
        this.key=Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Map<String, Object> claims) {
        Instant now = Instant.now();
        String token=Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))  // jjwt会自动转换为秒
                .expiration(Date.from(now.plusMillis(EXPIRATION_MS)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        //生成后先行验证token有效性
        if (!validateToken(token)) {
            System.err.printf("生成的令牌无效：token:%s\n", token);
            throw new SignatureException("令牌无效");
        }
        //如果有效尝试解析
        else{
            String user=getClaimsFromToken(token).toString();
            System.out.printf("生成的令牌有效：token:%s\n",user);
        }
        return token;
    }

    public Claims getClaimsFromToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 增强令牌验证
    public boolean validateToken(String token) {
        //System.out.println("正在验证令牌：" + token);
        if (!isTokenStructureValid(token)) {
            System.err.println("令牌结构无效");
            return false;
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            System.err.println("验证失败: " + e.getClass().getSimpleName());
            System.err.println("原因: " + e.getMessage());
            return false;
        }
    }


    // 验证令牌基本结构
    private boolean isTokenStructureValid(String token) {
        if (token == null || token.split("\\.").length != 3) {
            return false;
        }
        try {
            String header = new String(
                    Base64.getUrlDecoder().decode(token.split("\\.")[0])
            );
            // 修改为只检查alg字段，因为新版JJWT可能不包含typ
            return header.contains("\"alg\"");
        } catch (Exception e) {
            return false;
        }
    }
    public String refreshToken(String token) throws JwtException {
        Claims claims = getClaimsFromToken(token);
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXPIRATION_MS)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}