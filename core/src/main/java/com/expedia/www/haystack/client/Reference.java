package com.expedia.www.haystack.client;

import java.util.Objects;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Reference {
    private final String referenceType;
    private final SpanContext context;

    public Reference(String referenceType, SpanContext context) {
        this.referenceType = referenceType;
        this.context = context;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reference reference = (Reference) obj;
        return Objects.equals(referenceType, reference.getReferenceType())
            && Objects.equals(context, reference.getContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceType, context);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
            .toString();
    }

    /**
     * @return the referenceType
     */
    public String getReferenceType() {
        return referenceType;
    }

    /**
     * @return the context
     */
    public SpanContext getContext() {
        return context;
    }
}
