package com.augustana.golf.domain.model;

/**
 * Ordered tutorial prompts shown by the React hint panel.
 *
 * <p>The backend derives the current value from round state, then sends the
 * matching title and description to the frontend.</p>
 */
public enum TutorialStep {

    WELCOME(
        "Welcome to Golf!",
        "Golf is a card game where LOW scores win. Each player has a 2x3 grid of 6 cards "
        + "face-down in front of them. Your goal is to end each round with the lowest "
        + "total value in your grid. Ready? Let's flip two cards to get started."
    ),

    FLIP_FIRST(
        "Flip your first card",
        "Click any card in your grid to flip it face-up. You may look at its value - "
        + "your opponents cannot see face-down cards in your grid, but they can see yours "
        + "once flipped."
    ),

    FLIP_SECOND(
        "Flip your second card",
        "Great! Now flip one more card. You must flip exactly 2 cards before the round "
        + "begins. You want to know where your high cards are so you can "
        + "swap them out later."
    ),

    WAIT_FOR_OTHERS_TO_FLIP(
        "Waiting for other players...",
        "Every player must flip 2 cards before the active round begins. "
        + "The bot is choosing its cards now."
    ),

    YOUR_TURN_DRAW(
        "Your turn - draw a card",
        "On your turn you must draw one card. You have two choices:\n"
        + "- Draw from the DECK (face-down, unknown card)\n"
        + "- Take the TOP card of the DISCARD pile (visible)\n\n"
        + "The discard pile top card is always shown. Pick one!"
    ),

    YOUR_TURN_DECIDE(
        "Choose a card to replace",
        "You're now holding a drawn card.\n\n"
        + "If you drew from the DISCARD pile, you must swap it with one card in your grid.\n\n"
        + "If you drew from the DECK, you may either swap it with a grid card OR discard it and flip one face-down card.\n\n"
        + "Click a card in your grid to replace it with the card you're holding.\n"
        + "Tip: swap out high cards (Face cards = 10, Kings = 0). "
        + "Two cards of the same rank in a column cancel out to 0!"
    ),

    YOUR_TURN_SWAP(
        "Swap with a grid card",
        "Click any card in your grid to swap your held card into that position. "
        + "The card that was there moves to the discard pile face-up."
    ),

    YOUR_TURN_DISCARD_AND_FLIP(
        "Discard & flip",
        "You chose to discard. Now click any face-down card in your grid to flip it "
        + "face-up. You can't flip it back - choose a position you haven't seen yet!"
    ),

    BOT_TURN(
        "Bot's turn",
        "The bot is taking its turn. Watch the discard pile and the bot's grid "
        + "to see what it does. The bot plays randomly for this tutorial."
    ),

    COLUMN_CANCEL(
        "Column cancellation!",
        "When both cards in a column are the same rank, they cancel out - both are "
        + "removed from the grid and score 0. Keep an eye out for matching pairs!"
    ),

    FINAL_TURNS(
        "Final turns triggered!",
        "A player has flipped all their cards face-up, triggering the final round. "
        + "Every other player gets exactly one more turn, then scores are tallied. "
        + "Make your last move count!"
    ),

    ROUND_OVER(
        "Round over - see your score",
        "All cards are revealed and scored. Aces = 1, number cards = face value, "
        + "Kings = 0, Other face cards = 10, Twos = -2. "
        + "Cancelled columns score 0. The player with the lowest total wins!"
    ),

    TUTORIAL_COMPLETE(
        "Tutorial complete!",
        "You've played a full round of Golf! Head back to the lobby to start or join "
        + "a real game with friends. Good luck - may your score stay low!"
    );

    public final String title;
    public final String description;

    TutorialStep(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
