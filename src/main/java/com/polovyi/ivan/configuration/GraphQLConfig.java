package com.polovyi.ivan.configuration;

import com.polovyi.ivan.exeption.handler.GraphQLExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RequiredArgsConstructor
class GraphQLConfig {

    private final GraphQLExceptionHandler graphQLExceptionHandler;

    @Bean
    public GraphQlSourceBuilderCustomizer sourceBuilderCustomizer() {

        return builder ->
                builder.exceptionResolvers(List.of(graphQLExceptionHandler));
    }

}