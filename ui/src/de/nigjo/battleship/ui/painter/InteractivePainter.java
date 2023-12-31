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

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public abstract class InteractivePainter implements OceanBoardPainter
{
  private int currentPlayer;

  private BoardCellMouseListener mia;

  private boolean active;
  private Point validLocation;
  private Point lastMouseLocation;
  private Supplier<BoardData> boardSupplier;
  private Supplier<JComponent> uiSupplier;

  protected InteractivePainter()
  {
    BattleshipGame game =
        Storage.getDefault().find(BattleshipGame.class)
            .orElseThrow();
    game.addPropertyChangeListener(BattleshipGame.KEY_STATE,
        pce -> checkGameState(pce.getSource(), pce.getNewValue()));
    currentPlayer = game.getDataInt(BattleshipGame.KEY_PLAYER_NUM, 0);
  }

  private void attachListeners(JComponent context)
  {
    if(mia != null)
    {
      mia.removeListeners(context);
    }
    mia = new BoardCellMouseListener(this::updateCellLocation, this::selectCell);
    mia.registerListeners(context);
  }

  private void removeListeners(JComponent context)
  {
    if(mia != null)
    {
      mia.removeListeners(context);
      mia = null;
    }
  }

//  public Optional<BoardData> getBoarddata()
//  {
//    if(boarddata == null)
//    {
//      return Optional.empty();
//    }
//    return Optional.ofNullable(boarddata.get());
//  }
  protected Point updateCellLocation(OceanBoard.Data data)
  {
    return updateCellLocation(data, (x, y) -> true);
  }

  protected Point updateCellLocation(OceanBoard.Data data,
      BiPredicate<Integer, Integer> validateCoordiantes)
  {
    int cellSize = data.getCellSize();
    int offX = data.getOffsetX() + cellSize;
    int offY = data.getOffsetY() + cellSize;

    validLocation = null;
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
      }
    }

    return validLocation;
  }

  public Point getSelectedCell()
  {
    return validLocation;
  }

  protected Optional<BoardData> getBoard()
  {
    return get(boardSupplier);
  }

  protected void withGame(Consumer<BattleshipGame> worker)
  {
    Storage.getDefault().find(BattleshipGame.class)
        .ifPresent(worker);
  }

  protected void withBoard(Consumer<BoardData> worker)
  {
    get(boardSupplier)
        .ifPresent(worker);
  }

  protected void withComponent(Consumer<JComponent> worker)
  {
    get(uiSupplier)
        .ifPresent(worker);
  }

  protected final void repaint()
  {
    withComponent(JComponent::repaint);
  }

  protected void updateCellLocation(MouseEvent e)
  {
    if(e == null || get(boardSupplier).isEmpty())
    {
      lastMouseLocation = null;
    }
    else
    {
      lastMouseLocation = e.getPoint();
    }
    repaint();
  }

  private <T> Optional<T> get(Supplier<T> s)
  {
    return Optional.ofNullable(s)
        .map(Supplier::get);
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
        get(uiSupplier).ifPresent(this::attachListeners);
      }
      else
      {
        get(uiSupplier).ifPresent(this::removeListeners);
      }
    }
  }

  public int getCurrentPlayer()
  {
    return currentPlayer;
  }

  protected abstract boolean checkActive(Storage boarddata, String state);

  @Override
  public final void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
    this.boardSupplier = data::getBoard;
    this.uiSupplier = data::getContext;

    if(!isActive())
    {
      return;
    }

    updateCellLocation(data);

    paint(g, data);
  }

  protected abstract void paint(Graphics2D g, OceanBoard.Data data);

  protected final void setActive(boolean active)
  {
    this.active = active;
  }

  protected final boolean isActive()
  {
    return active;
  }

}
