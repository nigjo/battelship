/*
 * Copyright 2023 nigjo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.nigjo.battleship;

import java.nio.file.Path;
import java.util.Random;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class BattleshipGame
{
  private final Storage gamedata;

  public BattleshipGame(Path playerId)
  {
    this.gamedata = new Storage();
    this.gamedata.put(BoardData.KEY_SELF, new BoardData(10));
    BoardData op = new BoardData(10);
    op.setOpponent(true);
    this.gamedata.put(BoardData.KEY_OPPONENT, op);

    gamedata.put(KeyManager.KEY_MANAGER_SELF, new KeyManager(playerId));
  }

  public Storage getGamedata()
  {
    return gamedata;
  }

  public void initRandom()
  {
    initRandom(System.currentTimeMillis());
  }

  public void initRandom(long seed)
  {
    Random rnd = new Random(seed);
    BoardData own = BoardData.generateRandom(10, rnd, BoardData.GAME_SIMPLE);
    gamedata.put(BoardData.KEY_SELF, own);
    BoardData opponent = new BoardData(10);
    opponent.setOpponent(true);
    gamedata.put(BoardData.KEY_OPPONENT, opponent);
  }

}
