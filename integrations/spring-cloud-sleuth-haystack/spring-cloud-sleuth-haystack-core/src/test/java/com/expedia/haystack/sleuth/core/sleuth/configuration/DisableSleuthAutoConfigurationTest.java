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

package com.expedia.haystack.sleuth.core.sleuth.configuration;

import static com.expedia.haystack.sleuth.core.sleuth.configuration.DisableSleuthAutoConfiguration.SPRING_SLEUTH_LOGGER_SLF4J_ENABLED;
import static com.expedia.haystack.sleuth.core.sleuth.configuration.DisableSleuthAutoConfiguration.SPRING_SLEUTH_OPENTRACING_ENABLED;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DisableSleuthAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        this.context = new AnnotationConfigApplicationContext();
    }

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void checkDisablePropertiesAutomatically() {
        this.context.register(DisableSleuthAutoConfiguration.class);
        this.context.refresh();
        assertThat(context.getEnvironment().getProperty(SPRING_SLEUTH_OPENTRACING_ENABLED, Boolean.class)).isFalse();
        assertThat(context.getEnvironment().getProperty(SPRING_SLEUTH_LOGGER_SLF4J_ENABLED, Boolean.class)).isFalse();
    }
}
