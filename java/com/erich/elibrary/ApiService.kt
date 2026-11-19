package com.erich.elibrary

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("guest/start") suspend fun startGuest(): GuestResponse
    @POST("register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @POST("login") suspend fun login(@Body body: LoginRequest): AuthResponse
    @POST("logout") suspend fun logout(): ApiMessage
    @GET("me") suspend fun me(): MeResponse

    @GET("books") suspend fun books(
        @Query("q") q: String?,
        @Query("category") category: String?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20,
    ): BookPage

    @GET("categories") suspend fun categories(): List<Category>
    @GET("books/{id}/download") suspend fun download(@Path("id") id: Int): DownloadResponse
    @GET("downloads/status") suspend fun status(): DownloadStatus

    // My Library (members only). The favorite and delete calls take the BOOK id.
    @GET("library") suspend fun library(): List<LibraryItem>
    @POST("library") suspend fun saveToLibrary(@Body body: SaveRequest): LibraryItem
    @PATCH("library/{bookId}/favorite") suspend fun toggleFavorite(@Path("bookId") bookId: Int): LibraryItem
    @DELETE("library/{bookId}") suspend fun removeFromLibrary(@Path("bookId") bookId: Int): ApiMessage
}
