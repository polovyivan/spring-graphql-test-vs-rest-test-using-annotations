package com.polovyi.ivan.controller;

import com.polovyi.ivan.dto.request.CreateCustomerRequest;
import com.polovyi.ivan.dto.request.PartiallyUpdateCustomerRequest;
import com.polovyi.ivan.dto.request.UpdateCustomerRequest;
import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
@Validated
@RequiredArgsConstructor
public class CustomerGraphQLMutationController {

    private final CustomerService customerService;

    @MutationMapping
    public CustomerResponse createCustomer(@Argument @Valid CreateCustomerRequest createCustomerRequest) {
        return customerService.createCustomer(createCustomerRequest);
    }

    @MutationMapping
    public CustomerResponse updateCustomer(
            @Argument @NotNull String customerId,
            @Argument @Valid @NotNull UpdateCustomerRequest updateCustomerRequest) {
        return customerService.updateCustomer(customerId, updateCustomerRequest);
    }

    @MutationMapping
    public CustomerResponse partiallyUpdateCustomer(
            @Argument @NotNull String customerId,
            @Argument @Valid @NotNull PartiallyUpdateCustomerRequest partiallyUpdateCustomerRequest) {
        return customerService.partiallyUpdateCustomer(customerId, partiallyUpdateCustomerRequest);
    }

    @MutationMapping
    public String deleteCustomer(@Argument @NotNull String customerId) {
        customerService.deleteCustomer(customerId);
        return customerId;
    }

}
