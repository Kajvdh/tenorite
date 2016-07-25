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
package net.tenorite.stats.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import net.tenorite.stats.PlayerStatsRepository;
import net.tenorite.stats.actors.PlayingStatsActor;
import net.tenorite.stats.repository.MongoPlayerStatsRepository;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Johan Siebens
 */
@Configuration
public class PlayerStatsConfig {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private Jongo jongo;

    @Bean
    public PlayerStatsRepository playerStatsRepository() {
        return new MongoPlayerStatsRepository(jongo);
    }

    @Bean
    public ActorRef playerStatsActor() {
        return actorSystem.actorOf(PlayingStatsActor.props(playerStatsRepository()));
    }

}
