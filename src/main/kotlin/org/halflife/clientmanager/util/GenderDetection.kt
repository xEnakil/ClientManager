package org.halflife.clientmanager.util

import org.halflife.clientmanager.dto.response.GenderResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GenderDetection(private val restTemplate: RestTemplate) {

    fun getGender(name: String): GenderResponse? {
        val url = "https://api.genderize.io/?name=$name"
        return restTemplate.getForObject(url, GenderResponse::class.java)
    }
}