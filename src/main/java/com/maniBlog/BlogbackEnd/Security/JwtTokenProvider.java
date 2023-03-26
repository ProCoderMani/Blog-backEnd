package com.maniBlog.BlogbackEnd.Security;

import com.maniBlog.BlogbackEnd.Execption.BlogApiExecption;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app-jwt-expiration-miliseconds}")
    public long jwtExpirationDate;

    public String generateToken(Authentication authentication){
        String username = authentication.getName();

        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(key())
                .compact();
    }

    private Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    private String getUsername(String  token){
    Claims claims =  Jwts.parserBuilder()
            .setSigningKey(key()).build()
            .parseClaimsJwt(token).getBody();
    return claims.getSubject();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException ex){
            throw new BlogApiExecption(HttpStatus.BAD_REQUEST,"Invalid JWT token");
        } catch (ExpiredJwtException ex){
            throw new BlogApiExecption(HttpStatus.BAD_REQUEST,"Expired Jwt token");
        } catch (UnsupportedJwtException ex){
            throw new BlogApiExecption(HttpStatus.BAD_REQUEST,"Unsupported JWT token");
        } catch (IllegalArgumentException ex){
            throw new BlogApiExecption(HttpStatus.BAD_REQUEST,"JWT claims string is empty");
        }
    }
}
