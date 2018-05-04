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

package com.expedia.haystack.sleuth.core.sleuth.logger;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;

public class HaystackSlf4jAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;
    private MockEnvironment environment;

    @Before
    public void setUp() {
        this.environment = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        this.context.setEnvironment(environment);
    }

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void checkRegisteredBeanWithNoPropertySet() {
        this.context.register(HaystackSlf4jAutoConfiguration.class);
        this.context.refresh();

        assertThat(this.context.containsBean("slf4jSpanLogger")).isTrue();
        assertThat(this.context.containsBean("slf4jSpanLoggerBPP")).isTrue();
    }

    @Test
    public void checkRegisteredBeanWithPropertySetToFalse() {
        environment.setProperty("spring.sleuth.haystack.client.logger.slf4j.enabled", "false");
        this.context.register(HaystackSlf4jAutoConfiguration.class);
        this.context.refresh();

        assertThat(this.context.containsBean("slf4jSpanLogger")).isFalse();
        assertThat(this.context.containsBean("slf4jSpanLoggerBPP")).isFalse();
    }
}
