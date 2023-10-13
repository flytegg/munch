package gg.joshbaker.munch.exception

class MalformedConnectionException : IllegalArgumentException("Connections must have a defined sender uid.") {
}