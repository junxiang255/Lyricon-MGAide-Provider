package io.github.proify.lyricon.mgaprovider

import android.content.Context
import io.github.proify.lyricon.lyric.model.LyricWord
import io.github.proify.lyricon.lyric.model.RichLyricLine
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.LyriconFactory
import io.github.proify.lyricon.provider.LyriconProvider
import io.github.proify.lyricon.provider.providerMetadataOf

/**
 * 与词慕（Lyricon）的桥：创建 provider、注册、推送歌曲与进度。
 */
object ProviderBridge {

    private var provider: LyriconProvider? = null
    private var lastSongKey: String? = null
    private var lastPlaying: Boolean? = null
    private var lastPosition: Long = Long.MIN_VALUE
    private var lastRegisterAttemptAt = 0L

    @Synchronized
    fun ensure(context: Context): LyriconProvider? {
        provider?.let { p ->
            if (!runCatching { p.service.isActive }.getOrDefault(false)) {
                val now = System.currentTimeMillis()
                if (now - lastRegisterAttemptAt > 5_000L) {
                    lastRegisterAttemptAt = now
                    runCatching { p.register() }
                }
            }
            return p
        }

        return runCatching {
            val p = LyriconFactory.createProvider(
                context = context.applicationContext,
                providerPackageName = Constants.MODULE_PACKAGE,
                playerPackageName = Constants.TARGET_PACKAGE,
                metadata = providerMetadataOf(
                    "name" to "毛怪助手",
                    "description" to "MG Aide 状态栏歌词",
                ),
            )
            runCatching { p.player.setDisplayTranslation(true) }
            runCatching { p.autoSync = true }
            p.register()
            provider = p
            MLog.i("Lyricon provider created & register() sent")
            p
        }.onFailure { MLog.e("create provider failed", it) }.getOrNull()
    }

    @Synchronized
    fun push(snapshot: WSnapshot) {
        val p = provider ?: return
        val player = p.player
        val song = snapshot.song

        if (song == null) {
            clearLocked()
            return
        }

        if (song.key != lastSongKey) {
            runCatching { player.setSong(song.toModel()) }
                .onFailure { MLog.e("setSong failed", it) }
            lastSongKey = song.key
            lastPlaying = null
            lastPosition = Long.MIN_VALUE
        }

        if (lastPlaying != snapshot.playing) {
            runCatching { player.setPlaybackState(snapshot.playing) }
            lastPlaying = snapshot.playing
        }

        val position = snapshot.positionMs.coerceAtLeast(0L)
        if (position != lastPosition) {
            runCatching { player.setPosition(position) }
            lastPosition = position
        }
    }

    /** 关闭开关后清空词慕上的歌词 */
    @Synchronized
    fun clear() {
        clearLocked()
    }

    private fun clearLocked() {
        val p = provider ?: return
        if (lastSongKey == null) return
        runCatching { p.player.setSong(null) }
        lastSongKey = null
        lastPlaying = null
        lastPosition = Long.MIN_VALUE
    }
}

private fun WSong.toModel(): Song = Song(
    id = key,
    name = title.ifBlank { null },
    artist = artist.ifBlank { null },
    duration = durationMs,
    lyrics = lines.map { line ->
        RichLyricLine(
            begin = line.begin,
            end = line.end,
            text = line.text,
            secondary = line.secondary,
            words = line.words.takeIf { it.isNotEmpty() }?.map { word ->
                LyricWord(begin = word.begin, end = word.end, text = word.text)
            },
        )
    }.takeIf { it.isNotEmpty() },
)
