package com.aayush.authforge.authfordgeapi.role.repositories;

import com.aayush.authforge.authfordgeapi.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
