package org.halflife.clientmanager.exception

class EmailAlreadyInUseException(email: String) : RuntimeException("Email already in use: $email")