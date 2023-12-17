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

import java.util.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.Painter;

import de.nigjo.battleship.data.BoardData;

/**
 *
 * @author nigjo
 */
public class OceanBoard extends JPanel
{
  private List<Painter<BoardData>> painters;
  private final BoardData data;

  /**
   *
   * @param size Größe des Spielbretts.
   */
  public OceanBoard(BoardData data)
  {
    this.data = data;
    int dim = (data.getSize() + 3) * 32;
    setPreferredSize(new Dimension(dim, dim));

    painters = List.of(OceanBoard::paintDebugCross,
        OceanBoard::paintGridBoard,
        OceanBoard::paintKoords,
        OceanBoard::paintShips,
        OceanBoard::paintShoots
    );
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if(g instanceof Graphics2D)
    {
      Graphics2D work = (Graphics2D)g.create();
      try
      {
        Dimension size = getSize();
        for(Painter<BoardData> painter : painters)
        {
          painter.paint(work, data, size.width, size.height);
        }
      }
      finally
      {
        work.dispose();
      }
    }
  }

  private static int getGridStartX(int width, int height)
  {
    if(width <= height)
    {
      return 0;
    }
    return (width - height) / 2;
  }

  private static int getGridStartY(int width, int height)
  {
    if(height <= width)
    {
      return 0;
    }
    return (height - width) / 2;
  }

  private static int getGridSize(BoardData data, int width, int height)
  {
    int maxSize = Math.min(width, height);
    return maxSize / (data.getSize() + 3);
  }

  private static void paintGridBoard(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int offX = getGridStartX(width, height) + cellSize;
    int offY = getGridStartY(width, height) + cellSize;
    int cells = data.getSize() + 1;
    int boardSize = cellSize * (cells);

    g.setColor(Color.WHITE);
    g.fillRect(offX, offY, boardSize, boardSize);
    g.setColor(Color.BLUE);
    g.fillRect(offX + cellSize, offY + cellSize, boardSize - cellSize, boardSize
        - cellSize);
    g.setColor(Color.BLACK);
    g.drawRect(offX, offY, boardSize, boardSize);

    for(int i = 1; i < cells; i++)
    {
      g.drawLine(offX + i * cellSize, offY, offX + i * cellSize, offY + boardSize);
      g.drawLine(offX, offY + i * cellSize, offX + boardSize, offY + i * cellSize);
    }
  }

  private static void paintKoords(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int offX = getGridStartX(width, height) + cellSize;
    int offY = getGridStartY(width, height) + cellSize;

    for(int i = 0; i < data.getSize(); i++)
    {
      int x = offX + (i + 1) * cellSize;
      String mark = Character.toString('A' + i);
      Rectangle2D markSize = g.getFontMetrics().getStringBounds(mark, g);
      g.drawString(mark,
          x + (int)((cellSize - markSize.getWidth()) / 2),
          offY + cellSize - (int)((cellSize - markSize.getHeight()) / 2));

      int y = offY + (i + 1) * cellSize;
      mark = Integer.toString(i + 1);
      markSize = g.getFontMetrics().getStringBounds(mark, g);
      g.drawString(mark,
          offX + (int)((cellSize - markSize.getWidth()) / 2),
          y + cellSize - (int)((cellSize - markSize.getHeight()) / 2));
    }
  }

  private static void paintShips(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int shipSize = (int)(cellSize * .8f);
    int shipOff = (int)((cellSize - shipSize) / 2f + .5f);
    int offX = getGridStartX(width, height) + 2 * cellSize + shipOff;
    int offY = getGridStartY(width, height) + 2 * cellSize + shipOff;

    g.setColor(Color.LIGHT_GRAY);
    int size = data.getSize();
    for(int y = 0; y < size; y++)
    {
      for(int x = 0; x < size; x++)
      {
        int state = data.stateAt(x, y);
        if((state & BoardData.SHIP) == BoardData.SHIP)
        {
          if((state & BoardData.SHIP_NORTH) == BoardData.SHIP_NORTH)
          {
            g.fillOval(offX + x * cellSize, offY + y * cellSize, shipSize, shipSize);
            g.fillRect(offX + x * cellSize, offY + y * cellSize + shipSize / 2,
                shipSize, shipSize / 2 + 1);
          }
          else if((state & BoardData.SHIP_SOUTH) == BoardData.SHIP_SOUTH)
          {
            g.fillOval(offX + x * cellSize, offY + y * cellSize, shipSize, shipSize);
            g.fillRect(offX + x * cellSize, offY + y * cellSize,
                shipSize, shipSize / 2 + 1);
          }
          else if((state & BoardData.SHIP_EAST) == BoardData.SHIP_EAST)
          {
            g.fillOval(offX + x * cellSize, offY + y * cellSize, shipSize, shipSize);
            g.fillRect(offX + x * cellSize, offY + y * cellSize,
                shipSize / 2 + 1, shipSize);
          }
          else if((state & BoardData.SHIP_WEST) == BoardData.SHIP_WEST)
          {
            g.fillOval(offX + x * cellSize, offY + y * cellSize, shipSize, shipSize);
            g.fillRect(offX + x * cellSize + shipSize / 2, offY + y * cellSize,
                shipSize / 2 + 1, shipSize);
          }
          else
          {
            g.fillRect(offX + x * cellSize, offY + y * cellSize, shipSize, shipSize);
          }
        }
      }
    }
  }

  private static void paintShoots(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int offX = getGridStartX(width, height) + cellSize;
    int offY = getGridStartY(width, height) + cellSize;
    int size = data.getSize();
    for(int y = 0; y < size; y++)
    {
      for(int x = 0; x < size; x++)
      {
        int state = data.stateAt(x, y);

      }
    }
  }

  private static void paintDebugCross(Graphics2D g, BoardData data, int width, int height)
  {
    g.setColor(Color.BLACK);
    g.drawLine(0, 0, width, height);
    g.drawLine(0, height, width, 0);
  }

}
