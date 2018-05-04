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

import org.apache.commons.lang3.StringUtils;

import brave.SpanCustomizer;
import brave.grpc.GrpcServerParser;
import io.grpc.Metadata;
import io.grpc.ServerCall;

public class HaystackServerGrpcParser extends GrpcServerParser {

    @Override
    protected <ReqT, RespT> void onStart(ServerCall<ReqT, RespT> call, Metadata headers, SpanCustomizer span) {
        String spanName = spanName(call.getMethodDescriptor());

        // we will reduce the length of the spanName.
        // by default it equals to package/methodName
        span.name("grpc:" + StringUtils.substringAfterLast(spanName, "."));
    }
}
