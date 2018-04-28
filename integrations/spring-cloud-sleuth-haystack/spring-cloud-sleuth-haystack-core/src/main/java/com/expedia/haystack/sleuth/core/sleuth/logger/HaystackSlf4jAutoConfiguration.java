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

import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.log.Slf4jCurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.propagation.CurrentTraceContext;

@Configuration
@ConditionalOnClass(MDC.class)
@EnableConfigurationProperties(HaystackSlf4jProperties.class)
@ConditionalOnProperty(value = "spring.sleuth.haystack.client.logger.slf4j.enabled", matchIfMissing = true)
public class HaystackSlf4jAutoConfiguration {

    @Bean
    public CurrentTraceContext slf4jSpanLogger() {
        return HaystackSlf4jCurrentTraceContext.create();
    }

    @Bean
    @ConditionalOnBean(CurrentTraceContext.class)
    public BeanPostProcessor slf4jSpanLoggerBPP() {
        return new Slf4jBeanPostProcessor();
    }

    class Slf4jBeanPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof CurrentTraceContext && !(bean instanceof Slf4jCurrentTraceContext)) {
                return HaystackSlf4jCurrentTraceContext.create((CurrentTraceContext) bean);
            } else {
                return bean;
            }
        }
    }
}
