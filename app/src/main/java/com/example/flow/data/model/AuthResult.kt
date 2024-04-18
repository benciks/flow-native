package com.example.flow.data.model

sealed class AuthResult<T>(val data: T? = null) {
    class Authorized<T>(data: T) : AuthResult<T>(data)
    class Unauthorized<T> : AuthResult<T>()
    class UnknownError<T> : AuthResult<T>()
}