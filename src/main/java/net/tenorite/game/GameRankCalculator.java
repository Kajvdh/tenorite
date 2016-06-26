package net.tenorite.game;

import net.tenorite.core.Special;
import net.tenorite.protocol.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public final class GameRankCalculator {

    public List<PlayingStats> calculate(Game game) {
        return new Calculator(game).process();
    }

    private static class Calculator {

        private final Game game;

        private final LinkedList<PlayingStats> ranking = new LinkedList<>();

        private final Map<Integer, Player> players = new LinkedHashMap<>();

        private final Map<Integer, Long> playingTimes = new HashMap<>();

        private final Map<Integer, Integer> levels = new HashMap<>();

        private final Map<Integer, Integer> twoLineCombos = new HashMap<>();

        private final Map<Integer, Integer> threeLineCombos = new HashMap<>();

        private final Map<Integer, Integer> fourLineCombos = new HashMap<>();

        private final Map<Integer, Integer> lastFieldHeights = new HashMap<>();

        private final Map<Integer, Integer> maxFieldHeights = new HashMap<>();

        private final Map<Integer, Integer> blocks = new HashMap<>();

        private final Map<Integer, Map<Special, Integer>> specialsReceived = new HashMap<>();

        private final Map<Integer, Map<Special, Integer>> specialsOnSelf = new HashMap<>();

        private final Map<Integer, Map<Special, Integer>> specialsOnOpponent = new HashMap<>();

        private final Map<Integer, Map<Special, Integer>> specialsOnTeamPlayer = new HashMap<>();

        private Calculator(Game game) {
            this.game = game;
            this.players.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, identity())));

            this.levels.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> 0)));
            this.twoLineCombos.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> 0)));
            this.threeLineCombos.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> 0)));
            this.fourLineCombos.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> 0)));
            this.maxFieldHeights.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> 0)));
            this.lastFieldHeights.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> 0)));
            this.blocks.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> -1)));

            this.specialsOnOpponent.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> initSpecialsCounts())));
            this.specialsReceived.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> initSpecialsCounts())));
            this.specialsOnSelf.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> initSpecialsCounts())));
            this.specialsOnTeamPlayer.putAll(game.getPlayers().stream().collect(toMap(Player::getSlot, p -> initSpecialsCounts())));
        }

        private List<PlayingStats> process() {
            game.getMessages().forEach(this::process);
            players.values().stream().map(this::stats).forEach(ranking::addFirst);
            return new LinkedList<>(ranking);
        }

        private void process(GameMessage m) {
            if (m.getMessage() instanceof PlayerLostMessage) {
                process(m.getTimestamp(), (PlayerLostMessage) m.getMessage());
            }
            else if (m.getMessage() instanceof PlayerLeaveMessage) {
                process((PlayerLeaveMessage) m.getMessage());
            }
            else if (m.getMessage() instanceof LvlMessage) {
                process((LvlMessage) m.getMessage());
            }
            else if (m.getMessage() instanceof FieldMessage) {
                process((FieldMessage) m.getMessage());
            }
            else if (m.getMessage() instanceof ClassicStyleAddMessage) {
                process((ClassicStyleAddMessage) m.getMessage());
            }
            else if (m.getMessage() instanceof SpecialBlockMessage) {
                process((SpecialBlockMessage) m.getMessage());
            }
        }

        private void process(long timestamp, PlayerLostMessage message) {
            ofNullable(players.remove(message.getSender())).ifPresent(loser -> {
                playingTimes.put(loser.getSlot(), timestamp);
                ranking.addFirst(stats(loser));
            });
        }

        private void process(LvlMessage message) {
            levels.put(message.getSender(), message.getLevel());
        }

        private void process(ClassicStyleAddMessage message) {
            int sender = message.getSender();
            switch (message.getLines()) {
                case 1:
                    twoLineCombos.computeIfPresent(sender, (i, c) -> c + 1);
                    break;
                case 2:
                    threeLineCombos.computeIfPresent(sender, (i, c) -> c + 1);
                    break;
                case 4:
                    fourLineCombos.computeIfPresent(sender, (i, c) -> c + 1);
                    break;
            }
            removeBlockCounts(sender);
        }

        private void process(SpecialBlockMessage message) {
            int sender = message.getSender();
            int target = message.getTarget();
            Special special = message.getSpecial();

            ofNullable(players.get(target)).ifPresent(decr(blocks));

            if (target == sender) {
                ofNullable(players.get(sender)).ifPresent(incrSpecialOnSelf(special));
            }
            else {
                Optional<Player> optTargetPlayer = ofNullable(players.get(target));
                Optional<Player> optSenderPlayer = ofNullable(players.get(sender));

                if (optSenderPlayer.isPresent() && optTargetPlayer.isPresent()) {
                    Player senderPlayer = optSenderPlayer.get();
                    Player targetPlayer = optTargetPlayer.get();

                    if (senderPlayer.isTeamPlayerOf(targetPlayer)) {
                        incrSpecialOnTeamPlayer(special).accept(senderPlayer);
                    }
                    else {
                        incrSpecialOnOpponent(special).accept(senderPlayer);
                    }

                    incrSpecialReceived(special).accept(targetPlayer);
                }
            }
        }

        private void process(FieldMessage message) {
            int slot = message.getSender();
            int currentHeight = Field.of(message.getUpdate()).getHighest();
            lastFieldHeights.put(slot, currentHeight);
            maxFieldHeights.compute(slot, (k, v) -> max(v, currentHeight));
            blocks.compute(slot, (i, c) -> c + 1);
        }

        private void process(PlayerLeaveMessage message) {
            players.remove(message.getSender());
        }

        private boolean removeBlockCounts(int sender) {
            if (game.getGameRules().getClassicRules() || sender == 0) {
                if (sender == 0) {
                    players.values().stream().forEach(decr(blocks));
                }
                else {
                    Player player = players.get(sender);
                    if (player != null) {
                        players.values().stream()
                            .filter(o -> !o.equals(player))
                            .filter(o -> !o.isTeamPlayerOf(player))
                            .forEach(decr(blocks));
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }

        private Consumer<Player> decr(Map<Integer, Integer> counts) {
            return p -> counts.compute(p.getSlot(), (i, c) -> c - 1);
        }

        private Consumer<Player> incrSpecialOnOpponent(Special special) {
            return p -> specialsOnOpponent.get(p.getSlot()).compute(special, (i, c) -> c + 1);
        }

        private Consumer<Player> incrSpecialOnTeamPlayer(Special special) {
            return p -> specialsOnTeamPlayer.get(p.getSlot()).compute(special, (i, c) -> c + 1);
        }

        private Consumer<Player> incrSpecialReceived(Special special) {
            return p -> specialsReceived.get(p.getSlot()).compute(special, (i, c) -> c + 1);
        }

        private Consumer<Player> incrSpecialOnSelf(Special special) {
            return p -> specialsOnSelf.get(p.getSlot()).compute(special, (i, c) -> c + 1);
        }

        private PlayingStats stats(Player player) {
            GameRules gameRules = game.getGameRules();
            int slot = player.getSlot();

            return PlayingStats.of(
                player,
                playingTimes.getOrDefault(slot, game.getDuration()),
                levels.get(slot),
                max(0, levels.get(slot) - gameRules.getStartingLevel()) * gameRules.getLinesPerLevel(),
                twoLineCombos.get(slot),
                threeLineCombos.get(slot),
                fourLineCombos.get(slot),
                lastFieldHeights.get(slot),
                maxFieldHeights.get(slot),
                max(blocks.get(slot), 0),
                specialsReceived.get(slot),
                specialsOnOpponent.get(slot),
                specialsOnTeamPlayer.get(slot),
                specialsOnSelf.get(slot)
            );
        }

    }

    private static Map<Special, Integer> initSpecialsCounts() {
        return Arrays.stream(Special.values()).collect(Collectors.toMap(Function.identity(), s -> 0));
    }

}
