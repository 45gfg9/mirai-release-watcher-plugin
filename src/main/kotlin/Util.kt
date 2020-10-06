import org.jetbrains.annotations.Contract
import kotlin.math.log10
import kotlin.math.pow

private val SUFFIXES = charArrayOf('K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D')

@Contract(pure = true)
fun humanReadableSize(bytes: Long): String {
    if (bytes < 0)
        throw IllegalArgumentException("Negative size")

    val scale = (log10(bytes.toDouble()) / 3).toInt()

    if (scale == 0) return "${bytes}B"
    return String.format("%.2f", bytes / 1e3.pow(scale)) + SUFFIXES[scale - 1] + "B"
}

@Contract(pure = true)
fun toLegalId(repoId: RepoId): String {
    return repoId.toString()
        .replaceFirst(Regex("^(\\d)"), "_$1")
        .replace(Regex("[-/.]"), "_")
}
