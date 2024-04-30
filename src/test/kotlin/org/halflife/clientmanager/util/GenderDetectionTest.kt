package org.halflife.clientmanager.util

import org.halflife.clientmanager.dto.response.GenderResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate

@ActiveProfiles("test")
@SpringBootTest
class GenderDetectionTests {

    @Autowired
    private lateinit var genderDetection: GenderDetection

    @MockBean
    private lateinit var restTemplate: RestTemplate

    @Test
    fun `test getGender returns valid response`() {
        val name = "Alice"
        val expectedResponse = GenderResponse(name,gender = "female", probability = 0.98, count = 2338)
        given(restTemplate.getForObject("https://api.genderize.io/?name=$name", GenderResponse::class.java))
            .willReturn(expectedResponse)

        val result = genderDetection.getGender(name)

        assertNotNull(result)
        assertEquals("female", result?.gender)
        assertEquals(0.98, result?.probability)
        assertEquals(2338, result?.count)
    }
}