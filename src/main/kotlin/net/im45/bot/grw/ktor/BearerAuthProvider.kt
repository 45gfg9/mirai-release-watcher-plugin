package net.im45.bot.grw.ktor

import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*

fun Auth.bearer(block: BearerAuthConfig.() -> Unit) {
    with(BearerAuthConfig().apply(block)) {
        providers.add(BearerAuthProvider(token))
    }
}

class BearerAuthConfig {
    lateinit var token: String
}

class BearerAuthProvider(
    private val token: String,
    override val sendWithoutRequest: Boolean = true
) : AuthProvider {
    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        return auth.authScheme == AuthScheme.OAuth
    }

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.headers[HttpHeaders.Authorization] = "Bearer $token"
    }
}
