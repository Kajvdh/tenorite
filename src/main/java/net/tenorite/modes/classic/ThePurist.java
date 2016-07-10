package net.tenorite.modes.classic;

import net.tenorite.badges.Badge;
import net.tenorite.badges.BadgeRepository;
import net.tenorite.badges.BadgeValidator;
import net.tenorite.badges.events.BadgeEarned;
import net.tenorite.game.Game;
import net.tenorite.game.PlayingStats;
import net.tenorite.game.events.GameFinished;

import java.util.function.Consumer;

class ThePurist extends BadgeValidator {

    ThePurist(Badge badge) {
        super(badge);
    }

    @Override
    protected void doProcess(GameFinished gameFinished, BadgeRepository.BadgeOps badgeOps, Consumer<BadgeEarned> onBadgeEarned) {
        Game game = gameFinished.getGame();

        PlayingStats first = gameFinished.getRanking().get(0);
        PlayingStats second = gameFinished.getRanking().get(1);

        if (!first.getPlayer().isTeamPlayerOf(second.getPlayer())) {
            String name = first.getPlayer().getName();
            boolean noSpecialsUsed = first.getTotalNrOfSpecialsUsed() == 0;

            if (noSpecialsUsed) {
                long nextLevel = badgeOps.getProgress(badge, name) + 1;
                updateBadgeLevel(game, name, badge, nextLevel, badgeOps, onBadgeEarned);
            }
        }
    }

}