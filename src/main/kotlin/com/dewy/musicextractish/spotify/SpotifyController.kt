package com.dewy.musicextractish.spotify

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class SpotifyController(val webClient: WebClient) {
    @GetMapping("/login/oauth2/code/spotify")
    fun redirect(request: HttpServletRequest,
                 response: HttpServletResponse) {

        DefaultRedirectStrategy().sendRedirect(request, response, "http://localhost:3000")
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user/playlists")
    fun getUserPlaylists(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        val resourceUri = "https://api.spotify.com/v1/me/playlists"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    @CrossOrigin("http://localhost:3000", allowCredentials = "true")
    @GetMapping("/spotify/user")
    fun getUserInfo(
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        val resourceUri = "https://api.spotify.com/v1/me"
        return webClient
            .get()
            .uri(resourceUri)
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}