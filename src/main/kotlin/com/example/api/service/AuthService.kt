package com.example.api.service

import com.example.api.dto.LoginRequestDTO
import com.example.api.dto.LoginResponseDTO
import com.example.api.dto.RegisterRequestDTO
import com.example.api.model.User
import com.example.api.repository.UserRepository
import com.example.api.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: RegisterRequestDTO): User {
        if (userRepository.existsByUsername(request.username)) {
            logger.warn("Registration attempted with existing username: {}", "***")
            throw IllegalArgumentException("Registration failed — username or email already in use")
        }
        if (userRepository.existsByEmail(request.email)) {
            logger.warn("Registration attempted with existing email: {}", "***")
            throw IllegalArgumentException("Registration failed — username or email already in use")
        }

        val passwordHash = passwordEncoder.encode(request.password)
        val user = User(
            username = request.username,
            passwordHash = passwordHash,
            email = request.email,
            roles = setOf("ROLE_USER")  // Admin cannot self-register
        )
        val savedUser = userRepository.save(user)
        logger.info("New user registered: id={}, username={}", savedUser.id, "***")
        return savedUser
    }

    @Transactional
    fun login(request: LoginRequestDTO): LoginResponseDTO {
        logger.info("Login attempt for username: {}", "***")

        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        val token = jwtTokenProvider.createToken(authentication)
        val expiresIn = jwtTokenProvider.validityInMilliseconds / 1000

        logger.info("Login successful for user: {}", "***")
        return LoginResponseDTO(token = token, expiresIn = expiresIn)
    }
}
