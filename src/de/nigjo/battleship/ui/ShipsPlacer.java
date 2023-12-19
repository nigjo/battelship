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
import javax.swing.Painter;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

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
public class ShipsPlacer implements Painter<BoardData>
{
  private final JComponent context;
  private final Supplier<BoardData> boarddata;
  private boolean active;
  private Point validLocation;
  private Point lastMouseLocation;
  private boolean vertical;
  private int currentShip;
  private int[] ships;

  public ShipsPlacer(JComponent context, Supplier<BoardData> boarddata)
  {
    this.context = context;
    this.boarddata = boarddata;
    if(this.boarddata.get().isOpponent())
    {
      return;
    }
    Storage gamedata =
        Storage.getDefault().find(BattleshipGame.class)
            .map(BattleshipGame::getGamedata)
            .orElseThrow();
    gamedata.addPropertyChangeListener("gameState",
        pce -> checkGameState(pce.getSource(), pce.getNewValue()));
    MouseInputAdapter mia = new MouseInputAdapter()
    {
      boolean inside = false;

      @Override
      public void mouseClicked(MouseEvent e)
      {
        if(active && validLocation != null)
        {
          setShipLocation();
        }
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        inside = false;
        updateShipLocation(null);
      }

      @Override
      public void mouseEntered(MouseEvent e)
      {
        inside = true;
      }

      @Override
      public void mouseMoved(MouseEvent e)
      {
        if(active && inside)
        {
          updateShipLocation(e);
        }
      }

    };
    context.addMouseListener(mia);
    context.addMouseMotionListener(mia);
  }

  private void checkGameState(Object source, Object state)
  {
    if(source instanceof Storage)
    {
      Savegame savegame = ((Storage)source).get(Savegame.class);
      active = "new".equals(state);
      ships = savegame.records(1, Savegame.Record.SHIPS)
          .findFirst().stream()
          .flatMap(r -> r.getPayload().stream())
          .flatMap(s -> Arrays.stream(s.split(",")))
          .mapToInt(Integer::parseInt)
          .toArray();
      currentShip = 0;
    }
  }

  private void setShipLocation()
  {
    if(validLocation != null)
    {
      BoardData board = boarddata.get();
      try
      {
        board.placeShip(currentShip, validLocation.x, validLocation.y,
            ships[currentShip], vertical);
        ++currentShip;

        if(currentShip >= ships.length)
        {
          active = false;
          Storage.getDefault().find(BattleshipGame.class)
              .map(BattleshipGame::getGamedata)
              .ifPresent(s ->
              {
                KeyManager km = s.get(KeyManager.KEY_MANAGER_SELF, KeyManager.class);
                String playload = km.encode(board.toString());
                s.get(Savegame.class)
                    .addRecord(new Savegame.Record(Savegame.Record.BOARD, 1, playload));
                s.put("gameState", "wait");
              });
        }

        context.repaint();
      }
      catch(IllegalArgumentException ex)
      {
        StatusLine.getDefault().setText(ex.getLocalizedMessage());
      }
    }
  }

  private void updateShipLocation(MouseEvent e)
  {
    BoardData data = boarddata.get();
    if(e == null || data == null || data.isOpponent())
    {
      lastMouseLocation = null;
    }
    else
    {
      lastMouseLocation = e.getPoint();
      vertical = MouseEvent.SHIFT_DOWN_MASK == (e.getModifiersEx()
          & MouseEvent.SHIFT_DOWN_MASK);
    }
    context.repaint();
  }

  @Override
  public void paint(Graphics2D g, BoardData data, int width, int height)
  {
    if(!active)
    {
      return;
    }

    Graphics2D work = (Graphics2D)g.create();
    int cellSize = getGridSize(data, width, height);
    int borderWidth = cellSize / 2;
    int offX = getGridStartX(width, height) + cellSize + borderWidth;
    int offY = getGridStartY(width, height) + cellSize + borderWidth;

    if(lastMouseLocation != null)
    {
      if(lastMouseLocation.x > offX && lastMouseLocation.y > offY)
      {
        int cellX = (lastMouseLocation.x - offX) / cellSize;
        int cellY = (lastMouseLocation.y - offY) / cellSize;

        boolean valid;
        if(vertical)
        {
          valid = cellY + ships[currentShip] <= data.getSize();
        }
        else
        {
          valid = cellX + ships[currentShip] <= data.getSize();
        }

        if(valid)
        {
          validLocation = new Point(cellX, cellY);

          work.setColor(new Color(192, 224, 255, 64));
          for(int i = 0; i < ships[currentShip]; i++)
          {
            work.fillRect(
                offX + (cellX + (vertical ? 0 : i)) * cellSize,
                offY + (cellY + (vertical ? i : 0)) * cellSize,
                cellSize, cellSize);
          }
        }
        else
        {
          validLocation = null;
        }
      }

    }
    String msg = "Vertikale Positionierung mit Shift";
    work.setColor(UIManager.getColor("textText"));
    work.drawString(msg, offX, offY + cellSize * (data.getSize()) + cellSize * 2 / 5);

    work.dispose();
  }

}
