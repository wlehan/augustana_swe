package com.augustana.golf.domain.dto;

/**
 * Request body for drawing from either STOCK or DISCARD.
 */
public class DrawCardRequest {

    private String source;

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
