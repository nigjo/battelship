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
  public static final String KEY_PLAYER_NUM = "BattleshipGame.player";
  public static final String KEY_STATE = "BattleshipGame.gamestate";
  public static final String STATE_PLACEMENT = "BattleshipGame.gamestate.placement";
  public static final String STATE_WAIT_START = "BattleshipGame.gamestate.waitForStart";
  public static final String STATE_WAIT_RESPONSE =
      "BattleshipGame.gamestate.waitForResult";
  public static final String STATE_WAIT_ATTACK = "BattleshipGame.gamestate.waitForAttack";
  public static final String STATE_FINISHED = "BattleshipGame.gamestate.endOfGame";

  public BattleshipGame(Path playerId)
  {
    this.gamedata = new Storage();
    this.gamedata.put(BoardData.KEY_SELF, new BoardData(10));
    BoardData op = new BoardData(10);
    op.setOpponent(true);
    this.gamedata.put(BoardData.KEY_OPPONENT, op);

    gamedata.put(KeyManager.KEY_MANAGER_SELF, new KeyManager(playerId));

    //just run-test the keymanager
    validateKeyManager();
  }

  private void validateKeyManager()
  {
    KeyManager km = gamedata.get(KeyManager.KEY_MANAGER_SELF, KeyManager.class);

    validate(km, "BattleShip");
    validate(km, "A".repeat(100));

    String plainBoard =
        this.gamedata.get(BoardData.KEY_SELF, BoardData.class).toString();
    validate(km, plainBoard);
  }

  private static void validate(KeyManager km, String expected)
  {
    String resultE1 = km.encode(expected);
    String resultD1 = km.decode(resultE1);
    if(!expected.equals(resultD1))
    {
      throw new IllegalStateException("expected " + expected + " but got " + resultD1);
    }
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
