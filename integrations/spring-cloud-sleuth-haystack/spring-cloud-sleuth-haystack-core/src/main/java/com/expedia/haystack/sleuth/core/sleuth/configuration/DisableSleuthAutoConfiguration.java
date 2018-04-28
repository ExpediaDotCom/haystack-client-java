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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

@Configuration
public class DisableSleuthAutoConfiguration {

    public static final String SPRING_SLEUTH_OPENTRACING_ENABLED = "spring.sleuth.opentracing.enabled";
    public static final String SPRING_SLEUTH_LOGGER_SLF4J_ENABLED = "spring.sleuth.logger.slf4j.enabled";

    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public MapPropertySource csHaystackPropertySource() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(SPRING_SLEUTH_OPENTRACING_ENABLED, "false");
        properties.put(SPRING_SLEUTH_LOGGER_SLF4J_ENABLED, "false");

        MapPropertySource csHaystackPropSource = new MapPropertySource("cs-haystack", properties);
        MutablePropertySources sources = environment.getPropertySources();
        sources.addFirst(csHaystackPropSource);

        return csHaystackPropSource;
    }
}
