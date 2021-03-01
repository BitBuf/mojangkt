package dev.dewy.mojangkt

open class MojangApiException(message: String) : RuntimeException(message)

class InvalidNameException(message: String) : MojangApiException(message)

class UnavailableNameException(message: String) : MojangApiException(message)

class UnauthorizedAccessException(message: String) : MojangApiException(message)

class TimedOutException(message: String) : MojangApiException(message)