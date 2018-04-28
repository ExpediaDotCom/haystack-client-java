/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.haystack.sleuth.core.reporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.expedia.haystack.sleuth.core.haystack.TraceKeys;
import com.expedia.haystack.sleuth.core.reporter.base.AccumulatorClient;
import com.expedia.haystack.sleuth.core.reporter.base.HaystackDemoApplication;
import com.expedia.haystack.sleuth.core.reporter.base.Reservation;
import com.expedia.www.haystack.client.Span;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HaystackDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.application.name=haystack-app"})
public class HaystackHeadersTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private AccumulatorClient client;

    private HttpHeaders headers;
    private String traceId = "681ef06e-6086-4792-9dea-7415d3079785";
    private String parentId = "00000000-0000-0000-80e5-07fa6a56d6b6";

    @Before
    public void setup() {
        client.close();
        headers = new HttpHeaders();
        headers.set(TraceKeys.TRACE_ID, traceId);
        headers.set(TraceKeys.PARENT_ID, parentId);
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void simpleHttpCallToValidateSpansAreCreated() {
        ResponseEntity<Reservation[]> responseEntity =
            this.restTemplate.exchange("http://localhost:" + port() + "/reservations/1033?abc=def", HttpMethod.GET, new HttpEntity<>(null, headers),
                                       Reservation[].class);

        Reservation[] reservations = responseEntity.getBody();

        assertThat(reservations).isNotNull();
        assertThat(client.getSpans().size()).isGreaterThan(0);

        Span span = client.getSpans().stream().filter(s -> s.context().getParentId() != null).findFirst().get();

        assertThat(span.getOperatioName()).isNotBlank().isEqualTo("get /reservations/{langid}");
        assertThat(span.context()).isNotNull();
        assertThat(span.context().getTraceId().toString()).isEqualTo(traceId);
        assertThat(span.context().getParentId().toString()).isEqualTo(parentId);
        assertThat(span.context().getSpanId().toString()).isNotNull();
        assertThat(span.getStartTime()).isPositive();
        assertThat(span.getDuration()).isPositive();
        assertThat(span.getEndTime()).isPositive();
        assertThat(span.getTags().get("error")).isEqualTo("false");
        assertThat(span.getTags().get("errorMessage")).isNull();
    }

    @Test
    public void simpleHttpCallToValidateSpansForExceptionCases() {
        try {
            ResponseEntity<Reservation[]> responseEntity =
                this.restTemplate.exchange("http://localhost:" + port() + "/badReservations/1033?abc=def", HttpMethod.GET, new HttpEntity<>(null, headers),
                                           Reservation[].class);
            assertThat(responseEntity).isNotNull();
            fail("This call should have failed");
        } catch (RestClientException e) {
            // do nothing
        }

        assertThat(client.getSpans().size()).isGreaterThan(0);

        Span span = client.getSpans().get(0);

        assertThat(span.getOperatioName()).isNotBlank().isEqualTo("get /badreservations/{langid}");
        assertThat(span.context()).isNotNull();
        assertThat(span.context().getTraceId().toString()).isNotNull();
        assertThat(span.context().getParentId().toString()).isNotNull();
        assertThat(span.context().getSpanId().toString()).isNotNull();
        assertThat(span.getStartTime()).isPositive();
        assertThat(span.getDuration()).isPositive();
        assertThat(span.getEndTime()).isPositive();
        assertThat(span.getTags().get("error")).isEqualTo("true");
        assertThat(span.getTags().get("errorMessage")).isNotNull();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void simpleHttpCallToValidateSpansAreCreatedWithoutHeaders() {
        ResponseEntity<Reservation[]> responseEntity =
            this.restTemplate.getForEntity("http://localhost:" + port() + "/reservations/1033?abc=def", Reservation[].class);

        Reservation[] reservations = responseEntity.getBody();

        assertThat(reservations).isNotNull();
        assertThat(client.getSpans().size()).isGreaterThan(0);

        Span span = client.getSpans().stream().filter(s -> s.context().getParentId() != null).findFirst().get();

        assertThat(span.getOperatioName()).isNotBlank().isEqualTo("get /reservations/{langid}");
        assertThat(span.context()).isNotNull();
        assertThat(span.context().getTraceId().toString()).isNotNull();
        assertThat(span.context().getParentId().toString()).isNotNull();
        assertThat(span.context().getSpanId().toString()).isNotNull();
        assertThat(span.getStartTime()).isPositive();
        assertThat(span.getDuration()).isPositive();
        assertThat(span.getEndTime()).isPositive();
        assertThat(span.getTags().get("error")).isEqualTo("false");
        assertThat(span.getTags().get("errorMessage")).isNull();
    }

    @Test
    public void simpleHttpCallToValidateSpansForExceptionCasesWithoutHeaders() {
        try {
            ResponseEntity<Reservation[]> responseEntity =
                this.restTemplate.getForEntity("http://localhost:" + port() + "/badReservations/1033?abc=def", Reservation[].class);
            assertThat(responseEntity).isNotNull();
            fail("This call should have failed");
        } catch (RestClientException e) {
            // do nothing
        }

        assertThat(client.getSpans().size()).isGreaterThan(0);

        Span span = client.getSpans().get(0);

        assertThat(span.getOperatioName()).isNotBlank().isEqualTo("get /badreservations/{langid}");
        assertThat(span.context()).isNotNull();
        assertThat(span.context().getTraceId().toString()).isNotNull();
        assertThat(span.context().getParentId().toString()).isNotNull();
        assertThat(span.context().getSpanId().toString()).isNotNull();
        assertThat(span.getStartTime()).isPositive();
        assertThat(span.getDuration()).isPositive();
        assertThat(span.getEndTime()).isPositive();
        assertThat(span.getTags().get("error")).isEqualTo("true");
        assertThat(span.getTags().get("errorMessage")).isNotNull();
    }

    @SuppressWarnings("ConstantConditions")
    private int port() {
        return environment.getProperty("local.server.port", Integer.class);
    }

}
