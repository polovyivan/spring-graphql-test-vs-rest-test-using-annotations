package com.polovyi.ivan.controller;

import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Validated
@Controller
@RequiredArgsConstructor
public class CustomerGraphQLQueryController {

    private final CustomerService customerService;

    @QueryMapping(name = "allCustomersWithFilters")
    public List<CustomerResponse> getAllCustomersWithFilters(
            @NotNull(message = "Field fullName cannot be null") @Argument String fullName,
            @Argument String phoneNumber,
            @Argument LocalDate createdAt) {
        return customerService.getCustomersWithFilters(fullName, phoneNumber, createdAt);
    }

}
