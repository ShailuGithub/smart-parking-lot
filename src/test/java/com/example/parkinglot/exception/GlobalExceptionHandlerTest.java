package com.example.parkinglot.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void exhaustedRetriesAreReportedAs409NotAServerError() {
        ProblemDetail problem = new GlobalExceptionHandler()
                .handleContention(new ConcurrencyFailureException("lost the race"));

        assertThat(problem.getStatus()).isEqualTo(409);
    }
}
