package dev.dewy.mojangkt

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
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