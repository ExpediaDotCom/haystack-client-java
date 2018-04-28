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

package com.expedia.haystack.sleuth.core.instrument.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.Test;

import brave.SpanCustomizer;
import io.grpc.MethodDescriptor;

public class HaystackClientGrpcParserTest {

    @Test
    public void checkUpdatedSpanName() {
        HaystackClientGrpcParser haystackClientGrpcParser = new HaystackClientGrpcParser();

        String methodName = "my.package.sendBy";

        MethodDescriptor<String, String> method = MethodDescriptor.<String, String>newBuilder()
            .setFullMethodName(methodName)
            .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
            .setRequestMarshaller(new MethodDescriptor.Marshaller<String>() {
                @Override
                public InputStream stream(String value) {
                    return null;
                }

                @Override
                public String parse(InputStream stream) {
                    return null;
                }
            })
            .setResponseMarshaller(new MethodDescriptor.Marshaller<String>() {
                @Override
                public InputStream stream(String value) {
                    return null;
                }

                @Override
                public String parse(InputStream stream) {
                    return null;
                }
            })
            .build();

        CustomSpanCustomizer customSpanCustomizer = new CustomSpanCustomizer();

        haystackClientGrpcParser.onStart(method, null, null, customSpanCustomizer);

        assertThat(customSpanCustomizer.getTestedName()).isEqualTo("grpc:sendBy");
    }

    class CustomSpanCustomizer implements SpanCustomizer {
        private String testedName;

        String getTestedName() {
            return testedName;
        }

        @Override
        public SpanCustomizer name(String name) {
            this.testedName = name;
            return this;
        }

        @Override
        public SpanCustomizer tag(String key, String value) {
            return null;
        }

        @Override
        public SpanCustomizer annotate(String value) {
            return null;
        }

        @Override
        public SpanCustomizer annotate(long timestamp, String value) {
            return null;
        }
    }
}
