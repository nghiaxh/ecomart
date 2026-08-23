package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Address;
import com.ecomart.domain.entity.Customer;
import com.ecomart.dto.request.AddressRequest;
import com.ecomart.dto.response.AddressResponse;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final SecurityUtils securityUtils;
    private final AddressRepository addressRepository;

    public AddressService(SecurityUtils securityUtils, AddressRepository addressRepository) {
        this.securityUtils = securityUtils;
        this.addressRepository = addressRepository;
    }

    public List<AddressResponse> myAddresses() {
        Long userId = securityUtils.currentUserId();
        return addressRepository.findByCustomerIdOrderByIsDefaultDesc(userId).stream()
                .map(Mapper::toAddress)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        Customer customer = (Customer) securityUtils.currentUser();
        Address address = new Address();
        address.setCustomer(customer);
        apply(address, request);
        if (request.isDefault()) {
            clearDefault(customer.getId());
        }
        return Mapper.toAddress(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        Customer customer = (Customer) securityUtils.currentUser();
        Address address = getOwned(id, customer.getId());
        apply(address, request);
        if (request.isDefault()) {
            clearDefault(customer.getId());
        }
        return Mapper.toAddress(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = (Customer) securityUtils.currentUser();
        Address address = getOwned(id, customer.getId());
        addressRepository.delete(address);
    }

    @Transactional
    public void setDefault(Long id) {
        Customer customer = (Customer) securityUtils.currentUser();
        Address address = getOwned(id, customer.getId());
        clearDefault(customer.getId());
        address.setDefault(true);
        addressRepository.save(address);
    }

    public Address getOwned(Long id, Long customerId) {
        return addressRepository.findById(id)
                .filter(a -> a.getCustomer().getId().equals(customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
    }

    private void clearDefault(Long customerId) {
        addressRepository.findByCustomerId(customerId)
                .forEach(a -> {
                    if (a.isDefault()) {
                        a.setDefault(false);
                        addressRepository.save(a);
                    }
                });
    }

    private void apply(Address address, AddressRequest req) {
        address.setLabel(req.label());
        address.setStreet(req.street());
        address.setWard(req.ward());
        address.setDistrict(req.district());
        address.setCity(req.city());
        address.setReceiverName(req.receiverName());
        address.setReceiverPhone(req.receiverPhone());
    }
}
