package br.com.devisecenter.devise_center.modules.auth.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Service
public class TokenService {

    @Value("${jwt.key}")
    private String jwtKey;

    public String generateToken(String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtKey);
            String token = JWT.create()
                    .withIssuer("devise-center")
                    .withSubject(username)
                    .withExpiresAt(Instant.now().plusSeconds(604800))
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new JWTCreationException("Não foi possível gerar token: ", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("devise-center")
                    .build();

            return verifier.verify(token).getSubject();
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Não foi possível validar o token: ", exception);
        }
    }

}
