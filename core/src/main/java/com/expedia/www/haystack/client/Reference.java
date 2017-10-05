package com.expedia.www.haystack.client;

public class Reference {
    private final String referenceType;
    private final SpanContext context;

    public Reference(String referenceType, SpanContext context) {
        this.referenceType = referenceType;
        this.context = context;
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
