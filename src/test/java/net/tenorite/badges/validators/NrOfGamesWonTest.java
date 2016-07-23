package net.tenorite.badges.validators;

import net.tenorite.badges.Badge;
import net.tenorite.badges.BadgeLevel;
import net.tenorite.badges.BadgeRepositoryStub;
import net.tenorite.badges.BadgeType;
import net.tenorite.badges.events.BadgeEarned;
import net.tenorite.core.Tempo;
import net.tenorite.game.Game;
import net.tenorite.game.GameModeId;
import net.tenorite.game.Player;
import net.tenorite.game.PlayingStats;
import net.tenorite.game.events.GameFinished;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Johan Siebens
 */
public class NrOfGamesWonTest {

    private static final GameModeId GAME_MODE_ID = GameModeId.of("JUNIT");

    private static final BadgeType BADGE_TYPE = BadgeType.of("junit");

    private static final Badge BADGE = Badge.of(GAME_MODE_ID, BADGE_TYPE);

    private BadgeRepositoryStub badgeRepository = new BadgeRepositoryStub();

    private List<BadgeEarned> published = new ArrayList<>();

    private NrOfGamesWon validator;

    @Before
    public void setUp() {
        badgeRepository.clear();
        published.clear();

        validator = new NrOfGamesWon(BADGE, 10);
    }

    @Test
    public void testProgressIsIncreasedWhenPlayerWinsAGame() {
        PlayingStats player1 = PlayingStats.of(Player.of(1, "john", null));
        PlayingStats player2 = PlayingStats.of(Player.of(2, "jane", null));
        PlayingStats player3 = PlayingStats.of(Player.of(3, "nick", null));

        Game game = Game.of("id", 0, 100, Tempo.NORMAL, GAME_MODE_ID, emptyList(), emptyList());
        GameFinished gameFinished = GameFinished.of(game, asList(player1, player2, player3));

        validator.process(gameFinished, badgeRepository, e -> published.add(e));

        assertThat(published).isEmpty();

        assertThat(badgeRepository.getProgress(BADGE, "john")).isEqualTo(1);
        assertThat(badgeRepository.getProgress(BADGE, "jane")).isEqualTo(0);
        assertThat(badgeRepository.getProgress(BADGE, "nick")).isEqualTo(0);
    }

    @Test
    public void testEarnBadge() {
        badgeRepository.updateProgress(BADGE, "john", 9);
        badgeRepository.updateProgress(BADGE, "jane", 9);
        badgeRepository.updateProgress(BADGE, "nick", 7);

        PlayingStats player1 = PlayingStats.of(Player.of(1, "john", null));
        PlayingStats player2 = PlayingStats.of(Player.of(2, "jane", null));
        PlayingStats player3 = PlayingStats.of(Player.of(3, "nick", null));

        Game game = Game.of("id", 0, 100, Tempo.NORMAL, GAME_MODE_ID, emptyList(), emptyList());
        GameFinished gameFinished = GameFinished.of(game, asList(player1, player2, player3));

        validator.process(gameFinished, badgeRepository, e -> published.add(e));

        BadgeLevel expected = BadgeLevel.of(Tempo.NORMAL, BADGE, "john", 0, 1, "id");

        assertThat(published).containsExactly(BadgeEarned.of(expected, false));

        assertThat(badgeRepository.getBadgeLevel("john", BADGE).isPresent()).isTrue();
        assertThat(badgeRepository.getBadgeLevel("jane", BADGE).isPresent()).isFalse();
        assertThat(badgeRepository.getBadgeLevel("nick", BADGE).isPresent()).isFalse();
        assertThat(badgeRepository.getProgress(BADGE, "john")).isEqualTo(0);
        assertThat(badgeRepository.getProgress(BADGE, "jane")).isEqualTo(9);
        assertThat(badgeRepository.getProgress(BADGE, "nick")).isEqualTo(7);
    }

    @Test
    public void testUpgradeBadge() {
        badgeRepository.saveBadgeLevel(BadgeLevel.of(Tempo.NORMAL, BADGE, "john", 1000, 4, "gameId"));
        badgeRepository.updateProgress(BADGE, "john", 9);

        PlayingStats player1 = PlayingStats.of(Player.of(1, "john", null));
        PlayingStats player2 = PlayingStats.of(Player.of(2, "jane", null));
        PlayingStats player3 = PlayingStats.of(Player.of(3, "nick", null));

        Game game = Game.of("id", 0, 100, Tempo.NORMAL, GAME_MODE_ID, emptyList(), emptyList());
        GameFinished gameFinished = GameFinished.of(game, asList(player1, player2, player3));

        validator.process(gameFinished, badgeRepository, e -> published.add(e));

        BadgeLevel expected = BadgeLevel.of(Tempo.NORMAL, BADGE, "john", 0, 5, "id");

        assertThat(published).containsExactly(BadgeEarned.of(expected, true));

        assertThat(badgeRepository.getBadgeLevel("john", BADGE).isPresent()).isTrue();
        assertThat(badgeRepository.getProgress(BADGE, "john")).isEqualTo(0);
        assertThat(badgeRepository.getProgress(BADGE, "jane")).isEqualTo(0);
        assertThat(badgeRepository.getProgress(BADGE, "nick")).isEqualTo(0);
    }

}
