package gg.flyte.munch.exception

import java.util.UUID

class UnknownServerException(serverId: UUID) : MunchException("Received message from unknown server with ID $serverId.")