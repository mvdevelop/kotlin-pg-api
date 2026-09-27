package com.example.api.security

import com.example.api.model.User
import com.example.api.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(UserDetailsServiceImpl::class.java)

    @Transactional(readOnly = true)
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Authentication failed")

        if (user.isAccountLocked()) {
            logger.warn("Authentication attempt on locked account for username: {}", "***")
            throw UsernameNotFoundException("Account is locked")
        }

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.username)
            .password(user.passwordHash)
            .authorities(mapRolesToAuthorities(user.roles))
            .accountExpired(false)
            .accountLocked(user.accountLocked)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }

    private fun mapRolesToAuthorities(roles: Set<String>): Collection<GrantedAuthority> {
        return roles.map { role -> SimpleGrantedAuthority(role) }
    }
}
