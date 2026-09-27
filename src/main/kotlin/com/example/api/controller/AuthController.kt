package com.example.api.controller

import com.example.api.dto.LoginRequestDTO
import com.example.api.dto.RegisterRequestDTO
import com.example.api.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with default ROLE_USER. Password must be 12+ chars with upp/lowercase, digit, and special char.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "User registered successfully", content = [Content(mediaType = "application/json")]),
        ApiResponse(responseCode = "400", description = "Validation error", content = [Content(mediaType = "application/json")]),
        ApiResponse(responseCode = "409", description = "Username or email already exists", content = [Content(mediaType = "application/json")])
    )
    fun register(@Valid @RequestBody request: RegisterRequestDTO): ResponseEntity<Any> {
        return try {
            authService.register(request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("message" to "User registered successfully"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "conflict", "message" to e.message))
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates credentials and returns a JWT bearer token.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authentication successful — JWT token returned", content = [Content(mediaType = "application/json")]),
        ApiResponse(responseCode = "401", description = "Invalid credentials", content = [Content(mediaType = "application/json")])
    )
    fun login(@Valid @RequestBody request: LoginRequestDTO): ResponseEntity<Any> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(
                mapOf(
                    "token" to response.token,
                    "expiresIn" to response.expiresIn,
                    "tokenType" to "Bearer"
                )
            )
        } catch (e: org.springframework.security.authentication.BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "invalid_credentials", "message" to "Invalid username or password"))
        }
    }
}
