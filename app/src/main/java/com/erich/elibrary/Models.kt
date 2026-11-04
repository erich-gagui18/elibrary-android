package com.erich.elibrary

import com.google.gson.annotations.SerializedName

// Shapes match the Laravel API (elibrary-api).

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val category: String,
    @SerializedName("cover_url") val coverUrl: String?,
)

// Laravel paginator: GET /books
data class BookPage(
    val data: List<Book>,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int,
)

data class Category(val category: String, val total: Int)

data class User(val id: Int, val name: String, val email: String)

data class AuthResponse(val user: User, val token: String)
data class MeResponse(val user: User)
data class GuestResponse(val token: String)

data class DownloadStatus(val limit: Int, val used: Int, val remaining: Int)

data class DownloadResponse(
    @SerializedName("download_url") val downloadUrl: String,
    val limit: Int,
    val used: Int,
    val remaining: Int,
)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
)

// Error body: { message } or Laravel validation { message, errors: { field: [..] } }
data class ApiMessage(val message: String?, val errors: Map<String, List<String>>?)
