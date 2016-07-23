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
package net.tenorite.badges.repository;

import net.tenorite.AbstractTenoriteServerTestCase;
import net.tenorite.badges.Badge;
import net.tenorite.badges.BadgeLevel;
import net.tenorite.badges.BadgeLevelBuilder;
import net.tenorite.badges.BadgeRepository;
import net.tenorite.core.Tempo;
import net.tenorite.game.GameModeId;
import org.jongo.Jongo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author Johan Siebens
 */
public class MongoBadgeRepositoryTest extends AbstractTenoriteServerTestCase {

    @Autowired
    private Jongo jongo;

    @Autowired
    private BadgeRepository badgeRepository;

    @Before
    public void clear() {
        stream(Tempo.values()).forEach(t -> MongoBadgeRepository.progressCollection(jongo, t).drop());
        stream(Tempo.values()).forEach(t -> MongoBadgeRepository.badgeCollection(jongo, t).drop());
    }

    @Test
    public void testSaveAndGetBadge() {
        Badge type = Badge.of(GameModeId.of("CLASSIC"), "lorem");
        BadgeLevel badge = BadgeLevel.of(Tempo.NORMAL, type, "john", 100, 1, "gameA");

        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(badge);
        Optional<BadgeLevel> actualBadge = badgeRepository.badgeOps(Tempo.FAST).getBadgeLevel("john", type);

        assertThat(actualBadge).hasValue(badge);
    }

    @Test
    public void testUpdateLevelAndGetBadge() {
        Badge type = Badge.of(GameModeId.of("CLASSIC"), "lorem");
        BadgeLevel badge = BadgeLevel.of(Tempo.NORMAL, type, "john", 100, 1, "gameA");

        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(new BadgeLevelBuilder().from(badge).level(5).build());
        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(new BadgeLevelBuilder().from(badge).level(10).build());
        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(new BadgeLevelBuilder().from(badge).level(15).build());
        Optional<BadgeLevel> actualBadge = badgeRepository.badgeOps(Tempo.FAST).getBadgeLevel("john", type);

        assertThat(actualBadge).hasValue(new BadgeLevelBuilder().from(badge).level(15).build());
    }

    @Test
    public void testSaveAndListBadgesByType() {
        Badge typeA = Badge.of(GameModeId.of("CLASSIC"), "lorem");
        Badge typeB = Badge.of(GameModeId.of("CLASSIC"), "ipsum");

        BadgeLevel badgeA = BadgeLevel.of(Tempo.FAST, typeA, "john", 100, 1, "gameA");
        BadgeLevel badgeB = BadgeLevel.of(Tempo.FAST, typeB, "john", 100, 1, "gameB");

        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(new BadgeLevelBuilder().from(badgeA).level(5).build());
        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(new BadgeLevelBuilder().from(badgeA).level(10).build());
        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(new BadgeLevelBuilder().from(badgeA).level(15).build());
        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(badgeB);

        List<BadgeLevel> badgesA = badgeRepository.badgeOps(Tempo.FAST).badgeLevels(typeA);
        List<BadgeLevel> badgesB = badgeRepository.badgeOps(Tempo.FAST).badgeLevels(typeB);

        assertThat(badgesA).containsExactly(new BadgeLevelBuilder().from(badgeA).level(15).build());
        assertThat(badgesB).containsExactly(badgeB);
    }

    @Test
    public void testSaveAndGetBadgesByName() {
        Badge typeA = Badge.of(GameModeId.of("CLASSIC"), "lorem");
        Badge typeB = Badge.of(GameModeId.of("CLASSIC"), "ipsum");

        BadgeLevel badgeA = BadgeLevel.of(Tempo.FAST, typeA, "john", 100, 1, "gameA");
        BadgeLevel badgeB = BadgeLevel.of(Tempo.FAST, typeB, "john", 100, 1, "gameB");

        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(badgeA);
        badgeRepository.badgeOps(Tempo.FAST).saveBadgeLevel(badgeB);

        Map<Badge, BadgeLevel> badges = badgeRepository.badgeOps(Tempo.FAST).badgeLevels(GameModeId.of("CLASSIC"), "john");

        assertThat(badges).containsOnly(entry(typeA, badgeA), entry(typeB, badgeB));
    }

}
