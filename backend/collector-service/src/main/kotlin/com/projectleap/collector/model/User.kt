package com.projectleap.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Document(collection = "users")
data class User(
    @Id val id: String? = null,
    @Field("username")
    @get:JvmName("getUsernameValue")
    val username: String,
    val email: String,
    private val password: String,
    val role: Role = Role.USER
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${'$'}{role.name}"))

    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

enum class Role {
    USER, ADMIN
}
