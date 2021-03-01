package dev.dewy.mojangkt

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.Base64
import java.util.UUID
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MojangKt {
    private val gson = Gson()

    @Suppress("UNUSED")
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
        "https://api.mojang.com/users/profiles/minecraft/$name"
            .httpGet()
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
        "https://api.mojang.com/profiles/minecraft"
            .httpPost()
            .jsonBody(
                names, gson
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

    suspend fun getProfileFromUuid(uuid: String): Profile = suspendCoroutine { cont ->
        "https://sessionserver.mojang.com/session/minecraft/profile/$uuid"
            .httpGet()
            .responseString {_, _, result ->
                when (result) {
                    is Result.Failure -> {
                        cont.resumeWithException(result.getException())
                    }

                    is Result.Success -> {
                        val obj = gson.fromJson(result.value, JsonObject::class.java)
                        val encodedProperties = obj["properties"].asJsonArray[0].asJsonObject["value"].asString

                        val id = obj["id"].asString
                        val name = obj["name"].asString
                        val legacy = obj.has("legacy")

                        var skinUrl = ""
                        var skinType = getSkinType(id)

                        var capeUrl = ""

                        if (encodedProperties != null) {
                            val texturesObj = gson.fromJson(String(Base64.getDecoder()
                                .decode(encodedProperties)), JsonObject::class.java)
                                .getAsJsonObject("textures")

                            val skinObj = texturesObj.getAsJsonObject("SKIN")
                            val capeObj = texturesObj.getAsJsonObject("CAPE")

                            if (skinObj != null) {
                                skinUrl = skinObj["url"].asString

                                skinType = if (skinObj.has("metadata")) SkinType.SLIM else SkinType.DEFAULT
                            }

                            if (capeObj != null) {
                                capeUrl = capeObj["url"].asString
                            }
                        }

                        cont.resume(Profile(PrimitivePlayer(id, name, legacy), Skin(skinUrl, skinType), capeUrl))
                    }
                }
            }
    }

    suspend fun getNameHistory(uuid: String): NameHistory = suspendCoroutine { cont ->
        "https://api.mojang.com/user/profiles/$uuid/names"
            .httpGet()
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

    suspend fun changeName(name: String) = suspendCoroutine<Unit> { cont ->
        "https://api.minecraftservices.com/minecraft/profile/name/$name"
            .httpPut()
            .response { _, response, result ->
                when (result) {
                    is Result.Failure -> {
                        when (response.statusCode) {
                            400 -> cont.resumeWithException(InvalidNameException("Name must follow Mojang's name rules."))
                            401 -> cont.resumeWithException(UnauthorizedAccessException("Token expired or incorrect."))
                            403 -> cont.resumeWithException(UnavailableNameException("Name either taken or is in some other way unavailable."))
                            500 -> cont.resumeWithException(TimedOutException("Timed out."))
                        }
                    }

                    is Result.Success -> {
                        cont.resume(Unit)
                    }
                }
            }
    }

    suspend fun getBlockedServers(): List<String> = suspendCoroutine { cont ->
        "https://sessionserver.mojang.com/blockedservers"
            .httpGet()
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

    @Suppress("NAME_SHADOWING")
    private fun getSkinType(uuid: String): SkinType {
        val uuid = UUID.fromString(Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
            .matcher(uuid.replace("-", "")).replaceAll("$1-$2-$3-$4-$5"))

        return if ((uuid.hashCode() and 1) != 0)
            SkinType.SLIM
        else
            SkinType.DEFAULT
    }
}