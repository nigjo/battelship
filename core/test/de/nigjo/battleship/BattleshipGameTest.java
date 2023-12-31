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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;

/**
 *
 * @author nigjo
 */
public class BattleshipGameTest
{

  public BattleshipGameTest()
  {
  }

  @BeforeAll
  public static void createPlayerIds() throws IOException
  {
    Path p1 = Path.of("player1.id");
    Files.deleteIfExists(p1);
    KeyManager km1 = new KeyManager(p1);
    Path p2 = Path.of("player2.id");
    Files.deleteIfExists(p2);
    KeyManager km2 = new KeyManager(p2);
  }

  //@Test
  public void testSetConfig()
  {
    System.out.println("setConfig");
    String key = "";
    String value = "";
    BattleshipGame instance = null;
    instance.setConfig(key, value);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetConfig()
  {
    System.out.println("getConfig");
    String key = "";
    BattleshipGame instance = null;
    Optional<BattleshipGame.Config> expResult = null;
    Optional<BattleshipGame.Config> result = instance.getConfig(key);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testClearBoards()
  {
    System.out.println("clearBoards");
    BattleshipGame instance = null;
    instance.clearBoards();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testClearBoard()
  {
    System.out.println("clearBoard");
    boolean opponent = false;
    BattleshipGame instance = null;
    instance.clearBoard(opponent);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test if all Ships are placed; somewhere.
   */
  @Test
  public void testInitRandom()
  {
    Path playerId = Path.of("player-initRandom.id");
    BattleshipGame instance = new BattleshipGame(playerId);
    instance.initRandom();

    BoardData board = instance.getData(BoardData.KEY_SELF, BoardData.class);
    int size = board.getSize();
    int shipCells = 0;
    for(int x = 0; x < size; ++x)
    {
      for(int y = 0; y < size; ++y)
      {
        if((board.stateAt(x, y) & BoardData.SHIP) != 0)
        {
          ++shipCells;
        }
      }
    }
    assertEquals(17, shipCells);
  }

  //@Test
  public void testInitRandom_long()
  {
    System.out.println("initRandom");
    long seed = 0L;
    BattleshipGame instance = null;
    instance.initRandom(seed);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testInitRandom_3args()
  {
    System.out.println("initRandom");
    long seed = 0L;
    int size = 0;
    int[] ships = null;
    BattleshipGame instance = null;
    instance.initRandom(seed, size, ships);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testCreateNewGame() throws Exception
  {
    System.out.println("createNewGame");
    Path savegameFile = null;
    BattleshipGame instance = null;
    instance.createNewGame(savegameFile);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testUpdateState_0args()
  {
    System.out.println("updateState");
    BattleshipGame instance = null;
    instance.updateState();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  //@Test
  public void testUpdateState_String()
  {
    System.out.println("updateState");
    String state = "";
    BattleshipGame instance = null;
    instance.updateState(state);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
