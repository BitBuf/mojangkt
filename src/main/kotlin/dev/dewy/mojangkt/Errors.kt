package dev.dewy.mojangkt

class ErrorResponse(val error: String, val errorMessage: String)

class ForbiddenOperationException(message: String) : RuntimeException(message)