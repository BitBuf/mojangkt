package dev.dewy.mojangkt

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MojangKt {
    var token = ""
        set(value) {
            field = value

            if (value.isEmpty()) {
                FuelManager.instance.baseHeaders = emptyMap()
                return
            }

            FuelManager.instance.baseHeaders = mapOf(
                "Authorization" to "Bearer $value"
            )
        }

    suspend fun getPlayerFromName(name: String): PrimitivePlayer = suspendCoroutine { cont ->
        "https://api.mojang.com/users/profiles/minecraft/$name".httpGet()
            .responseObject<PrimitivePlayer> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        cont.resumeWithException(result.getException())
                    }

                    is Result.Success -> {
                        cont.resume(result.value)
                    }
                }
            }
    }

    suspend fun getPlayersFromNames(names: List<String>): List<PrimitivePlayer> = suspendCoroutine { cont ->
        "https://api.mojang.com/profiles/minecraft".httpPost()
            .jsonBody(
                names, Gson()
            )
            .responseObject<List<PrimitivePlayer>> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        cont.resumeWithException(result.getException())
                    }

                    is Result.Success -> {
                        cont.resume(result.value)
                    }
                }
            }
    }

    suspend fun getNameHistory(uuid: String): NameHistory = suspendCoroutine { cont ->
        "https://api.mojang.com/user/profiles/$uuid/names".httpGet()
            .responseObject<List<NameHistoryNode>> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        cont.resumeWithException(result.getException())
                    }

                    is Result.Success -> {
                        cont.resume(NameHistory(result.value))
                    }
                }
            }
    }

    suspend fun getBlockedServers(): List<String> = suspendCoroutine { cont ->
        "https://sessionserver.mojang.com/blockedservers".httpGet()
            .responseString { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        cont.resumeWithException(result.getException())
                    }

                    is Result.Success -> {
                        cont.resume(result.value.split("\n"))
                    }
                }
            }
    }
}