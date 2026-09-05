package com.weeklyreport.backend.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import tools.jackson.databind.ObjectMapper;

// Shared integration-test base: MockMvc + a real (H2) Spring context, one outer transaction per
// test method that's always rolled back afterward, so tests never leak state into each other.
//
// CAUTION - shared persistence context across requests: because the whole test method runs in one
// Spring-test-managed transaction, every MockMvc request in that method reuses the SAME Hibernate
// persistence context/identity map (not a fresh one per "request" like in production). An entity
// fetched (and a lazy collection initialized) by an earlier request stays cached and won't reflect
// writes made by a later request against the same row/collection. Call flushAndClear() between
// mutating MockMvc calls in a multi-step test to force the next read to hit the DB fresh.
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestDataFactory testDataFactory;

    @Autowired
    protected TestJwtFactory testJwtFactory;

    @Autowired
    protected EntityManager entityManager;

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
