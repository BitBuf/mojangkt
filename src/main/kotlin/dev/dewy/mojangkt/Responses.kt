package dev.dewy.mojangkt

class PrimitivePlayer(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val demo: Boolean = false
)

class NameHistoryNode(
    val name: String,
    val changedToAt: Long
)

class NameHistory(
    val history: List<NameHistoryNode>
)