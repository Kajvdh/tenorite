/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tenorite.badges.validators;

import net.tenorite.badges.Badge;
import net.tenorite.badges.BadgeRepository;
import net.tenorite.badges.BadgeValidator;
import net.tenorite.badges.events.BadgeEarned;
import net.tenorite.game.Game;
import net.tenorite.game.PlayingStats;
import net.tenorite.game.events.GameFinished;

import java.util.function.Consumer;

/**
 * @author Johan Siebens
 */
public final class NrOfGamesPlayed extends BadgeValidator {

    private final int target;

    public NrOfGamesPlayed(Badge badgeType, int target) {
        super(badgeType);
        this.target = target;
    }

    @Override
    protected void doProcess(GameFinished gameFinished, BadgeRepository.BadgeOps badgeOps, Consumer<BadgeEarned> onBadgeEarned) {
        gameFinished.getRanking().forEach(p -> validateBadge(gameFinished.getGame(), p, badgeOps, onBadgeEarned));
    }

    private void validateBadge(Game game, PlayingStats p, BadgeRepository.BadgeOps badgeOps, Consumer<BadgeEarned> onBadgeEarned) {
        String name = p.getPlayer().getName();
        long count = badgeOps.getProgress(badge, name) + 1;
        updateBadgeLevelAndProgressWhenTargetIsReached(game, name, badge, count, target, badgeOps, onBadgeEarned);
    }

}
