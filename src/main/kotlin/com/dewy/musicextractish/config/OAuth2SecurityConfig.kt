package com.dewy.musicextractish.config

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.reactive.function.client.WebClient


@EnableWebSecurity
class OAuth2SecurityConfig : WebSecurityConfigurerAdapter() {
    @Bean
    fun webClient(authorizedClientManager: OAuth2AuthorizedClientManager?): WebClient {
        val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .build()
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .cors()
            .and()
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .oauth2Client()

    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository?,
        authorizedClientRepository: OAuth2AuthorizedClientRepository?
    ): OAuth2AuthorizedClientManager? {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build()
        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository
        )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}