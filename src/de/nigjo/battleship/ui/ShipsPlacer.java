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
package de.nigjo.battleship.ui;

import java.util.Arrays;
import java.util.function.Supplier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.UIManager;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.data.Savegame;
import static de.nigjo.battleship.ui.OceanBoard.getGridSize;
import static de.nigjo.battleship.ui.OceanBoard.getGridStartX;
import static de.nigjo.battleship.ui.OceanBoard.getGridStartY;
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

  public ShipsPlacer(JComponent context, Supplier<BoardData> boarddata)
  {
    super(context, boarddata);
  }

  @Override
  protected boolean checkActive(Storage gamedata, String state)
  {
    if(BattleshipGame.STATE_PLACEMENT.equals(state))
    {
      boolean opponent = getBoarddata()
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
      BoardData board = getBoarddata().orElseThrow();
      board.placeShip(currentShip, validLocation.x, validLocation.y,
          ships[currentShip], vertical);
      ++currentShip;

      if(currentShip >= ships.length)
      {
        setActive(false);
        Storage.getDefault().find(BattleshipGame.class)
            .ifPresent(g ->
            {
              KeyManager km = g.getData(KeyManager.KEY_MANAGER_SELF,
                  KeyManager.class);
              String playload = km.encode(board.toString());
              g.getData(Savegame.class)
                  .addRecord(Savegame.Record.BOARD, getCurrentPlayer(), playload);
              g.putData(BattleshipGame.KEY_STATE, BattleshipGame.STATE_WAIT_START);
            });
      }

      Object source = e.getSource();
      if(source instanceof JComponent)
      {
        ((JComponent)source).repaint();
      }
    }
    catch(IllegalArgumentException ex)
    {
      StatusLine.getDefault().setText(ex.getLocalizedMessage());
    }
  }

  @Override
  protected void updateCellLocation(MouseEvent e)
  {
    super.updateCellLocation(e);
    vertical = MouseEvent.SHIFT_DOWN_MASK == (e.getModifiersEx()
        & MouseEvent.SHIFT_DOWN_MASK);
  }

  @Override
  public void paint(Graphics2D g, BoardData data, int width, int height)
  {
    if(!isActive())
    {
      return;
    }

    Graphics2D work = (Graphics2D)g.create();
    int cellSize = getGridSize(data, width, height);
    int borderWidth = cellSize / 2;
    int offX = getGridStartX(width, height) + cellSize + borderWidth;
    int offY = getGridStartY(width, height) + cellSize + borderWidth;

    Point currentCell = updateCellLocation(data, width, height,
        (x, y) -> (vertical ? y : x) + ships[currentShip] <= data.getSize());
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
    work.setColor(UIManager.getColor("textText"));
    work.drawString(msg, offX, offY + cellSize * (data.getSize()) + cellSize * 2 / 5);

    work.dispose();
  }

}
