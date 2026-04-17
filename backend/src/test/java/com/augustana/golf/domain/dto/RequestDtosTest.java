package com.augustana.golf.domain.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestDtosTest {

    // CreateGameRequest Tests
    @Test
    void createGameRequest_defaultMaxPlayers_is4() {
        CreateGameRequest request = new CreateGameRequest();
        assertEquals(4, request.getMaxPlayers());
    }

    @Test
    void createGameRequest_setMaxPlayers_works() {
        CreateGameRequest request = new CreateGameRequest();
        request.setMaxPlayers(2);
        assertEquals(2, request.getMaxPlayers());
    }

    @Test
    void createGameRequest_setMaxPlayers_toOne() {
        CreateGameRequest request = new CreateGameRequest();
        request.setMaxPlayers(1);
        assertEquals(1, request.getMaxPlayers());
    }

    @Test
    void createGameRequest_setMaxPlayers_toThree() {
        CreateGameRequest request = new CreateGameRequest();
        request.setMaxPlayers(3);
        assertEquals(3, request.getMaxPlayers());
    }

    // JoinGameRequest Tests
    @Test
    void joinGameRequest_defaultGameCode_isNull() {
        JoinGameRequest request = new JoinGameRequest();
        assertNull(request.getGameCode());
    }

    @Test
    void joinGameRequest_setGameCode_works() {
        JoinGameRequest request = new JoinGameRequest();
        request.setGameCode("ABC123");
        assertEquals("ABC123", request.getGameCode());
    }

    @Test
    void joinGameRequest_setGameCode_withDifferentValue() {
        JoinGameRequest request = new JoinGameRequest();
        request.setGameCode("XYZ789");
        assertEquals("XYZ789", request.getGameCode());
    }

    @Test
    void joinGameRequest_setGameCode_toNull() {
        JoinGameRequest request = new JoinGameRequest();
        request.setGameCode("ABC123");
        request.setGameCode(null);
        assertNull(request.getGameCode());
    }

    // DiscardCardRequest Tests
    @Test
    void discardCardRequest_defaultFlipPosition_isNull() {
        DiscardCardRequest request = new DiscardCardRequest();
        assertNull(request.getFlipPosition());
    }

    @Test
    void discardCardRequest_setFlipPosition_works() {
        DiscardCardRequest request = new DiscardCardRequest();
        request.setFlipPosition(1);
        assertEquals(Integer.valueOf(1), request.getFlipPosition());
    }

    @Test
    void discardCardRequest_setFlipPosition_withDifferentValue() {
        DiscardCardRequest request = new DiscardCardRequest();
        request.setFlipPosition(5);
        assertEquals(Integer.valueOf(5), request.getFlipPosition());
    }

    @Test
    void discardCardRequest_setFlipPosition_toNull() {
        DiscardCardRequest request = new DiscardCardRequest();
        request.setFlipPosition(3);
        request.setFlipPosition(null);
        assertNull(request.getFlipPosition());
    }

    @Test
    void discardCardRequest_setFlipPosition_toZero() {
        DiscardCardRequest request = new DiscardCardRequest();
        request.setFlipPosition(0);
        assertEquals(Integer.valueOf(0), request.getFlipPosition());
    }

    // DrawCardRequest Tests
    @Test
    void drawCardRequest_defaultSource_isNull() {
        DrawCardRequest request = new DrawCardRequest();
        assertNull(request.getSource());
    }

    @Test
    void drawCardRequest_setSource_toStock() {
        DrawCardRequest request = new DrawCardRequest();
        request.setSource("STOCK");
        assertEquals("STOCK", request.getSource());
    }

    @Test
    void drawCardRequest_setSource_toDiscard() {
        DrawCardRequest request = new DrawCardRequest();
        request.setSource("DISCARD");
        assertEquals("DISCARD", request.getSource());
    }

    @Test
    void drawCardRequest_setSource_toNull() {
        DrawCardRequest request = new DrawCardRequest();
        request.setSource("STOCK");
        request.setSource(null);
        assertNull(request.getSource());
    }

    @Test
    void drawCardRequest_setSource_withCustomValue() {
        DrawCardRequest request = new DrawCardRequest();
        request.setSource("CUSTOM_SOURCE");
        assertEquals("CUSTOM_SOURCE", request.getSource());
    }

    // FlipInitialRequest Tests
    @Test
    void flipInitialRequest_defaultPosition_isZero() {
        FlipInitialRequest request = new FlipInitialRequest();
        assertEquals(0, request.getPosition());
    }

    @Test
    void flipInitialRequest_setPosition_works() {
        FlipInitialRequest request = new FlipInitialRequest();
        request.setPosition(2);
        assertEquals(2, request.getPosition());
    }

    @Test
    void flipInitialRequest_setPosition_toOne() {
        FlipInitialRequest request = new FlipInitialRequest();
        request.setPosition(1);
        assertEquals(1, request.getPosition());
    }

    @Test
    void flipInitialRequest_setPosition_toSix() {
        FlipInitialRequest request = new FlipInitialRequest();
        request.setPosition(6);
        assertEquals(6, request.getPosition());
    }

    @Test
    void flipInitialRequest_setPosition_toLargeNumber() {
        FlipInitialRequest request = new FlipInitialRequest();
        request.setPosition(100);
        assertEquals(100, request.getPosition());
    }

    // SwapCardRequest Tests
    @Test
    void swapCardRequest_defaultPosition_isZero() {
        SwapCardRequest request = new SwapCardRequest();
        assertEquals(0, request.getPosition());
    }

    @Test
    void swapCardRequest_setPosition_works() {
        SwapCardRequest request = new SwapCardRequest();
        request.setPosition(3);
        assertEquals(3, request.getPosition());
    }

    @Test
    void swapCardRequest_setPosition_toOne() {
        SwapCardRequest request = new SwapCardRequest();
        request.setPosition(1);
        assertEquals(1, request.getPosition());
    }

    @Test
    void swapCardRequest_setPosition_toSix() {
        SwapCardRequest request = new SwapCardRequest();
        request.setPosition(6);
        assertEquals(6, request.getPosition());
    }

    @Test
    void swapCardRequest_setPosition_multipleTimesShouldUpdate() {
        SwapCardRequest request = new SwapCardRequest();
        request.setPosition(2);
        assertEquals(2, request.getPosition());
        request.setPosition(5);
        assertEquals(5, request.getPosition());
    }
}
