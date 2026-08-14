package com.ecomart.repository;

import com.ecomart.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerIdOrderByIsDefaultDesc(Long customerId);
    List<Address> findByCustomerId(Long customerId);
}
