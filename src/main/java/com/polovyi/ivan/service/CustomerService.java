package com.polovyi.ivan.service;

import com.polovyi.ivan.dto.request.CreateCustomerRequest;
import com.polovyi.ivan.dto.request.PartiallyUpdateCustomerRequest;
import com.polovyi.ivan.dto.request.UpdateCustomerRequest;
import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.entity.CustomerEntity;
import com.polovyi.ivan.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerResponse> getAllCustomers() {
        log.info("Getting all customers...");
        return customerRepository.findAll().stream().map(CustomerResponse::valueOf).collect(Collectors.toList());
    }

    public List<CustomerResponse> getCustomersWithFilters(String fullName, String phoneNumber,
            LocalDate createdAt) {
        log.info("Getting all customers with filters fullName {}, phoneNumber {}, createdAt {} ...", fullName,
                phoneNumber, createdAt);
        return customerRepository.findCustomersWithFilters(fullName, phoneNumber, createdAt)
                .stream()
                .map(CustomerResponse::valueOf)
                .collect(Collectors.toList());
    }

    public CustomerResponse createCustomer(CreateCustomerRequest createCustomerRequest) {
        log.info("Creating a customer... ");
        CustomerEntity customer = CustomerEntity.valueOf(createCustomerRequest);
        return CustomerResponse.valueOf(customerRepository.save(customer));
    }

    public CustomerResponse updateCustomer(String customerId, UpdateCustomerRequest updateCustomerRequest) {
        log.info("Updating a customer... ");
        Optional<CustomerEntity> customer = customerRepository.findById(customerId);
        customer.ifPresent(entity -> {
            entity.setFullName(updateCustomerRequest.getFullName());
            entity.setPhoneNumber(updateCustomerRequest.getPhoneNumber());
            entity.setAddress(updateCustomerRequest.getAddress());
            customerRepository.save(entity);
        });
        return customer
                .map(CustomerResponse::valueOf)
                .orElse(null);
    }

    public CustomerResponse partiallyUpdateCustomer(String customerId,
            PartiallyUpdateCustomerRequest partiallyUpdateCustomerRequest) {
        log.info("Partially updating a customer... ");
        Optional<CustomerEntity> customer = customerRepository.findById(customerId);
        if (partiallyUpdateCustomerRequest.getPhoneNumber() != null) {
            customer.ifPresent(entity -> {
                Optional.ofNullable(partiallyUpdateCustomerRequest.getPhoneNumber()).ifPresent(entity::setPhoneNumber);
                customerRepository.save(entity);
            });
        }
        return customer
                .map(CustomerResponse::valueOf)
                .orElse(null);
    }

    public void deleteCustomer(String customerId) {
        log.info("Deleting a customer... ");
        customerRepository.findById(customerId).ifPresent(customerRepository::delete);
    }
}
