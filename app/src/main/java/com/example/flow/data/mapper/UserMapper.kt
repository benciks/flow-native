package com.example.flow.data.mapper

import com.example.flow.MeQuery
import com.example.flow.SignInMutation
import com.example.flow.SignUpMutation
import com.example.flow.data.model.User

fun SignInMutation.SignIn.toUser(): User {
    return User(
        id = user.id.toInt(),
        username = user.username,
        timewHook = user.timewHook
    )
}

fun SignUpMutation.SignUp.toUser(): User {
    return User(
        id = user.id.toInt(),
        username = user.username,
        timewHook = user.timewHook
    )
}

fun MeQuery.Me.toUser(): User {
    return User(
        id = id.toInt(),
        username = username,
        timewHook = timewHook
    )
}
