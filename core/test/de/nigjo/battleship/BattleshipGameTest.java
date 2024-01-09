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

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;

/**
 *
 * @author nigjo
 */
public class BattleshipGameTest
{
  @BeforeAll
  public static void createPlayerIds() throws IOException
  {
    Path p1 = Path.of("player1.id");
    Files.deleteIfExists(p1);
    KeyManager km1 = new KeyManager(p1);
    p1.toFile().deleteOnExit();
    Path p2 = Path.of("player2.id");
    Files.deleteIfExists(p2);
    KeyManager km2 = new KeyManager(p2);
    p2.toFile().deleteOnExit();
  }

  private Path playerId;

  @BeforeEach
  public void initTestId(TestInfo info) throws IOException
  {
    String name = info.getDisplayName()
        .replace("()", "")
        .replaceAll("[^\\w.-]", "_");
    playerId = Path.of("player-" + name + ".id");
    Files.deleteIfExists(playerId);
    playerId.toFile().deleteOnExit();
  }

  @Test
  public void testSetConfig()
  {
    String key = "sampleConfig";
    String value = "23";
    BattleshipGame game = new BattleshipGame(playerId);

    game.setConfig(key, value);

    Object data = game.getData("config.sampleConfig", Object.class);
    Assertions.assertInstanceOf(BattleshipGame.Config.class, data);
    BattleshipGame.Config cfg = (BattleshipGame.Config)data;
    assertEquals("sampleConfig", cfg.getKey());
    assertEquals("23", cfg.getValue());
  }

  @Test
  public void testGetConfig()
  {
    String key = "size";
    BattleshipGame instance = new BattleshipGame(playerId);

    Optional<BattleshipGame.Config> result = instance.getConfig(key);
    assertNotNull(result.get());

    assertEquals("size", result.get().getKey());
    assertEquals("10", result.get().getValue());
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

  @Test
  public void testInitRandomWithSeed()
  {
    long seed = 8_374_564_356L;
    BattleshipGame game = new BattleshipGame(playerId);
    game.initRandom(seed);

    String expected = ""
        + "N........."
        + "V........."
        + "V....WHE.."
        + "V..N......"
        + "S..V......"
        + "...S......"
        + "..WHHE...."
        + "....N....."
        + "....S....."
        + "..........";

    BoardData self = game.getData(BoardData.KEY_SELF, BoardData.class);
    assertEquals(expected, self.toString());
  }

  @Test
  public void testInitRandomCustomConfig()
  {
    System.out.println("initRandom");
    long seed = 87_356_238_745L;
    int size = 12;
    int[] ships =
    {
      7, 6, 5, 4, 3
    };

    String expected = ""
        + "............"
        + ".N...WHHHHE."
        + ".V.........."
        + ".S.........."
        + "....WHHHHHE."
        + ".......WHHHE"
        + "............"
        + "........N..."
        + "........V..."
        + "........V..."
        + "........S..."
        + "............";

    BattleshipGame game = new BattleshipGame(playerId);
    game.initRandom(seed, size, ships);

    BoardData self = game.getData(BoardData.KEY_SELF, BoardData.class);
    assertEquals(expected, self.toString());
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
