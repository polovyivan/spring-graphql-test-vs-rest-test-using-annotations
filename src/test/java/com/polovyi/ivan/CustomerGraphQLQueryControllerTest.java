package com.polovyi.ivan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.polovyi.ivan.dto.request.CreateCustomerRequest;
import com.polovyi.ivan.dto.request.PartiallyUpdateCustomerRequest;
import com.polovyi.ivan.dto.request.UpdateCustomerRequest;
import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CustomerGraphQLQueryControllerTest {

    private final static String CUSTOMER_ID = UUID.randomUUID().toString();
    private static ObjectMapper mapper;

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private CustomerService customerService;

    private CustomerResponse customerResponse;

    private CreateCustomerRequest createCustomerRequest;

    private UpdateCustomerRequest updateCustomerRequest;

    private PartiallyUpdateCustomerRequest partiallyUpdateCustomerRequest;

    private GraphQlTester.Response response;

    private String fullName;
    private String phoneNumber;
    private String createdAt;

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

        /*
        POST /v1/customers-with-filters
         */

    @Test
    public void shouldReturnListOfCustomersFromGetAllCustomersWithFiltersAPI() throws Exception {
        givenAllAPIMethodParameters();
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers();
        whenPostForAllCustomersGraphQLAPICalled();
        thenExpectCustomerServiceGetCustomersWithFiltersCalledOnce();
        thenExpectResponseWithCustomerListFromAllCustomersWithFilters();
    }

    @Test
    public void shouldNotReturnListOfCustomersFromGetAllCustomersWithFiltersAPIGivenInvalidDateFormat()
            throws Exception {
        givenAllAPIMethodParametersWithInvalidDateFormat();
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers();
        whenPostForAllCustomersGraphQLAPICalled();
        thenExpectNoCallToCustomerServiceGetCustomersWithFilters();
        thenExpectResponseWithFieldInvalidFormatErrorMessage();
    }

            /*
            POST /v1/customers
             */

    @Test
    public void shouldCreateCustomer() {
        givenCustomerResponse();
        givenValidCreateCustomerRequest();
        givenCustomerServiceCreateCustomerReturnsCustomerResponse();
        whenPostCreateCustomerGraphQLAPICalled();
        thenExpectCustomerServiceCreateCustomerCalledOnce();
        thenExpectCreateCustomerResponseWithCustomer();
    }

    @Test
    public void shouldNotCreateCustomerGivenRequestWithoutRequiredFields() {
        givenCreateCustomerRequestWithoutRequiredFields();
        whenPostCreateCustomerGraphQLAPICalled();
        thenExpectNoCallToCustomerServiceCreateCustomer();
        thenExpectResponseWithThreeBadRequestErrorCodes();
        thenExpectResponseWithRequiredFieldErrorMessages();
    }

            /*
            PUT /v1/customers
             */

    @Test
    public void shouldUpdateCustomer() {
        givenCustomerResponse();
        givenValidUpdateCustomerRequest();
        givenCustomerServiceUpdateCustomerReturnsCustomerResponse();
        whenUpdateCustomersAPICalled();
        thenExpectCustomerServiceUpdateCustomerCalledOnce();
        thenExpectUpdateCustomerResponseWithCustomer();
    }

    @Test
    public void shouldNotUpdateCustomerGivenRequestWithoutRequiredFields() {
        givenUpdateCustomerRequestWithoutRequiredFields();
        whenUpdateCustomersAPICalled();
        thenExpectNoCallToCustomerServiceUpdateCustomer();
        thenExpectResponseWithThreeBadRequestErrorCodes();
        thenExpectResponseWithRequiredFieldErrorMessages();
    }

            /*
            PATCH /v1/customers
             */

    @Test
    public void shouldPartiallyUpdateCustomer() {
        givenCustomerResponse();
        givenValidPartiallyUpdateCustomerRequest();
        givenCustomerServicePartiallyUpdateCustomerReturnsCustomerResponse();
        whenPartiallyUpdateCustomersAPICalled();
        thenExpectCustomerServicePartiallyUpdateCustomerCalledOnce();
        thenExpectPartiallyUpdateCustomerResponseWithCustomer();
    }

            /*
            DELETE /v1/customers
             */

    @Test
    public void shouldDeleteCustomer() {
        givenCustomerServiceDeleteCustomerReturnsNothing();
        whenDeleteCustomersAPICalled();
        thenExpectCustomerServiceDeleteCustomerCalledOnce();
        thenExpectDeleteCustomerResponseWithCustomerId();
    }

    /*
     * GIVEN Methods
     */

    private void givenCustomerResponse() {
        customerResponse = CustomerResponse.builder()
                .id(CUSTOMER_ID)
                .fullName("Ivan Polovyi")
                .address("Address")
                .phoneNumber("1-669-210-0504")
                .createdAt(LocalDate.now())
                .build();
    }

    private void givenAllAPIMethodParameters() {
        fullName = "Ivan Polovyi";
        phoneNumber = "626.164.7481";
        createdAt = "2015-09-01";
    }

    private void givenAllAPIMethodParametersWithInvalidDateFormat() {
        givenAllAPIMethodParameters();
        createdAt = "09-01-2015";
    }

    private void givenValidCreateCustomerRequest() {
        createCustomerRequest = CreateCustomerRequest.builder()
                .fullName("Ivan Polovyi")
                .phoneNumber("6261647481")
                .address("Apt. 843 399 Lachelle Crossing, New Eldenhaven, LA 63962-9260")
                .build();
    }

    private void givenValidUpdateCustomerRequest() {
        updateCustomerRequest = UpdateCustomerRequest.builder()
                .fullName("Ivan Polovyi")
                .phoneNumber("626.164.7481")
                .address("Apt. 843 399 Lachelle Crossing, New Eldenhaven, LA 63962-9260")
                .build();
    }

    private void givenValidPartiallyUpdateCustomerRequest() {
        partiallyUpdateCustomerRequest = PartiallyUpdateCustomerRequest.builder()
                .phoneNumber("626.164.7481")
                .build();
    }

    private void givenCustomerServiceCreateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).createCustomer(any(CreateCustomerRequest.class));
    }

    private void givenCustomerServiceUpdateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).updateCustomer(anyString(), any(UpdateCustomerRequest.class));
    }

    private void givenCustomerServicePartiallyUpdateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService)
                .partiallyUpdateCustomer(CUSTOMER_ID, partiallyUpdateCustomerRequest);
    }

    private void givenCustomerServiceDeleteCustomerReturnsNothing() {
        doNothing().when(customerService).deleteCustomer(CUSTOMER_ID);
    }

    private void givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers() {
        doReturn(List.of(customerResponse)).when(customerService)
                .getCustomersWithFilters(any(), any(), any());
    }

    private void givenCreateCustomerRequestWithoutRequiredFields() {
        givenValidCreateCustomerRequest();
        createCustomerRequest.setFullName(null);
        createCustomerRequest.setPhoneNumber(null);
        createCustomerRequest.setAddress(null);
    }

    private void givenUpdateCustomerRequestWithoutRequiredFields() {
        givenValidUpdateCustomerRequest();
        updateCustomerRequest.setFullName(null);
        updateCustomerRequest.setPhoneNumber(null);
        updateCustomerRequest.setAddress(null);
    }

    /*
     * WHEN Methods
     */

    private void whenPostForAllCustomersGraphQLAPICalled() {
        response = this.graphQlTester
                .documentName("allCustomersWithFilters")
                .variable("fullName", fullName)
                .variable("phoneNumber", phoneNumber)
                .variable("createdAt", createdAt)
                .execute();
    }

    private void whenPostCreateCustomerGraphQLAPICalled() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("fullName", createCustomerRequest.getFullName());
        varMap.put("phoneNumber", createCustomerRequest.getPhoneNumber());
        varMap.put("address", createCustomerRequest.getPhoneNumber());
        response = this.graphQlTester
                .documentName("createCustomer")
                .variable("createCustomerRequest", varMap)
                .execute();
    }

    private void whenUpdateCustomersAPICalled() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("fullName", updateCustomerRequest.getFullName());
        varMap.put("phoneNumber", updateCustomerRequest.getPhoneNumber());
        varMap.put("address", updateCustomerRequest.getPhoneNumber());
        response = this.graphQlTester
                .documentName("updateCustomer")
                .variable("customerId", CUSTOMER_ID)
                .variable("updateCustomerRequest", varMap)
                .execute();
    }

    private void whenPartiallyUpdateCustomersAPICalled() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("phoneNumber", partiallyUpdateCustomerRequest.getPhoneNumber());
        response = this.graphQlTester
                .documentName("partiallyUpdateCustomer")
                .variable("customerId", CUSTOMER_ID)
                .variable("partiallyUpdateCustomerRequest", varMap)
                .execute();
    }

    private void whenDeleteCustomersAPICalled() {
        response = this.graphQlTester
                .documentName("deleteCustomer")
                .variable("customerId", CUSTOMER_ID)
                .execute();
    }


    /*
     * THEN Methods
     */

    private void thenExpectResponseWithCustomerListFromAllCustomersWithFilters() {
        List<CustomerResponse> getAllCustomers = response.path("allCustomersWithFilters")
                .entityList(CustomerResponse.class).get();
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains(customerResponse));
    }

    private void thenExpectResponseWithFieldInvalidFormatErrorMessage() {
        response.errors().satisfy(responseErrors -> {
            assertTrue(responseErrors.size() == 1);
            assertEquals(
                    "Variable 'createdAt' has an invalid value: Invalid RFC3339 full date value : '09-01-2015'. because of : 'Text '09-01-2015' could not be parsed at index 0'",
                    responseErrors.get(0).getMessage());
        });
    }

    private void thenExpectCreateCustomerResponseWithCustomer() {
        CustomerResponse createdCustomer = response.path("createCustomer").entity(CustomerResponse.class).get();
        assertEquals(customerResponse, createdCustomer);
    }

    private void thenExpectResponseWithRequiredFieldErrorMessages() {
        response.errors().satisfy(errors -> {
            assertTrue(errors.size() == 3);
            List<String> errorMessages = getErrorMessages(errors);
            assertTrue(errorMessages.contains("Field fullName cannot be null"));
            assertTrue(errorMessages.contains("Field phoneNumber cannot be null"));
            assertTrue(errorMessages.contains("Field address cannot be null"));
        });
    }

    private void thenExpectResponseWithThreeBadRequestErrorCodes() {
        response.errors().satisfy(responseErrors -> assertTrue(responseErrors.stream()
                .allMatch(error -> error.getExtensions().get("errorCode").equals(HttpStatus.BAD_REQUEST.value()))));
    }

    private void thenExpectUpdateCustomerResponseWithCustomer() {
        CustomerResponse customer = response.path("updateCustomer").entity(
                CustomerResponse.class).get();
        assertTrue(customer.equals(customerResponse));
    }

    private void thenExpectPartiallyUpdateCustomerResponseWithCustomer() {
        CustomerResponse customer = response.path("partiallyUpdateCustomer").entity(
                CustomerResponse.class).get();
        assertTrue(customer.equals(customerResponse));
    }

    private void thenExpectDeleteCustomerResponseWithCustomerId() {
        String customerId = response.path("deleteCustomer").entity(
                String.class).get();
        assertTrue(customerId.equals(CUSTOMER_ID));
    }

    private void thenExpectCustomerServiceGetCustomersWithFiltersCalledOnce() {
        verify(customerService).getCustomersWithFilters(any(), any(), any());
    }

    private void thenExpectNoCallToCustomerServiceGetCustomersWithFilters() {
        verify(customerService, times(0)).getCustomersWithFilters(anyString(), anyString(), any());
    }

    private void thenExpectCustomerServiceCreateCustomerCalledOnce() {
        verify(customerService).createCustomer(any(CreateCustomerRequest.class));
    }

    private void thenExpectNoCallToCustomerServiceCreateCustomer() {
        verify(customerService, times(0)).createCustomer(any(CreateCustomerRequest.class));
    }

    private void thenExpectCustomerServiceUpdateCustomerCalledOnce() {
        verify(customerService).updateCustomer(anyString(), any(UpdateCustomerRequest.class));
    }

    private void thenExpectNoCallToCustomerServiceUpdateCustomer() {
        verify(customerService, times(0)).updateCustomer(anyString(), any(UpdateCustomerRequest.class));
    }

    private void thenExpectCustomerServicePartiallyUpdateCustomerCalledOnce() {
        verify(customerService).partiallyUpdateCustomer(anyString(), any(PartiallyUpdateCustomerRequest.class));
    }

    private void thenExpectCustomerServiceDeleteCustomerCalledOnce() {
        verify(customerService).deleteCustomer(anyString());
    }

    private List<String> getErrorMessages(List<ResponseError> errors) {
        return errors.stream()
                .map(ResponseError::getMessage)
                .collect(Collectors.toList());
    }

}
