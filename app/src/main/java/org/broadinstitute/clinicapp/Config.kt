package org.broadinstitute.clinicapp

object Config {

    const val clientId = BuildConfig.clientId
    const val keycloakBaseUrl = BuildConfig.keycloakBaseUrl
    const val apiBaseUrl = BuildConfig.apiBaseUrl
    const val redirectUri = BuildConfig.redirectUri
    const val host = BuildConfig.host
    const val authenticationCodeUrl = "$keycloakBaseUrl/auth"

}