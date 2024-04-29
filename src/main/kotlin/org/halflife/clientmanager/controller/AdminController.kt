package org.halflife.clientmanager.controller

import org.halflife.clientmanager.dto.request.ClientRequest
import org.halflife.clientmanager.dto.response.ClientResponse
import org.halflife.clientmanager.dto.response.FullClientResponse
import org.halflife.clientmanager.mapper.ClientMapper
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
class AdminController(
    private val adminService: AdminService,
    private val clientMapper: ClientMapper,
) {

    @PostMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    fun addClient(@RequestBody clientRequest: ClientRequest) : ResponseEntity<ClientResponse> {
        return ResponseEntity.ok(adminService.addClient(clientRequest)?.let { clientMapper.toResponse(it) })
    }

    @GetMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getClientById(@PathVariable id: String): ResponseEntity<ClientResponse> {
        return ResponseEntity.ok(adminService.getClientById(id)?.let { clientMapper.toResponse(it) })
    }

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    fun getClients(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): ResponseEntity<Page<FullClientResponse>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val clientPage = adminService.getClients(pageable)

        val clientResponsePage = clientPage.map { client -> clientMapper.toFullResponse(client)}
        return ResponseEntity.ok(clientResponsePage)
    }

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteClient(@PathVariable id: UUID): ResponseEntity<Map<String, String>> {
        adminService.removeClient(id)
        val response = mapOf("message" to "Client with $id has been deleted")
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    fun search(@RequestParam search: String) : ResponseEntity<List<ClientResponse>> {
        val clients = adminService.search(search)
        return ResponseEntity.ok(clients.map { client -> clientMapper.toResponse(client)})
    }
}