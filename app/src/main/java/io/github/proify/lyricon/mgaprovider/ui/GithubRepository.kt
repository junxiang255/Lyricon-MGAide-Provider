package io.github.proify.lyricon.mgaprovider.ui

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** junxiang255 的 GitHub 项目 */
data class GithubProject(
    val name: String,
    val description: String,
    val language: String,
    val stars: Int,
    val isFork: Boolean,
    val url: String,
)

/**
 * GitHub 项目数据：每天最多请求一次 API，其余时间读本地缓存（filesDir）。
 */
object GithubRepository {

    private const val TAG = "MGLyricProvider/Github"

    const val LOGIN = "junxiang255"
    const val NICKNAME = "俊翔不会玩"
    const val BIO = "新来的毛怪"
    const val PROFILE_URL = "https://github.com/junxiang255"

    private const val CACHE_FILE = "github_repos.json"
    private const val TIME_FILE = "github_repos.fetched_at"
    private const val TTL_MS = 24L * 60 * 60 * 1000L

    private const val API_URL =
        "https://api.github.com/users/junxiang255/repos?sort=updated&per_page=30"

    private fun cacheFile(context: Context) = File(context.applicationContext.filesDir, CACHE_FILE)
    private fun timeFile(context: Context) = File(context.applicationContext.filesDir, TIME_FILE)

    /** 本地缓存（可能为空） */
    fun cached(context: Context): List<GithubProject> {
        return try {
            val file = cacheFile(context)
            if (!file.exists()) return emptyList()
            parse(String(file.readBytes(), Charsets.UTF_8))
        } catch (t: Throwable) {
            Log.w(TAG, "read cache failed", t)
            emptyList()
        }
    }

    fun lastFetchedAt(context: Context): Long {
        return try {
            val file = timeFile(context)
            if (!file.exists()) return 0L
            String(file.readBytes(), Charsets.UTF_8).trim().toLongOrNull() ?: 0L
        } catch (t: Throwable) {
            0L
        }
    }

    /** 距上次拉取是否已超过 24 小时（无缓存时为 true） */
    fun shouldRefresh(context: Context): Boolean {
        val last = lastFetchedAt(context)
        if (last <= 0L) return true
        return System.currentTimeMillis() - last > TTL_MS
    }

    /** 请求 GitHub API；成功后写入缓存。需在 IO 线程调用。 */
    fun refresh(context: Context): List<GithubProject>? {
        val body = try {
            fetchBody()
        } catch (t: Throwable) {
            Log.w(TAG, "fetch failed", t)
            return null
        }

        val list = try {
            parse(body)
        } catch (t: Throwable) {
            Log.w(TAG, "parse failed", t)
            return null
        }

        if (list.isNotEmpty()) {
            try {
                cacheFile(context).writeBytes(body.toByteArray(Charsets.UTF_8))
                timeFile(context).writeBytes(System.currentTimeMillis().toString().toByteArray())
                Log.i(TAG, "cached ${list.size} repos")
            } catch (t: Throwable) {
                Log.w(TAG, "write cache failed", t)
            }
        }
        return list
    }

    private fun fetchBody(): String {
        val conn = URL(API_URL).openConnection() as HttpURLConnection
        conn.connectTimeout = 8_000
        conn.readTimeout = 8_000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "lyricon-mgaprovider")

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        var body = ""
        if (stream != null) {
            val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            try {
                body = reader.readText()
            } finally {
                reader.close()
            }
        }
        conn.disconnect()

        if (code !in 200..299) throw IllegalStateException("HTTP $code")
        return body
    }

    private fun parse(json: String): List<GithubProject> {
        val array = JSONArray(json)
        val result = ArrayList<GithubProject>(array.length())
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index) ?: continue
            val name = obj.optString("name")
            if (name.isBlank()) continue
            result.add(
                GithubProject(
                    name = name,
                    description = obj.optString("description").ifBlank { "暂无描述" },
                    language = obj.optString("language").ifBlank { "—" },
                    stars = obj.optInt("stargazers_count"),
                    isFork = obj.optBoolean("fork"),
                    url = obj.optString("html_url", "https://github.com/$LOGIN/$name"),
                )
            )
        }
        return result
    }
}
