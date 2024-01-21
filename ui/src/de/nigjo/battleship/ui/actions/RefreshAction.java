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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.api.StatusDisplayer;
import de.nigjo.battleship.data.Savegame;
import de.nigjo.battleship.ui.ActionBase;
import de.nigjo.battleship.util.Bundle;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class RefreshAction extends ActionBase
{
  @Override
  public String getName()
  {
    return Bundle.getMessage(RefreshAction.class, "RefreshAction.name");
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    BattleshipGame game = Storage.getDefault().get(BattleshipGame.class);
    Savegame savegame = game.getData(Savegame.class);
    if(savegame == null || savegame.getFilename() == null)
    {
      StatusDisplayer.getDefault().setText("Keinen Spielstand gefunden.");
    }
    else
    {
      try
      {
        StatusDisplayer.getDefault().setText("Lade Spielstanddatei neu ein.");
        game.reload();
      }
      catch(IOException ex)
      {
        Logger.getLogger(RefreshAction.class.getName()).log(Level.SEVERE, null, ex);
        StatusDisplayer.getDefault().setText(
            ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
      }
    }
  }

  @Override
  public List<String> getPaths()
  {
    return List.of("Options");
  }

  @Override
  public int getPosition()
  {
    return 4000;
  }

  @Override
  public Icon getIcon()
  {
    return loadIcon("icons8-refresh-24(-hdpi).png");
  }

}
