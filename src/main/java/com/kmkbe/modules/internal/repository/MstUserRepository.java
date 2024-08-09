package com.kmkbe.modules.internal.repository;

import com.kmkbe.modules.internal.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MstUserRepository extends JpaRepository<MstUser, UUID> {
}
