/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.lastfm.models

import kotlinx.serialization.Serializable

@Serializable
data class Authentication(
    val session: Session,
) {
    @Serializable
    data class Session(
        val name: String,
        val key: String,
        val subscriber: Int,
    )
}

@Serializable
data class TokenResponse(
    val token: String,
)

@Serializable
data class LastFmError(
    val error: Int,
    val message: String,
)
