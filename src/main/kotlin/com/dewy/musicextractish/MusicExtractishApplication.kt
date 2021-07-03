package com.dewy.musicextractish

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import javax.servlet.http.HttpServletResponse


@SpringBootApplication
@RestController
class MusicExtractishApplication(val webClient: WebClient) {
    @GetMapping("/authorized/spotify")
    fun hello(@RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient, httpServletResponse: HttpServletResponse) {
        val resourceUri = "https://api.spotify.com/v1/me"

        val body = webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        println(body)

        httpServletResponse.setHeader("Location", "http://localhost:3000/home")
        httpServletResponse.status = 302
    }
}

fun main(args: Array<String>) {
    runApplication<MusicExtractishApplication>(*args)
}

@EnableWebSecurity
class OAuth2LoginSecurityConfig : WebSecurityConfigurerAdapter() {
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
//            .authorizeRequests { authorize ->
//                authorize
//                    .anyRequest().authenticated()
//            }
            .oauth2Client()
    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider: OAuth2AuthorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build()
        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository)
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}