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
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.Status;

public class HaystackServerGrpcParserTest {

    @Test
    public void checkUpdatedSpanName() {
        HaystackServerGrpcParser haystackServerGrpcParser = new HaystackServerGrpcParser();

        String methodName = "my.package.sendBy";

        MethodDescriptor<Object, Object> methodDescriptor = MethodDescriptor.newBuilder()
            .setFullMethodName(methodName)
            .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
            .setRequestMarshaller(new MethodDescriptor.Marshaller<Object>() {
                @Override
                public InputStream stream(Object value) {
                    return null;
                }

                @Override
                public Object parse(InputStream stream) {
                    return null;
                }
            })
            .setResponseMarshaller(new MethodDescriptor.Marshaller<Object>() {
                @Override
                public InputStream stream(Object value) {
                    return null;
                }

                @Override
                public Object parse(InputStream stream) {
                    return null;
                }
            })
            .build();

        ServerCall<Object, Object> serverCall = new ServerCall<Object, Object>() {
            @Override
            public void request(int numMessages) {

            }

            @Override
            public void sendHeaders(Metadata headers) {

            }

            @Override
            public void sendMessage(Object message) {

            }

            @Override
            public void close(Status status, Metadata trailers) {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public MethodDescriptor<Object, Object> getMethodDescriptor() {
                return methodDescriptor;
            }
        };

        CustomSpanCustomizer customSpanCustomizer = new CustomSpanCustomizer();

        haystackServerGrpcParser.onStart(serverCall, null, customSpanCustomizer);

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
