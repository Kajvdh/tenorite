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
package net.tenorite.websocket;

import akka.actor.ActorSystem;
import net.tenorite.channel.actors.ChannelsActors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author Johan Siebens
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ActorSystem system;

    private final ChannelsActors channels;

    @Autowired
    public WebSocketConfig(ActorSystem system, ChannelsActors channels) {
        this.system = system;
        this.channels = channels;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(spectateWebSocketHandler(), "/ws/spectate")
            .withSockJS()
            .setInterceptors(spectateHandshakeInterceptor());
    }

    @Bean
    public SpectateWebSocketHandler spectateWebSocketHandler() {
        return new SpectateWebSocketHandler(system, channels);
    }

    @Bean
    public SpectateHandshakeInterceptor spectateHandshakeInterceptor() {
        return new SpectateHandshakeInterceptor();
    }

}
