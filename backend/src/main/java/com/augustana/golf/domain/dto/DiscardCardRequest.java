package com.augustana.golf.domain.dto;

/**
 * Request body for discarding a stock-drawn card and optionally flipping a grid
 * position.
 */
public class DiscardCardRequest {

    private Integer flipPosition;

    public Integer getFlipPosition() { return flipPosition; }
    public void setFlipPosition(Integer flipPosition) { this.flipPosition = flipPosition; }
}
