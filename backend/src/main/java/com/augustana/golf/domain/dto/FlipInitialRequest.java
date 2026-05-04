package com.augustana.golf.domain.dto;

/**
 * Request body for flipping one setup card in positions 1 through 6.
 */
public class FlipInitialRequest {

    private int position;

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
