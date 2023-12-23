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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.data.Savegame;
import de.nigjo.battleship.ui.ActionBase;
import de.nigjo.battleship.ui.ActionsManager;
import de.nigjo.battleship.ui.DialogDisplayer;
import de.nigjo.battleship.util.Bundle;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class NewGameAction extends ActionBase
{
  private File lastFolder = null;

  @Override
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      Storage gamedata =
          Storage.getDefault().find(BattleshipGame.class)
              .map(BattleshipGame::getGamedata)
              .orElseThrow();
      KeyManager km = gamedata.get(KeyManager.KEY_MANAGER_SELF, KeyManager.class);
      createNewGame(gamedata, km);
    }
    catch(IllegalStateException ex)
    {
      DialogDisplayer.getDefault()
          .showError((String)getValue(NAME), ex.getLocalizedMessage());
    }
    catch(RuntimeException ex)
    {
      Logger.getLogger(NewGameAction.class.getName())
          .log(Level.WARNING, ex.getLocalizedMessage(), ex);
      DialogDisplayer.getDefault().showError(getName(), ex.toString());
    }
  }

  private void createNewGame(Storage gamedata, KeyManager km)
  {
    JFileChooser chooser = new JFileChooser(lastFolder);
    if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(ActionsManager.getFrame()))
    {
      File savegameFile = chooser.getSelectedFile();
      lastFolder = savegameFile.getParentFile();
      if(savegameFile.exists())
      {
        int answer = DialogDisplayer.getDefault()
            .showQuestion(getName(),
                "Die Spielstanddatei existiert bereit. Soll die Datei überschrieben werden?");
        if(JOptionPane.YES_OPTION == answer)
        {
          createNewGame(savegameFile.toPath(), gamedata, km, 10, BoardData.GAME_SIMPLE);
        }
      }
      else
      {
        createNewGame(savegameFile.toPath(), gamedata, km, 10, BoardData.GAME_SIMPLE);
      }
    }
  }

  public static void createNewGame(Path savegameFile,
      Storage gamedata, KeyManager km,
      int size, int... ships)
  {
    Savegame savegame = Savegame.createNew(ships);
    savegame.addRecord(
        new Savegame.Record(Savegame.Record.PLAYER, 1, km.getPublicKey()));
    try
    {
      savegame.storeToFile(savegameFile);
      gamedata.put(Savegame.class.getName(), savegame);

      BattleshipGame.clearBoards(gamedata, size);
      gamedata.put(BattleshipGame.KEY_PLAYER_NUM, 1);

      BattleshipGame.updateState(gamedata, BattleshipGame.STATE_PLACEMENT);
    }
    catch(IOException ex)
    {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public String getName()
  {
    return Bundle.getMessage(NewGameAction.class, "NewGameAction.name");
  }

  @Override
  public Icon getIcon()
  {
    return loadIcon("icons8-ship-24(-hdpi).png");
  }

  @Override
  public Icon getMenuIcon()
  {
    return loadIcon("icons8-ship-16(-mdpi).png");
  }

  @Override
  public List<String> getPaths()
  {
    return List.of("Options", "Menu");
  }

  @Override
  public int getPosition()
  {
    return 100;
  }

}
