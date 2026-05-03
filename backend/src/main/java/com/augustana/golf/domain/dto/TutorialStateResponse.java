package com.augustana.golf.domain.dto;

import com.augustana.golf.domain.model.TutorialStep;

/**
 * Wraps the normal {@link GameStateResponse} with tutorial-specific metadata so
 * the React client always gets game state and guidance in a single response.
 */
public class TutorialStateResponse {

    /** Full game state with the same shape as the regular game state endpoint. */
    public GameStateResponse gameState;

    /** Which step of the tutorial the human player is on right now. */
    public TutorialStep currentStep;

    /** Human-readable title for the current step. */
    public String stepTitle;

    /** Full guidance text for the current step. */
    public String stepDescription;

    /**
     * How many initial flips the human player has completed (0, 1, or 2).
     */
    public int humanFlipsCompleted;

    /**
     * True once the bot has also finished its two initial flips and the active
     * turn loop can begin.
     */
    public boolean allPlayersReady;

    /** True if the bot just executed an action. */
    public boolean botJustMoved;

    /** Convenience game id so the frontend does not have to dig into gameState. */
    public Long gameId;

    public static TutorialStateResponse of(
            GameStateResponse gameState,
            TutorialStep step,
            int humanFlipsCompleted,
            boolean allPlayersReady,
            boolean botJustMoved) {

        TutorialStateResponse r = new TutorialStateResponse();
        r.gameState = gameState;
        r.currentStep = step;
        r.stepTitle = step.title;
        r.stepDescription = step.description;
        r.humanFlipsCompleted = humanFlipsCompleted;
        r.allPlayersReady = allPlayersReady;
        r.botJustMoved = botJustMoved;
        r.gameId = gameState != null ? gameState.gameId : null;
        return r;
    }
}
