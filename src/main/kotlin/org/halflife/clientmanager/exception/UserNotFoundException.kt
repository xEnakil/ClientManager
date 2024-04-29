package org.halflife.clientmanager.exception

class UserNotFoundException(id: String) : RuntimeException("Client with ID $id not found.")