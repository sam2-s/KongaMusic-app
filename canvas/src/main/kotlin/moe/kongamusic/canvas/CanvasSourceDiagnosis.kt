/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.canvas

sealed interface CanvasSourceDiagnosis {

    val detail: String

    data class Ok(
        val canvasFound: Boolean,
        override val detail: String,
    ) : CanvasSourceDiagnosis

    data class Rejected(
        val httpStatus: Int?,
        override val detail: String,
    ) : CanvasSourceDiagnosis

    data class Unreachable(
        override val detail: String,
    ) : CanvasSourceDiagnosis

    data class Skipped(
        override val detail: String,
    ) : CanvasSourceDiagnosis
}
