package dev.dewy.mojangkt

class PrimitivePlayer(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val demo: Boolean = false
)

class Profile(
    val player: PrimitivePlayer,
    val skin: Skin,
    val capeUrl: String = ""
)

class Skin(
    val url: String,
    val type: SkinType
)

enum class SkinType {
    DEFAULT,
    SLIM
}

class NameHistoryNode(
    val name: String,
    val changedToAt: Long
)

class NameHistory(
    val history: List<NameHistoryNode>
)