package com.diary.moonpage.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.UUID

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SpotifyApi {
    @GET("https://api.spotify.com/v1/search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 20
    ): Response<SpotifySearchResponse>

    @GET("https://api.spotify.com/v1/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<SpotifyUserResponse>

    @GET("https://api.spotify.com/v1/me/top/tracks")
    suspend fun getTopTracks(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<SpotifyTopTracksResponse>

    @GET("https://api.spotify.com/v1/me/player/recently-played")
    suspend fun getRecentlyPlayedTracks(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<SpotifyRecentlyPlayedResponse>

    @FormUrlEncoded
    @POST("https://accounts.spotify.com/api/token")
    suspend fun exchangeToken(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String
    ): Response<SpotifyTokenResponse>

    @FormUrlEncoded
    @POST("https://accounts.spotify.com/api/token")
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<SpotifyTokenResponse>

    companion object {
        const val CLIENT_ID = "61d0e03380e44e42b515534d6d133598"
        const val REDIRECT_URI = "moonpage://spotify-callback/"
        const val AUTH_URL = "https://accounts.spotify.com/authorize"
        
        fun getAuthUrl(codeChallenge: String, state: String): String {
            val encodedRedirectUri = android.net.Uri.encode(REDIRECT_URI)
            return "$AUTH_URL?client_id=$CLIENT_ID" +
                    "&response_type=code" +
                    "&redirect_uri=$encodedRedirectUri" +
                    "&scope=user-read-private%20user-read-email%20user-read-recently-played%20user-top-read" +
                    "&show_dialog=true" +
                    "&state=$state" +
                    "&code_challenge_method=S256" +
                    "&code_challenge=$codeChallenge"
        }
    }
}

data class SpotifyUserResponse(
    val id: String,
    val product: String?
)

data class SpotifyTopTracksResponse(
    val items: List<SpotifyTrack>
)

data class SpotifyRecentlyPlayedResponse(
    val items: List<SpotifyRecentlyPlayedItem>
)

data class SpotifyRecentlyPlayedItem(
    val track: SpotifyTrack
)

data class SpotifyTokenResponse(
    @com.google.gson.annotations.SerializedName("access_token") val accessToken: String,
    @com.google.gson.annotations.SerializedName("token_type") val tokenType: String,
    @com.google.gson.annotations.SerializedName("expires_in") val expiresIn: Int,
    @com.google.gson.annotations.SerializedName("refresh_token") val refreshToken: String?,
    @com.google.gson.annotations.SerializedName("scope") val scope: String
)

data class SpotifySearchResponse(
    val tracks: SpotifyTracks
)

data class SpotifyTracks(
    val items: List<SpotifyTrack>
)

data class SpotifyExternalUrls(
    val spotify: String
)

data class SpotifyTrack(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum,
    val duration_ms: Long,
    val preview_url: String?,
    @com.google.gson.annotations.SerializedName("external_urls") val externalUrls: SpotifyExternalUrls
)

data class SpotifyArtist(
    val name: String
)

data class SpotifyAlbum(
    val name: String,
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    val url: String,
    val height: Int,
    val width: Int
)
