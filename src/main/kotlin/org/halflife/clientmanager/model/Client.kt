package org.halflife.clientmanager.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import java.util.UUID

@Entity
@Table(name = "clients")
@AllArgsConstructor
@NoArgsConstructor
 class Client(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id : UUID = UUID.randomUUID(),
    val email : String,
    var password : String,
    val firstName : String,
    val lastName : String,
    val role: Role,
    val job: String? = null,
    val position: String? = null,
    var gender: String? = null,
)

enum class Role {
    USER, ADMIN
}
