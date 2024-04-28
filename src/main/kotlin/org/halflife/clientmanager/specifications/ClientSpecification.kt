package org.halflife.clientmanager.specifications

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.halflife.clientmanager.model.Client
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class ClientSpecifications {
    fun search(search: String): Specification<Client> {
        return Specification { root: Root<Client>, query, cb: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = mutableListOf()
            val terms = search.split(" ")

            terms.forEach { term ->
                val predicatesForTerm: MutableList<Predicate> = mutableListOf()
                predicatesForTerm.add(cb.like(cb.lower(root.get("firstName")), "%${term.lowercase(Locale.getDefault())}%"))
                predicatesForTerm.add(cb.like(cb.lower(root.get("lastName")), "%${term.lowercase(Locale.getDefault())}%"))
                predicates.add(cb.or(*predicatesForTerm.toTypedArray()))
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}