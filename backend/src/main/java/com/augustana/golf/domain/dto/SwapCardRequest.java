package com.augustana.golf.domain.dto;

/**
 * Request body for swapping the held card into a grid position.
 */
public class SwapCardRequest {

    private int position;

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
