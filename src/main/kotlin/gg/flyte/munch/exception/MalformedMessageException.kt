package gg.flyte.munch.exception

class MalformedMessageException : IllegalArgumentException("Messages must have a defined sender uid.")