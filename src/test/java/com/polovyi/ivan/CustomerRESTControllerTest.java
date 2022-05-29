package com.polovyi.ivan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.polovyi.ivan.dto.request.CreateCustomerRequest;
import com.polovyi.ivan.dto.request.PartiallyUpdateCustomerRequest;
import com.polovyi.ivan.dto.request.UpdateCustomerRequest;
import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CustomerRESTControllerTest {

    private final static String CUSTOMERS_API_PATH = "/v1/customers";
    private final static String CUSTOMERS_API_PATH_WITH_VARIABLE = "/v1/customers/{id}";
    private final static String GET_ALL_CUSTOMERS_WITH_FILTERS_API_PATH = "/v1/customers-with-filters";

    private static ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private MockHttpServletResponse response;

    private CustomerResponse customerResponse;

    private MultiValueMap<String, String> queryParams;

    private CreateCustomerRequest createCustomerRequest;

    private UpdateCustomerRequest updateCustomerRequest;

    private PartiallyUpdateCustomerRequest partiallyUpdateCustomerRequest;

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }


    /*
    GET /v1/customers
     */

    @Test
    public void shouldReturnListOfCustomersFromGetAllCustomersRestAPI() throws Exception {
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersReturnsListOfCustomers();
        whenGetAllCustomersAPICalled();
        thenExpectResponseHasOkStatus();
        thenExpectCustomerServiceGetAllCustomersCalledOnce();
        thenExpectResponseWithCustomerList();
    }

    /*
    GET /v1/customers-with-filters
     */

    @Test
    public void shouldReturnListOfCustomersFromGetAllCustomersWithFiltersRestAPI() throws Exception {
        givenAllQueryParams();
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers();
        whenGetAllCustomersWithFiltersAPICalled();
        thenExpectCustomerServiceGetCustomersWithFiltersCalledOnce();
        thenExpectResponseHasOkStatus();
        thenExpectResponseWithCustomerList();
    }

    @Test
    public void shouldNotReturnListOfCustomersFromGetAllCustomersWithFiltersRestAPIGivenInvalidDateFormat()
            throws Exception {
        givenQueryParamsWithInvalidDateFormat();
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers();
        whenGetAllCustomersWithFiltersAPICalled();
        thenExpectNoCallToCustomerServiceGetCustomersWithFilters();
        thenExpectResponseHasBadRequestStatus();
    }

    /*
    POST /v1/customers
     */

    @Test
    public void shouldCreateCustomerCallingRestAPI() throws Exception {
        givenCustomerResponse();
        givenValidCreateCustomerRequest();
        givenCustomerServiceCreateCustomerReturnsCustomerResponse();
        whenCreateCustomersAPICalled();
        thenExpectCustomerServiceCreateCustomerCalledOnce();
        thenExpectResponseHasCreatedStatus();
    }

    @Test
    public void shouldNotCreateCustomerGivenRequestWithoutRequiredFieldsCallingRestAPI() throws Exception {
        givenRequestWithoutRequiredFields();
        givenCustomerServiceCreateCustomerReturnsCustomerResponse();
        whenCreateCustomersAPICalled();
        thenExpectNoCallToCustomerServiceCreateCustomer();
        thenExpectResponseHasBadRequestStatus();
    }

    /*
    PUT /v1/customers
     */

    @Test
    public void shouldUpdateCustomerCallingRestAPI() throws Exception {
        givenCustomerResponse();
        givenValidUpdateCustomerRequest();
        givenCustomerServiceUpdateCustomerReturnsCustomerResponse();
        whenUpdateCustomersAPICalled();
        thenExpectCustomerServiceUpdateCustomerCalledOnce();
        thenExpectResponseHasNoContentStatus();
    }

    @Test
    public void shouldNotUpdateCustomerGivenRequestWithoutRequiredFieldsCallingRestAPI() throws Exception {
        givenCustomerResponse();
        givenInvalidUpdateCustomerRequest();
        givenCustomerServiceUpdateCustomerReturnsCustomerResponse();
        whenUpdateCustomersAPICalled();
        thenExpectNoCallToCustomerServiceUpdateCustomer();
        thenExpectResponseHasBadRequestStatus();
    }

    /*
    PATCH /v1/customers
     */

    @Test
    public void shouldPartiallyUpdateCustomerCallingRestAPI() throws Exception {
        givenCustomerResponse();
        givenValidPartiallyUpdateCustomerRequest();
        givenCustomerServicePartiallyUpdateCustomerReturnsCustomerResponse();
        whenPartiallyUpdateCustomersAPICalled();
        thenExpectCustomerServicePartiallyUpdateCustomerCalledOnce();
        thenExpectResponseHasNoContentStatus();
    }

    /*
    DELETE /v1/customers
     */

    @Test
    public void shouldDeleteCustomerCallingRestAPI() throws Exception {
        whenDeleteCustomersAPICalled();
        givenCustomerServiceDeleteCustomerReturnsNothing();
        thenExpectCustomerServiceDeleteCustomerCalledOnce();
        thenExpectResponseHasNoContentStatus();
    }

    /*
     * GIVEN Methods
     */

    private void givenCustomerResponse() {
        customerResponse = CustomerResponse.builder()
                .id("1")
                .fullName("Ivan Polovyi")
                .address("Address")
                .phoneNumber("1-669-210-0504")
                .createdAt(LocalDate.now())
                .build();
    }

    private void givenAllQueryParams() {
        queryParams = new LinkedMultiValueMap<>();
        queryParams.add("fullName", "Ivan Polovyi");
        queryParams.add("phoneNumber", "626.164.7481");
        queryParams.add("createdAt", "2015-09-01");

    }

    private void givenQueryParamsWithInvalidDateFormat() {
        givenAllQueryParams();
        queryParams.set("createdAt", "09-01-2015");
    }

    private void givenValidCreateCustomerRequest() {
        createCustomerRequest = CreateCustomerRequest.builder()
                .fullName("Ivan Polovyi")
                .phoneNumber("626.164.7481")
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

    private void givenInvalidUpdateCustomerRequest() {
        givenValidUpdateCustomerRequest();
        updateCustomerRequest.setAddress(null);
        updateCustomerRequest.setFullName(null);
        updateCustomerRequest.setPhoneNumber(null);
    }

    private void givenValidPartiallyUpdateCustomerRequest() {
        partiallyUpdateCustomerRequest = PartiallyUpdateCustomerRequest.builder()
                .phoneNumber("626.164.7481")
                .build();
    }

    private void givenCustomerServiceGetAllCustomersReturnsListOfCustomers() {
        doReturn(List.of(customerResponse)).when(customerService).getAllCustomers();
    }

    private void givenCustomerServiceCreateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).createCustomer(createCustomerRequest);
    }

    private void givenCustomerServiceUpdateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).updateCustomer("1", updateCustomerRequest);
    }

    private void givenCustomerServicePartiallyUpdateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).partiallyUpdateCustomer("1", partiallyUpdateCustomerRequest);
    }

    private void givenCustomerServiceDeleteCustomerReturnsNothing() {
        doNothing().when(customerService).deleteCustomer("1");
    }

    private void givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers() {
        doReturn(List.of(customerResponse)).when(customerService)
                .getCustomersWithFilters(any(), any(), any());
    }

    private void givenRequestWithoutRequiredFields() {
        givenValidCreateCustomerRequest();
        createCustomerRequest.setFullName(null);
        createCustomerRequest.setPhoneNumber(null);
        createCustomerRequest.setAddress(null);
    }


    /*
     * WHEN Methods
     */

    private void whenGetAllCustomersAPICalled() throws Exception {
        response = mockMvc.perform(get(CUSTOMERS_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    private void whenGetAllCustomersWithFiltersAPICalled() throws Exception {
        response = mockMvc.perform(get(GET_ALL_CUSTOMERS_WITH_FILTERS_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParams(queryParams))
                .andReturn()
                .getResponse();
    }

    private void whenCreateCustomersAPICalled() throws Exception {
        response = mockMvc.perform(post(CUSTOMERS_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJsonString(createCustomerRequest)))
                .andReturn()
                .getResponse();
    }

    private void whenUpdateCustomersAPICalled() throws Exception {
        response = mockMvc.perform(put(CUSTOMERS_API_PATH_WITH_VARIABLE, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJsonString(updateCustomerRequest)))
                .andReturn()
                .getResponse();
    }

    private void whenPartiallyUpdateCustomersAPICalled() throws Exception {
        response = mockMvc.perform(patch(CUSTOMERS_API_PATH_WITH_VARIABLE, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJsonString(partiallyUpdateCustomerRequest)))
                .andReturn()
                .getResponse();
    }

    private void whenDeleteCustomersAPICalled() throws Exception {
        response = mockMvc.perform(delete(CUSTOMERS_API_PATH_WITH_VARIABLE, "1"))
                .andReturn()
                .getResponse();
    }

    /*
     * THEN Methods
     */

    private void thenExpectResponseHasOkStatus() {
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    private void thenExpectResponseHasBadRequestStatus() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    private void thenExpectResponseHasCreatedStatus() {
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
    }

    private void thenExpectResponseHasNoContentStatus() {
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }

    private void thenExpectResponseWithCustomerList() throws UnsupportedEncodingException {
        List<CustomerResponse> getAllCustomers = stringJsonToList(response.getContentAsString(),
                CustomerResponse.class);
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains(customerResponse));
    }

    private void thenExpectCustomerServiceGetAllCustomersCalledOnce() {
        verify(customerService).getAllCustomers();
    }

    private void thenExpectCustomerServiceGetCustomersWithFiltersCalledOnce() {
        verify(customerService).getCustomersWithFilters(anyString(), anyString(), any());
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

    @SneakyThrows
    protected <T> List<T> stringJsonToList(String json, Class<T> clazz) {
        return mapper.readValue(json, new TypeReference<>() {
            @Override
            public Type getType() {
                return mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            }
        });
    }

    @SneakyThrows
    protected String objectToJsonString(Object object) {
        return mapper.writeValueAsString(object);
    }

}
