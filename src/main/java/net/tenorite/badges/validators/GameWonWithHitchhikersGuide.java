package net.tenorite.badges.validators;

import net.tenorite.badges.Badge;
import net.tenorite.badges.BadgeRepository;
import net.tenorite.badges.BadgeValidator;
import net.tenorite.badges.events.BadgeEarned;
import net.tenorite.game.Game;
import net.tenorite.game.PlayingStats;
import net.tenorite.game.events.GameFinished;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class GameWonWithHitchhikersGuide extends BadgeValidator {

    static final int THE_ANSWER_TO_THE_ULTIMATE_QUESTION_OF_LIFE = 42;

    private final List<Function<PlayingStats, Integer>> function;

    public GameWonWithHitchhikersGuide(Badge type, List<Function<PlayingStats, Integer>> function) {
        super(type);
        this.function = function;
    }

    @Override
    protected void doProcess(GameFinished gameFinished, BadgeRepository.BadgeOps badgeOps, Consumer<BadgeEarned> onBadgeEarned) {
        Game game = gameFinished.getGame();

        PlayingStats first = gameFinished.getRanking().get(0);
        PlayingStats second = gameFinished.getRanking().get(1);

        if (!first.getPlayer().isTeamPlayerOf(second.getPlayer())) {
            String name = first.getPlayer().getName();

            for (Function<PlayingStats, Integer> func : function) {
                if (func.apply(first) == THE_ANSWER_TO_THE_ULTIMATE_QUESTION_OF_LIFE) {
                    long nextLevel = badgeOps.getProgress(badge, name) + 1;
                    updateBadgeLevel(game, name, badge, nextLevel, badgeOps, onBadgeEarned);
                    return;
                }
            }
        }
    }

}
