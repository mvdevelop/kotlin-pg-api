package com.example.api.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration:86400000}") val validityInMilliseconds: Long
) {

    private val key: SecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey))

    fun createToken(authentication: org.springframework.security.core.Authentication): String {
        val userPrincipal = authentication.principal as org.springframework.security.core.userdetails.UserDetails
        val authorities = authentication.authorities.joinToString(",") { it.authority }

        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .setSubject(userPrincipal.username)
            .claim("roles", authorities)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key)
            .compact()
    }

    fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return parseClaims(token).subject
    }

    companion object {
        fun resolveToken(bearerToken: String?): String? {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) return null
            return bearerToken.substring(7)
        }
    }
}