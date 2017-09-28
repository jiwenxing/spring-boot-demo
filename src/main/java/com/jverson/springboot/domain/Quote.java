package com.jverson.springboot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * for RestTemplate test
 * @author jiwenxing
 * @date Sep 27, 2017 7:45:23 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true) // indicate that any properties not bound in this type should be ignored.
public class Quote {

	private String type;
    private Value value;

    public Quote() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
    
}
