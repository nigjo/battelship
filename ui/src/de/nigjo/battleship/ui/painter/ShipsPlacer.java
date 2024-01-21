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

import java.util.Arrays;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.UIManager;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.api.StatusDisplayer;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class ShipsPlacer extends InteractivePainter
{
  private boolean vertical;
  private int currentShip;
  private int[] ships;

  public ShipsPlacer()
  {
    super();
  }

  @Override
  public int getPosition()
  {
    return 5000;
  }

  @Override
  protected boolean checkActive(Storage gamedata, String state)
  {
    if(BattleshipGame.STATE_PLACEMENT.equals(state))
    {
      boolean opponent = getBoard()
          .map(BoardData::isOpponent)
          .orElse(true);

      ships = Arrays.stream(
          gamedata.get("config.ships", BattleshipGame.Config.class)
              .getValue().split(","))
          .mapToInt(Integer::parseInt).toArray();

      int currentPlayer = gamedata.getInt(BattleshipGame.KEY_PLAYER_NUM, 0);
      return !opponent && currentPlayer > 0;
    }
    return false;
  }

  @Override
  protected void selectCell(MouseEvent e)
  {
    Point validLocation = getSelectedCell();
    if(validLocation == null)
    {
      return;
    }
    try
    {
      BoardData board = getBoard().orElseThrow();
      board.placeShip(currentShip, validLocation.x, validLocation.y,
          ships[currentShip], vertical);
      ++currentShip;

      if(currentShip >= ships.length)
      {
        setActive(false);
        withGame(BattleshipGame::storeOwnBoard);
      }

      repaint();
    }
    catch(IllegalArgumentException ex)
    {
      StatusDisplayer.getDefault().setText(ex.getLocalizedMessage());
    }
  }

  @Override
  protected void updateCellLocation(MouseEvent e)
  {
    super.updateCellLocation(e);
    if(e != null)
    {
      vertical = MouseEvent.SHIFT_DOWN_MASK == (e.getModifiersEx()
          & MouseEvent.SHIFT_DOWN_MASK);
    }
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data)
  {
    int cellSize = data.getCellSize();
    int offX = data.getOffsetX() + cellSize;
    int offY = data.getOffsetY() + cellSize;

    Graphics2D work = (Graphics2D)g.create();

    Point currentCell = updateCellLocation(data,
        (x, y) -> (vertical ? y : x) + ships[currentShip] <= data.getBoard().getSize());
    if(currentCell != null)
    {
      work.setColor(new Color(192, 224, 255, 64));
      for(int i = 0; i < ships[currentShip]; i++)
      {
        work.fillRect(
            offX + (currentCell.x + (vertical ? 0 : i)) * cellSize,
            offY + (currentCell.y + (vertical ? i : 0)) * cellSize,
            cellSize, cellSize);
      }
    }

    String msg = "Vertikale Positionierung mit Shift";
    writeMessage(work, data, msg);

    work.dispose();
  }

  private void writeMessage(Graphics2D work, OceanBoard.Data data, String msg)
  {
    int cellSize = data.getCellSize();
    Graphics textOut = work.create();
    textOut.setColor(UIManager.getColor("textText"));
    textOut.drawString(msg, data.getOffsetX(), data.getOffsetY()
        + cellSize * (data.getBoard().getSize()) + cellSize * 2 / 5);
    textOut.dispose();
  }

}
