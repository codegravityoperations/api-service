package com.codegravity.itconsultancy.repository;

import com.codegravity.itconsultancy.entity.RefreshToken;
import com.codegravity.itconsultancy.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userEmail = :email AND rt.userType = :userType AND rt.revoked = false")
    void revokeAllByEmailAndUserType(String email, UserType userType);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
    void deleteAllRevoked();
}