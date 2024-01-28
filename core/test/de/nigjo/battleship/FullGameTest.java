/*
 * Copyright 2024 nigjo.
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.io.LocalFileManager;

/**
 * Integration Test für ein vollständiges Spiel
 *
 * @author nigjo
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullGameTest
{
  private static Path gamefile;
//  private static boolean success;
  private static Random rnd1;
  private static Random rnd2;

  @BeforeAll
  public static void initFullGame() throws IOException
  {
    gamefile = Path.of("fullgame.bsg").toAbsolutePath();
    Files.deleteIfExists(gamefile);
//    success = true;

    long gameseed1 = "ThisIsPlayer1".hashCode();
    long gameseed2 = "ThisIsPlayer2".hashCode();
    rnd1 = new Random(gameseed1);
    rnd2 = new Random(gameseed2);
  }

  @Order(1)
  @TestFactory()
  public Iterator<DynamicTest> initializeNewGame()
  {
    AtomicReference<BattleshipGame> player1Ref = new AtomicReference<>();

    //BattleshipGame player1 = new BattleshipGame(Path.of("fullgame-p1.id"));
    String initState = "BattleshipGame.gamestate.init";
    return testrunnerOf(
        DynamicTest.dynamicTest("initPlayer1",
            () ->
        {
          player1Ref.set(new BattleshipGame(Path.of("fullgame-p1.id")));
          validateState(player1Ref.get(), initState);
        }),
        DynamicTest.dynamicTest("createGame",
            () -> createFilebasedGame(player1Ref.get())),
        DynamicTest.dynamicTest("createGame-state",
            () -> validateState(player1Ref.get(), BattleshipGame.STATE_PLACEMENT)),
        DynamicTest.dynamicTest("initPlayer1",
            () -> initPlayer(player1Ref.get(), rnd1, 5)),
        DynamicTest.dynamicTest("initPlayer1-state",
            () -> validateState(player1Ref.get(), BattleshipGame.STATE_WAIT_START))
    );
  }

  @Order(2)
  @TestFactory()
  public Iterator<DynamicTest> initializeOpponent()
  {
    AtomicReference<BattleshipGame> player2Ref = new AtomicReference<>();
    //BattleshipGame player2 = new BattleshipGame(Path.of("fullgame-p2.id"));
    String initState = "BattleshipGame.gamestate.init";
    return testrunnerOf(
        DynamicTest.dynamicTest("initPlayer2",
            () ->
        {
          player2Ref.set(new BattleshipGame(Path.of("fullgame-p2.id")));
          validateState(player2Ref.get(), initState);
        }),
        DynamicTest.dynamicTest("loadPlayer2",
            () -> loadGame(player2Ref.get(), 5)),
        DynamicTest.dynamicTest("loadPlayer2-state",
            () -> validateState(player2Ref.get(), BattleshipGame.STATE_PLACEMENT)),
        DynamicTest.dynamicTest("initPlayer2",
            () -> initPlayer(player2Ref.get(), rnd2, 7)),
        DynamicTest.dynamicTest("initPlayer2-state",
            () -> validateState(player2Ref.get(), BattleshipGame.STATE_WAIT_START))
    );
  }

  private Iterator<DynamicTest> testrunnerOf(DynamicTest... startupTests)
  {
    return Arrays.asList(startupTests).iterator();
    //System.out.println("dir="+System.getProperty("user.dir"));
//    Iterator<DynamicTest> testsRunner = new Iterator<>()
//    {
//      Iterator<DynamicTest> delegate = startupTests.iterator();
//
//      @Override
//      public boolean hasNext()
//      {
//        return delegate.hasNext();
//      }
//
//      @Override
//      public DynamicTest next()
//      {
//        return delegate.next();
//      }
//    };

//    return testsRunner;
  }

  private void validateState(BattleshipGame player, String expected)
  {
    System.err.println("- validateState()");
    player.updateState();

    String state = player.getState();

    assertEquals(expected, state);
  }

  private void createFilebasedGame(BattleshipGame player1) throws IOException
  {
    System.err.println("- createGame()");
    player1.createNewGame(new LocalFileManager(gamefile));

    Assertions.assertEquals(4,
        Files.readAllLines(gamefile, StandardCharsets.UTF_8).size());

//    player1.updateState();
//    String state = player1.getState();
//    assertEquals(BattleshipGame.STATE_PLACEMENT, state);
  }

  private void initPlayer(BattleshipGame player1, Random rnd, int expectedLines)
      throws IOException
  {
    System.err.println("- initPlayer1()");
    BoardData own = BoardData.generateRandom(10, rnd, BoardData.GAME_SIMPLE);
    player1.putData(BoardData.KEY_SELF, own);

    player1.storeOwnBoard();
    assertEquals(expectedLines,
        Files.readAllLines(gamefile, StandardCharsets.UTF_8).size());
  }

  private void loadGame(BattleshipGame player, int expectedLines) throws IOException
  {
    assertEquals(expectedLines,
        Files.readAllLines(gamefile, StandardCharsets.UTF_8).size());

    player.loadSavegame(new LocalFileManager(gamefile));
  }
}
