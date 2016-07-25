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
package net.tenorite.protocol;

import net.tenorite.core.Tempo;
import net.tenorite.util.ImmutableStyle;
import org.immutables.value.Value;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author Johan Siebens
 */
@Value.Immutable
@ImmutableStyle
public abstract class WinlistMessage implements Message {

    public static WinlistMessage of(List<String> winlist) {
        return new WinlistMessageBuilder().winlist(winlist).build();
    }

    public abstract List<String> getWinlist();

    @Override
    public String raw(Tempo tempo) {
        return "winlist " + getWinlist().stream().collect(joining(" "));
    }

}
