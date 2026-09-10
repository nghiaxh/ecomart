package com.ecomart.repository;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.RefreshToken;
import com.ecomart.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired CustomerRepository customerRepository;

    private Customer customer() {
        Customer c = new Customer();
        c.setUsername("minh");
        c.setEmail("minh@ecomart.test");
        c.setNumberPhone("0901234567");
        c.setPasswordHash("encoded");
        c.setRole(UserRole.CUSTOMER);
        return customerRepository.save(c);
    }

    private RefreshToken token(Customer c, String hash, Instant expiresAt) {
        RefreshToken t = new RefreshToken();
        t.setUser(c);
        t.setTokenHash(hash);
        t.setExpiresAt(expiresAt);
        return refreshTokenRepository.save(t);
    }

    @Test
    void findByTokenHashReturnsMatchingToken() {
        Customer c = customer();
        token(c, "hash-a", Instant.now().plusSeconds(3600));

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash("hash-a");

        assertTrue(found.isPresent());
        assertEquals("hash-a", found.get().getTokenHash());
        assertTrue(refreshTokenRepository.findByTokenHash("hash-missing").isEmpty());
    }

    @Test
    void deleteByExpiresAtBeforeRemovesOnlyExpiredTokens() {
        Customer c = customer();
        RefreshToken expired = token(c, "hash-expired", Instant.now().minusSeconds(10));
        RefreshToken valid = token(c, "hash-valid", Instant.now().plusSeconds(3600));

        long deleted = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());

        assertEquals(1L, deleted);
        assertTrue(refreshTokenRepository.findById(expired.getId()).isEmpty());
        assertTrue(refreshTokenRepository.findById(valid.getId()).isPresent());
    }
}