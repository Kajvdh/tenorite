package net.tenorite.channel.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import net.tenorite.channel.Channel;
import net.tenorite.channel.Channels;
import net.tenorite.channel.commands.CreateChannel;
import net.tenorite.channel.commands.ListChannels;
import net.tenorite.channel.commands.ReserveSlot;
import net.tenorite.channel.events.ChannelCreated;
import net.tenorite.channel.events.ChannelCreationFailed;
import net.tenorite.channel.events.SlotReservationFailed;
import net.tenorite.core.Tempo;
import net.tenorite.game.GameMode;
import net.tenorite.game.GameModes;
import net.tenorite.util.AbstractActor;
import scala.Option;
import scala.concurrent.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static akka.dispatch.Futures.sequence;
import static akka.pattern.Patterns.ask;
import static java.util.stream.StreamSupport.stream;

public class ChannelsActor extends AbstractActor {

    public static Props props(GameModes gameModes) {
        return Props.create(ChannelsActor.class, gameModes);
    }

    private final GameModes gameModes;

    private final Map<Tempo, ActorRef> actors = new HashMap<>();

    public ChannelsActor(GameModes gameModes) {
        this.gameModes = gameModes;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        actors.put(Tempo.NORMAL, context().actorOf(Props.create(ChannelsPerTempoActor.class, Tempo.NORMAL, gameModes), Tempo.NORMAL.name()));
        actors.put(Tempo.FAST, context().actorOf(Props.create(ChannelsPerTempoActor.class, Tempo.FAST, gameModes), Tempo.FAST.name()));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof CreateChannel) {
            CreateChannel cc = (CreateChannel) o;
            actors.get(cc.getTempo()).forward(o, context());
        }
        else if (o instanceof ReserveSlot) {
            ReserveSlot rs = (ReserveSlot) o;
            actors.get(rs.getTempo()).forward(o, context());
        }
        else if (o instanceof ListChannels) {
            ListChannels lc = (ListChannels) o;
            actors.get(lc.getTempo()).forward(o, context());
        }
    }

    private static class ChannelsPerTempoActor extends AbstractActor {

        private final Tempo tempo;

        private final GameModes gameModes;

        public ChannelsPerTempoActor(Tempo tempo, GameModes gameModes) {
            this.tempo = tempo;
            this.gameModes = gameModes;
        }

        @Override
        public void preStart() throws Exception {
            super.preStart();
            for (GameMode gameMode : gameModes) {
                String name = gameMode.getId().toString().toLowerCase();
                context().actorOf(ChannelActor.props(tempo, gameMode, name, false), name);
            }
        }

        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof CreateChannel) {
                createChannel((CreateChannel) o);
            }
            else if (o instanceof ReserveSlot) {
                handleReserveSlot((ReserveSlot) o);
            }
            else if (o instanceof ListChannels) {
                handleListChannels((ListChannels) o);
            }
        }

        private void handleReserveSlot(ReserveSlot o) {
            Option<ActorRef> channel = context().child(o.getChannel());

            if (channel.isDefined()) {
                channel.get().forward(o, context());
            }
            else {
                replyWith(SlotReservationFailed.channelNotAvailable());
            }
        }

        private void handleListChannels(ListChannels listChannels) {
            ActorRef sender = sender();
            ActorRef self = self();

            ExecutionContext ec = context().dispatcher();
            Iterable<ActorRef> children = getContext().getChildren();

            sequence(
                stream(children.spliterator(), false)
                    .map(c -> ask(c, listChannels, 20).map(mapper(o -> (Channel) o), ec))
                    .collect(Collectors.toList()),
                ec
            ).onSuccess(onSuccess(i -> sender.tell(Channels.of(i), self)), ec);
        }

        private void createChannel(CreateChannel c) {
            if (context().child(c.getName()).isDefined()) {
                replyWith(ChannelCreationFailed.nameAlreadyInUse());
                return;
            }

            if (!isValid(c.getName())) {
                replyWith(ChannelCreationFailed.invalidName());
                return;
            }

            Optional<GameMode> optGameMode = gameModes.find(c.getGameModeId());

            if (!optGameMode.isPresent()) {
                replyWith(ChannelCreationFailed.invalidGameMode());
                return;
            }

            context().actorOf(ChannelActor.props(c.getTempo(), optGameMode.get(), c.getName(), c.isEphemeral()), c.getName());
            replyWith(ChannelCreated.of(c.getTempo(), c.getGameModeId(), c.getName()));
        }

        private boolean isValid(String name) {
            return name.length() >= 2 && name.length() <= 25 && Pattern.matches("[a-z0-9:_-]+", name);
        }

    }

}
