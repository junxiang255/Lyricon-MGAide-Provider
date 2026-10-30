package io.github.proify.lyricon.mgaprovider

import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/** 一行歌词 */
data class WLine(
    val begin: Long,
    val end: Long,
    val text: String,
    val secondary: String?,
    val words: List<WWord>,
)

/** 一个逐字单元 */
data class WWord(val text: String, val begin: Long, val end: Long)

/** 一首歌 */
data class WSong(
    val key: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val lines: List<WLine>,
)

/** 一次状态快照 */
data class WSnapshot(
    val song: WSong?,
    val positionMs: Long,
    val playing: Boolean,
)

/**
 * 从毛怪助手的 MusicViewModel 读取播放状态。
 *
 * 目标类 `com.sennagiapp.mgaide.viewmodel.MusicViewModel` 未被混淆，公开的
 * StateFlow 字段名（currentSong / progress / lyricsLines / isPlaying）保持原样，
 * 因此这里用反射读取即可。
 */
object MusicState {

    private val fieldCache = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Field>>()
    private val methodCache = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Method>>()

    @Volatile
    private var vmRef: WeakReference<Any>? = null

    fun capture(vm: Any) {
        vmRef = WeakReference(vm)
    }

    fun isReady(): Boolean = vmRef?.get() != null

    private fun field(cls: Class<*>, name: String): Field? {
        val map = fieldCache.getOrPut(cls) { ConcurrentHashMap() }
        map[name]?.let { return it }
        return runCatching {
            cls.getDeclaredField(name).apply { isAccessible = true }
        }.getOrNull()?.also { map[name] = it }
    }

    private fun readField(target: Any, name: String): Any? =
        runCatching { field(target.javaClass, name)?.get(target) }.getOrNull()

    private fun method(cls: Class<*>, name: String, vararg params: Class<*>): Method? {
        val map = methodCache.getOrPut(cls) { ConcurrentHashMap() }
        val key = name + params.joinToString(",") { it.name }
        map[key]?.let { return it }
        return runCatching {
            cls.getMethod(name, *params).apply { isAccessible = true }
        }.getOrNull()?.also { map[key] = it }
    }

    /** 读取 StateFlow 的当前值 */
    private fun flowValue(vm: Any, name: String): Any? {
        val flow = readField(vm, name) ?: return null
        val getter = runCatching {
            flow.javaClass.methods.first { it.name == "getValue" && it.parameterCount == 0 }
        }.getOrNull() ?: return null
        return runCatching { getter.invoke(flow) }.getOrNull()
    }

    fun snapshot(): WSnapshot? {
        val vm = vmRef?.get() ?: return null
        val item = flowValue(vm, "currentSong")
        val playing = flowValue(vm, "isPlaying") as? Boolean ?: false
        val position = (flowValue(vm, "progress") as? Number)?.toLong() ?: 0L
        val rawLines = flowValue(vm, "lyricsLines") as? List<*> ?: emptyList<Any>()
        val vmDuration = (flowValue(vm, "duration") as? Number)?.toLong() ?: 0L
        val song = item?.let { buildSong(it, rawLines, vmDuration) }
        return WSnapshot(song, position, playing)
    }

    private fun callString(target: Any, name: String): String? =
        runCatching { method(target.javaClass, name)?.invoke(target) as? String }.getOrNull()

    private fun buildSong(item: Any, rawLines: List<*>, fallbackDurationMs: Long): WSong {
        val title = callString(item, "getTitle").orEmpty()
        val artist = callString(item, "getArtist").orEmpty()
        val durationMs = parseDuration(callString(item, "getDuration")).takeIf { it > 0 }
            ?: fallbackDurationMs
        val lines = mapLines(rawLines, durationMs)
        val key = "$title\u0001$artist\u0001${lines.size}\u0001${lines.firstOrNull()?.text}\u0001${lines.lastOrNull()?.text}"
        return WSong(key, title, artist, durationMs, lines)
    }

    /** "03:45" / "225" / "1:02:03" -> 毫秒 */
    private fun parseDuration(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val parts = raw.trim().split(":")
        return runCatching {
            val seconds = when (parts.size) {
                1 -> parts[0].toDouble()
                2 -> parts[0].toDouble() * 60 + parts[1].toDouble()
                else -> parts[0].toDouble() * 3600 + parts[1].toDouble() * 60 + parts[2].toDouble()
            }
            (seconds * 1000).toLong()
        }.getOrDefault(0L)
    }

    private fun mapLines(rawLines: List<*>, durationMs: Long): List<WLine> {
        if (rawLines.isEmpty()) return emptyList()

        val items = rawLines.mapNotNull { raw ->
            if (raw == null) return@mapNotNull null
            val time = (readField(raw, "time") as? Number)?.toLong() ?: return@mapNotNull null
            val text = (readField(raw, "mainText") as? String).orEmpty()
            if (text.isBlank()) return@mapNotNull null
            val secondary = (readField(raw, "subText") as? String)?.takeIf { it.isNotBlank() }
            val words = (readField(raw, "words") as? List<*>)?.mapNotNull { w ->
                if (w == null) return@mapNotNull null
                val wText = (readField(w, "text") as? String).orEmpty()
                if (wText.isEmpty()) return@mapNotNull null
                WWord(
                    text = wText,
                    begin = (readField(w, "startTime") as? Number)?.toLong() ?: 0L,
                    end = (readField(w, "endTime") as? Number)?.toLong() ?: 0L,
                )
            } ?: emptyList()
            Triple(time, text, secondary) to words
        }.sortedBy { it.first.first }

        if (items.isEmpty()) return emptyList()

        return items.mapIndexed { index, (core, words) ->
            val (time, text, secondary) = core
            val nextBegin = items.getOrNull(index + 1)?.first?.first
            val end = when {
                nextBegin != null && nextBegin > time -> nextBegin
                durationMs > time -> durationMs
                else -> time + 5_000L
            }
            WLine(
                begin = time,
                end = end,
                text = text,
                secondary = secondary,
                words = words.filter { it.end > it.begin }.sortedBy { it.begin },
            )
        }
    }
}
