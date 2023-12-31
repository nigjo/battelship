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
package de.nigjo.battleship.ui.painter;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.api.StatusDisplayer;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.data.Savegame;
import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class AttackSelection extends InteractivePainter
{
  public AttackSelection()
  {
    super();
  }

  @Override
  public int getPosition()
  {
    return 5100;
  }

  @Override
  protected void selectCell(MouseEvent e)
  {
    Point selectedCell = getSelectedCell();
    String message = "Attacke auf "
        + Character.toString('A' + selectedCell.x) + (selectedCell.y + 1);
    StatusDisplayer.getDefault().setText(message);
    withBoard(board ->
    {
      board.shootAt(selectedCell.x, selectedCell.y);
      withGame(game ->
      {
        KeyManager km = game.getData(
            KeyManager.KEY_MANAGER_OPPONENT, KeyManager.class);
        String payload = km.encode(selectedCell.x + "," + selectedCell.y);
        //Immer mit der Spielernummer markieren, die den Record lesen kann
        game.getData(Savegame.class)
            .addRecord(Savegame.Record.MESSAGE, getCurrentPlayer(), message);
        Logger.getLogger(BattleshipGame.class.getName())
            .log(Level.INFO, "{0}", message);
        game.getData(Savegame.class)
            .addRecord(Savegame.Record.ATTACK, 3 - getCurrentPlayer(), payload);
        game.updateState(BattleshipGame.STATE_WAIT_RESPONSE);
      });
    });
    repaint();
  }

  @Override
  protected boolean checkActive(Storage gamedata, String state)
  {
    boolean opponent = getBoard()
        .map(BoardData::isOpponent)
        .orElse(true);
    boolean active = opponent && BattleshipGame.STATE_ATTACK.equals(state);
    if(active)
    {
      Logger.getLogger(AttackSelection.class.getName()).log(Level.FINE, "activated");
    }
    return active;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data)
  {
    Point cell = getSelectedCell();
    if(cell != null)
    {
      int cellSize = data.getCellSize();
      int offX = data.getOffsetX() + cellSize;
      int offY = data.getOffsetY() + cellSize;

      Graphics2D work = (Graphics2D)g.create();

      work.setColor(new Color(255, 224, 192, 64));
      work.fillRect(
          offX + (cell.x) * cellSize,
          offY + (cell.y) * cellSize,
          cellSize, cellSize);

      work.dispose();
    }
  }

}
