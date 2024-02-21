package gg.flyte.munch.exception

import java.util.UUID

class UnknownServerException(serverUid: UUID) : MunchException("Received message from unknown server with uid $serverUid.")