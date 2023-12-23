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

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.Painter;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public abstract class InteractivePainter implements Painter<BoardData>
{
  private final JComponent context;
  private final Supplier<BoardData> boarddata;
  private int currentPlayer;

  private BoardCellMouseListener mia;

  private boolean active;
  private Point validLocation;
  private Point lastMouseLocation;

  protected InteractivePainter(JComponent context, Supplier<BoardData> boarddata)
  {
    this.context = context;
    this.boarddata = boarddata;

    BattleshipGame game =
        Storage.getDefault().find(BattleshipGame.class)
            .orElseThrow();
    game.addPropertyChangeListener(BattleshipGame.KEY_STATE,
        pce -> checkGameState(pce.getSource(), pce.getNewValue()));
    currentPlayer = game.getDataInt(BattleshipGame.KEY_PLAYER_NUM, 0);
  }

  private void attachListener()
  {
    if(mia != null)
    {
      mia.removeListeners(context);
    }
    mia = new BoardCellMouseListener(this::updateCellLocation, this::selectCell);
    mia.registerListeners(context);
  }

  private void removeListeners()
  {
    if(mia != null)
    {
      mia.removeListeners(context);
      mia = null;
    }
  }

  public Optional<BoardData> getBoarddata()
  {
    if(boarddata == null)
    {
      return Optional.empty();
    }
    return Optional.ofNullable(boarddata.get());
  }

  protected Point updateCellLocation(BoardData data, int width, int height)
  {
    return updateCellLocation(data, width, height, (x, y) -> true);
  }

  protected Point updateCellLocation(BoardData data, int width, int height,
      BiPredicate<Integer, Integer> validateCoordiantes)
  {
    int cellSize = OceanBoard.getGridSize(data, width, height);
    int borderWidth = cellSize / 2;
    int offX = OceanBoard.getGridStartX(width, height) + cellSize + borderWidth;
    int offY = OceanBoard.getGridStartY(width, height) + cellSize + borderWidth;

    if(lastMouseLocation != null)
    {
      if(lastMouseLocation.x > offX && lastMouseLocation.y > offY)
      {
        int cellX = (lastMouseLocation.x - offX) / cellSize;
        int cellY = (lastMouseLocation.y - offY) / cellSize;

        if(validateCoordiantes.test(cellX, cellY))
        {
          validLocation = new Point(cellX, cellY);
        }
        else
        {
          validLocation = null;
        }
      }
    }

    return validLocation;
  }

  public Point getSelectedCell()
  {
    return validLocation;
  }

  protected void updateCellLocation(MouseEvent e)
  {
    BoardData data = boarddata.get();
    if(e == null || data == null)
    {
      lastMouseLocation = null;
    }
    else
    {
      lastMouseLocation = e.getPoint();
    }
    context.repaint();
  }

  abstract protected void selectCell(MouseEvent e);

  private void checkGameState(Object source, Object state)
  {
    if(source instanceof Storage && state instanceof String)
    {
      currentPlayer = ((Storage)source).getInt(BattleshipGame.KEY_PLAYER_NUM, 0);

      active = checkActive((Storage)source, (String)state);
      if(active)
      {
        attachListener();
      }
      else
      {
        removeListeners();
      }
    }
  }

  public int getCurrentPlayer()
  {
    return currentPlayer;
  }

  protected abstract boolean checkActive(Storage boarddata, String state);

  protected final void setActive(boolean active)
  {
    this.active = active;
  }

  protected final boolean isActive()
  {
    return active;
  }

}
