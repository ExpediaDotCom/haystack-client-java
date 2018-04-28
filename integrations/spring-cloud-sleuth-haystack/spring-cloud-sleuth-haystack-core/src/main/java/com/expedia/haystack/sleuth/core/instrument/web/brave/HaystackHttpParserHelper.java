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

package com.expedia.haystack.sleuth.core.instrument.web.brave;

import java.net.URI;
import java.util.List;

import org.springframework.cloud.sleuth.util.SpanNameUtil;

import com.expedia.haystack.sleuth.core.instrument.web.adjuster.NameAdjuster;

import brave.SpanCustomizer;

public class HaystackHttpParserHelper  {

    private final List<NameAdjuster> nameAdjusters;

    public HaystackHttpParserHelper(List<NameAdjuster> nameAdjusters) {
        this.nameAdjusters = nameAdjusters;
    }

    public void error(int httpStatus, Throwable error, SpanCustomizer customizer) {
        String message = null;

        if (error != null) {
            message = error.getMessage();

            if (message == null) {
                message = error.getClass().getName();
            }
        } else if (httpStatus != 0) {
            if (httpStatus < 200 || httpStatus > 399) {
                message = String.valueOf(httpStatus);
            }
        }

        if (message != null) {
            customizer.tag("error", "true");
            customizer.tag("errorMessage", message);
        } else {
            customizer.tag("error", "false");
        }
    }

    public String getName(URI uri) {
        // The returned name should comply with RFC 882 - Section 3.1.2.
        // i.e Header values must composed of printable ASCII values.
        String name = SpanNameUtil.shorten(uriScheme(uri) + ":" + uri.getRawPath());

        for (NameAdjuster nameAdjuster : nameAdjusters) {
            name = nameAdjuster.adjustName(name);
        }

        return name;
    }

    private String uriScheme(URI uri) {
        return (uri.getScheme() == null) ? "http" : uri.getScheme();
    }
}
