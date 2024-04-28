package org.halflife.clientmanager.controller

import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.FullClientResponse
import org.halflife.clientmanager.model.Client
import org.halflife.clientmanager.model.Role
import org.halflife.clientmanager.service.AdminService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/admin")
class AdminController(private val adminService: AdminService) {

    @PostMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    fun addClient(@RequestBody clientRequest: ClientRequest) : ResponseEntity<ClientResponse> {
        return ResponseEntity.ok(adminService.addClient(clientRequest)?.toResponse())    }

    @GetMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getClientById(@PathVariable id: String): ResponseEntity<ClientResponse> {
        return ResponseEntity.ok(adminService.getClientById(id)?.toResponse())    }

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    fun getClients(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): ResponseEntity<Page<FullClientResponse>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val clientPage = adminService.getClients(pageable)

        val clientResponsePage = clientPage.map { client ->
            FullClientResponse(
                id = client.id.toString(),
                email = client.email,
                firstName = client.firstName,
                lastName = client.lastName,
                job = client.job.toString(),
                position = client.position.toString(),
                role = client.role.toString(),
                gender = client.gender.toString()
            )
        }
        return ResponseEntity.ok(clientResponsePage)
    }

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteClient(@PathVariable id: UUID): ResponseEntity<Any> {
        adminService.removeClient(id)
        return ResponseEntity.noContent().build<Any>()
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    fun search(@RequestParam search: String) : ResponseEntity<List<ClientResponse>> {
        val clients = adminService.search(search)
        return ResponseEntity.ok(clients.map { clients -> clients.toResponse()})
    }

    private fun ClientRequest.toModel(): Client =
        Client(
            email = this.email,
            password = this.password,
            firstName = this.firstName,
            lastName = this.lastName,
            gender = this.gender,
            role = Role.USER,
            job = this.job,
            position = this.position
        )

    private fun Client.toResponse(): ClientResponse =
        ClientResponse(
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            gender = this.gender,
            job = this.job,
            position = this.position
        )
}