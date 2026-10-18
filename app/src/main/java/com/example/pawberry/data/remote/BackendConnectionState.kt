package com.example.pawberry.data.remote

sealed class BackendConnectionState {
    data object Checking : BackendConnectionState()
    data class Reachable(val message: String) : BackendConnectionState()
    data class Unreachable(val reason: String) : BackendConnectionState()
}
