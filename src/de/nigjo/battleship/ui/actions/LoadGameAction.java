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
package de.nigjo.battleship.ui.actions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JFileChooser;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.data.Savegame;
import de.nigjo.battleship.ui.ActionBase;
import de.nigjo.battleship.ui.StatusLine;
import de.nigjo.battleship.util.Bundle;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class LoadGameAction extends ActionBase
{
  private String lastFolder;

  @Override
  public void actionPerformed(ActionEvent e)
  {
    JFileChooser chooser = new JFileChooser(lastFolder);
    if(JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null))
    {
      try
      {
        loadGame(chooser.getSelectedFile().toPath());
      }
      catch(IOException ex)
      {
        StatusLine.getDefault().setText("FEHLER: " + ex.getLocalizedMessage());
      }
    }
  }

  public static void loadGame(Path saveGameFile) throws IOException
  {
    BattleshipGame game =
        Storage.getDefault().find(BattleshipGame.class)
            .orElseThrow();

    Savegame savegame = Savegame.readFromFile(saveGameFile);
    game.putData(Savegame.class.getName(), savegame);

    String player1key =
        savegame.records(1, Savegame.Record.PLAYER)
            .findFirst().orElseThrow()
            .getPayload();
    String player2key =
        savegame.records(2, Savegame.Record.PLAYER)
            .findFirst()
            .map(Savegame.Record::getPayload)
            .orElse(null);

    KeyManager km = game.getData(KeyManager.KEY_MANAGER_SELF, KeyManager.class);
    if(player1key.equals(km.getPublicKey()))
    {
      StatusLine.getDefault().setText("Willkommen Spieler 1");
      game.putData(BattleshipGame.KEY_PLAYER_NUM, 1);
      loadBoardForPlayer(1, savegame, km, game);
      if(player2key == null)
      {
        StatusLine.getDefault().setText("Warte auf Spieler 2");
      }
      else
      {
        KeyManager opponent = new KeyManager(player2key);
        game.putData(KeyManager.KEY_MANAGER_OPPONENT, opponent);

        //TODO:savegame.playbackTo(gamedata, km, 1);
        boolean hasPlacedShipsForPlayer2 =
            savegame.records(2, Savegame.Record.BOARD)
                .findFirst().isPresent();
        if(!hasPlacedShipsForPlayer2)
        {
          StatusLine.getDefault().setText("Spieler 2 noch nicht bereit.");
          game.updateState(BattleshipGame.STATE_WAIT_START);
        }
        else
        {
          //TODO:savegame.playbackTo(gamedata, km, 2);
          game.updateState(BattleshipGame.STATE_ATTACK);
        }
      }
    }
    else
    {
      KeyManager opponent = new KeyManager(player1key);
      game.putData(KeyManager.KEY_MANAGER_OPPONENT, opponent);

      if(player2key == null)
      {
        //Noch kein Playerkey. Wir sind Spieler 2
        StatusLine.getDefault().setText("Willkommen Spieler 2");
        game.putData(BattleshipGame.KEY_PLAYER_NUM, 2);
        //nur Spieler 1 vorhanden. Spieler 2 (wir) am Zug
        game.clearBoards();
        savegame.addRecord(Savegame.Record.PLAYER, 2, km.getPublicKey());
        game.updateState(BattleshipGame.STATE_PLACEMENT);
      }
      else if(player2key.equals(km.getPublicKey()))
      {
        //Wir sind dem Spiel bereits beigetreten.
        StatusLine.getDefault().setText("Willkommen Spieler 2");
        game.putData(BattleshipGame.KEY_PLAYER_NUM, 2);
        if(!loadBoardForPlayer(2, savegame, km, game))
        {
          // noch keine Schiffe platziert. Wir sind dran.
          game.clearBoards();
          game.updateState(BattleshipGame.STATE_PLACEMENT);
        }
        else
        {
          //TODO:savegame.playbackTo(gamedata, km, 2);
          game.updateState(BattleshipGame.STATE_WAIT_ATTACK);
        }
      }
      else
      {
        StatusLine.getDefault().setText("Das Spiel hat bereits 2 Spieler.");
        game.clearBoards();
      }
    }
  }

  private static boolean loadBoardForPlayer(int player,
      Savegame savegame, KeyManager km, BattleshipGame game)
  {
    try
    {
      String encodedBoard =
          savegame.records(player, Savegame.Record.BOARD)
              .findFirst()
              .orElseThrow()
              .getPayload();
      String boarddata = km.decode(encodedBoard);
      if(boarddata == null)
      {
        throw new IllegalArgumentException(
            "board data for player " + player + " could not be decoded");
      }
      BoardData parsed = BoardData.parse(boarddata);
      game.putData(BoardData.KEY_SELF, parsed);
    }
    catch(NoSuchElementException noboard)
    {
      return false;
    }

    //2.Board ist anfangs immer "leer".
    game.clearBoard(true);

    game.putData(BattleshipGame.KEY_PLAYER_NUM, player);
    return true;
  }

  @Override
  public String getName()
  {
    return Bundle.getMessage(LoadGameAction.class, "LoadGameAction.name");
  }

  @Override
  public List<String> getPaths()
  {
    return List.of("Options");
  }

  @Override
  public int getPosition()
  {
    return 500;
  }

  @Override
  public Icon getIcon()
  {
    return loadIcon("icons8-globe-24(-hdpi).png");
  }

}
