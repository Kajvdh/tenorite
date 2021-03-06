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
package net.tenorite.winlist;

import net.tenorite.core.Tempo;
import net.tenorite.game.GameModeId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author Johan Siebens
 */
public class WinlistRepositoryStub implements WinlistRepository {

    private Map<String, Map<String, WinlistItem>> data = new HashMap<>();

    @Override
    public WinlistOps winlistOps(Tempo tempo) {
        return new OpsImpl(tempo);
    }

    private class OpsImpl implements WinlistOps {

        private final Tempo tempo;

        public OpsImpl(Tempo tempo) {
            this.tempo = tempo;
        }

        @Override
        public Optional<WinlistItem> getWinlistItem(GameModeId mode, WinlistItem.Type type, String name) {
            return Optional.ofNullable(winlist(mode).get(type.getLetter() + name));
        }

        @Override
        public void saveWinlistItem(GameModeId mode, WinlistItem score) {
            winlist(mode).put(score.getType().getLetter() + score.getName(), score);
        }

        @Override
        public List<WinlistItem> loadWinlist(GameModeId mode) {
            return winlist(mode).values().stream().sorted((a, b) -> new Long(b.getScore()).compareTo(a.getScore())).collect(toList());
        }

        private Map<String, WinlistItem> winlist(GameModeId mode) {
            return data.computeIfAbsent(tempo + ":winlist:" + mode, s -> new HashMap<>());
        }

    }

    public void clear() {
        this.data.clear();
    }

}
