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
package net.tenorite.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import net.tenorite.util.ImmutableStyle;
import org.immutables.value.Value;

/**
 * @author Johan Siebens
 */
@Value.Immutable
@ImmutableStyle
public abstract class GameModeId implements Comparable<GameModeId> {

    @JsonCreator
    public static GameModeId of(String name) {
        return new GameModeIdBuilder().value(name).build();
    }

    abstract String value();

    @Override
    public int compareTo(GameModeId o) {
        return value().compareTo(o.value());
    }

    @Override
    @JsonValue
    public String toString() {
        return value();
    }

}
