package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Address;
import com.ecomart.domain.entity.Customer;
import com.ecomart.dto.request.AddressRequest;
import com.ecomart.dto.response.AddressResponse;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock SecurityUtils securityUtils;
    @Mock AddressRepository addressRepository;

    private AddressService service;

    @BeforeEach
    void setUp() {
        service = new AddressService(securityUtils, addressRepository);
    }

    private Customer customer(long id) {
        Customer c = new Customer();
        c.setId(id);
        return c;
    }

    private Address address(long id, long customerId, boolean defaultAddress) {
        Address a = new Address();
        a.setId(id);
        a.setCustomer(customer(customerId));
        a.setDefault(defaultAddress);
        return a;
    }

    private AddressRequest request(boolean defaultAddress) {
        return new AddressRequest("Nha rieng", "12 Nguyen Hue", "Ben Nghe", "Q1", "HCM",
                "Nguyen Van A", "0901234567", defaultAddress);
    }

    @Test
    void createDefaultClearsOtherDefaults() {
        Address oldDefault = address(1L, 5L, true);
        Address other = address(2L, 5L, false);
        when(securityUtils.currentUser()).thenReturn(customer(5L));
        when(addressRepository.findByCustomerId(5L)).thenReturn(List.of(oldDefault, other));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = service.create(request(true));

        assertTrue(response.isDefault());
        assertFalse(oldDefault.isDefault());
        verify(addressRepository).save(oldDefault);
    }

    @Test
    void createNonDefaultKeepsExistingDefault() {
        Address oldDefault = address(1L, 5L, true);
        when(securityUtils.currentUser()).thenReturn(customer(5L));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = service.create(request(false));

        assertFalse(response.isDefault());
        assertTrue(oldDefault.isDefault());
    }

    @Test
    void updateForeignAddressThrowsNotFound() {
        when(securityUtils.currentUser()).thenReturn(customer(5L));
        when(addressRepository.findById(9L)).thenReturn(Optional.of(address(9L, 99L, false)));

        assertThrows(ResourceNotFoundException.class, () -> service.update(9L, request(false)));
    }

    @Test
    void setDefaultSwitchesFromOtherDefault() {
        Address owned = address(2L, 5L, false);
        Address otherDefault = address(1L, 5L, true);
        when(securityUtils.currentUser()).thenReturn(customer(5L));
        when(addressRepository.findById(2L)).thenReturn(Optional.of(owned));
        when(addressRepository.findByCustomerId(5L)).thenReturn(List.of(otherDefault, owned));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = service.setDefault(2L);

        assertTrue(response.isDefault());
        assertTrue(owned.isDefault());
        assertFalse(otherDefault.isDefault());
        verify(addressRepository).save(otherDefault);
    }
}