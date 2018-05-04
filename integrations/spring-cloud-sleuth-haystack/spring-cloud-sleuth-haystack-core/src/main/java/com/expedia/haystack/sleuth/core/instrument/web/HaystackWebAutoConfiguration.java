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

package com.expedia.haystack.sleuth.core.instrument.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.instrument.web.SleuthWebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expedia.haystack.sleuth.core.haystack.B3Codex;
import com.expedia.haystack.sleuth.core.haystack.B3KeyConvention;
import com.expedia.haystack.sleuth.core.instrument.web.adjuster.ExceptionSpanAdjuster;
import com.expedia.haystack.sleuth.core.instrument.web.adjuster.NumericalNameAdjuster;
import com.expedia.haystack.sleuth.core.instrument.web.adjuster.NumericalSpanAdjuster;

@Configuration
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
@EnableConfigurationProperties(SleuthWebProperties.class)
public class HaystackWebAutoConfiguration {

    @Bean
    public HaystackPreTraceFilter haystackPreTraceFilter() {
        return new HaystackPreTraceFilter(new B3KeyConvention(), new B3Codex());
    }

    @Bean
    public NumericalNameAdjuster numericalNameAdjuster() {
        return new NumericalNameAdjuster();
    }

    @Bean
    public ExceptionSpanAdjuster exceptionSpanAdjuster() {
        return new ExceptionSpanAdjuster();
    }

    @Bean
    public NumericalSpanAdjuster numericalSpanAdjuster(NumericalNameAdjuster numericalNameAdjuster) {
        return new NumericalSpanAdjuster(numericalNameAdjuster);
    }
}
