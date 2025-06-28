package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.SignerPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignerPersonRepository extends JpaRepository<SignerPerson, Long> {
}

