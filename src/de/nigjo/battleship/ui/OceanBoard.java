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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.Painter;
import javax.swing.SwingUtilities;

import de.nigjo.battleship.data.BoardData;

/**
 *
 * @author nigjo
 */
public class OceanBoard extends JPanel
{
  private final List<Painter<BoardData>> painters;
  private BoardData data;

  /**
   *
   * @param size Größe des Spielbretts.
   */
  public OceanBoard(BoardData data)
  {
    this.data = data;
    // groesse + Koordinate + Rand
    int dim = (data.getSize() + 1 + 1) * 32;
    setPreferredSize(new Dimension(dim, dim));
    Painter<BoardData> empty = (g, d, w, h) ->
    {
    };

    painters = List.of(OceanBoard::paintDebugCross,
        OceanBoard::paintGridBoard,
        OceanBoard::paintKoords,
        OceanBoard::paintShips,
        new ShipsPlacer(this, () -> this.data),
        OceanBoard::paintShoots
    );
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if(g instanceof Graphics2D && data != null)
    {
      Graphics2D work = (Graphics2D)g.create();
      try
      {
        Dimension size = getSize();
        for(Painter<BoardData> painter : painters)
        {
          if(painter != null)
          {
            painter.paint(work, data, size.width, size.height);
          }
        }
      }
      finally
      {
        work.dispose();
      }
    }
  }

  public static int getGridStartX(int width, int height)
  {
    if(width <= height)
    {
      return 0;
    }
    return (width - height) / 2;
  }

  public static int getGridStartY(int width, int height)
  {
    if(height <= width)
    {
      return 0;
    }
    return (height - width) / 2;
  }

  public static int getGridSize(BoardData data, int width, int height)
  {
    int maxSize = Math.min(width, height);
    return maxSize / (data.getSize() + 2);
  }

  private static void paintGridBoard(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int borderWidth = cellSize / 2;
    int offX = getGridStartX(width, height) + borderWidth;
    int offY = getGridStartY(width, height) + borderWidth;
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
      g.setColor(Color.BLACK);
      g.drawLine(offX + i * cellSize, offY, offX + i * cellSize, offY + boardSize);
      g.drawLine(offX, offY + i * cellSize, offX + boardSize, offY + i * cellSize);
      if(i > 1)
      {
        g.setColor(Color.CYAN);
        g.drawLine(offX + i * cellSize, offY + cellSize, offX + i * cellSize, offY
            + boardSize);
        g.drawLine(offX + cellSize, offY + i * cellSize, offX + boardSize, offY + i
            * cellSize);
      }
    }
  }

  private static void paintKoords(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int borderWidth = cellSize / 2;
    int offX = getGridStartX(width, height) + borderWidth;
    int offY = getGridStartY(width, height) + borderWidth;

    g.setColor(Color.BLACK);
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
    int borderWidth = cellSize / 2;
    int shipSize = (int)(cellSize * .8f);
    int shipOff = (int)((cellSize - shipSize) / 2f + .5f);
    int offX = getGridStartX(width, height) + cellSize + borderWidth;
    int offY = getGridStartY(width, height) + cellSize + borderWidth;

    Area shipSouth = new Area();
    shipSouth.add(new Area(
        new Ellipse2D.Float(shipOff, shipOff, shipSize, shipSize)));
    shipSouth.add(new Area(
        new Rectangle2D.Float(shipOff, shipOff, shipSize, shipSize / 2)));
    shipSouth.transform(AffineTransform.getTranslateInstance(cellSize / -2., cellSize
        / -2.));

    Area shipNorth = shipSouth.createTransformedArea(
        AffineTransform.getRotateInstance(Math.toRadians(180.)));
    Area shipEast = shipSouth.createTransformedArea(
        AffineTransform.getRotateInstance(Math.toRadians(-90.)));
    Area shipWest = shipSouth.createTransformedArea(
        AffineTransform.getRotateInstance(Math.toRadians(90.)));

    g.setColor(Color.LIGHT_GRAY);
    int size = data.getSize();
    for(int y = 0; y < size; y++)
    {
      for(int x = 0; x < size; x++)
      {
        int state = data.stateAt(x, y);
        if((state & BoardData.SHIP) > 0)
        {
          Graphics2D cell = (Graphics2D)g.create();
          cell.translate(offX + x * cellSize + cellSize / 2., offY + y * cellSize
              + cellSize / 2.);
          if((state & BoardData.SHIP_MID_V) == BoardData.SHIP_NORTH)
          {
            cell.fill(shipNorth);
          }
          else if((state & BoardData.SHIP_MID_V) == BoardData.SHIP_SOUTH)
          {
            cell.fill(shipSouth);
          }
          else if((state & BoardData.SHIP_MID_H) == BoardData.SHIP_EAST)
          {
            cell.fill(shipEast);
          }
          else if((state & BoardData.SHIP_MID_H) == BoardData.SHIP_WEST)
          {
            cell.fill(shipWest);
          }
          else
          {
            cell.fillRect(shipOff - cellSize / 2, shipOff - cellSize / 2, shipSize,
                shipSize);
          }
          cell.dispose();
        }
      }
    }
  }

  private static void paintShoots(Graphics2D g, BoardData data, int width, int height)
  {
    int cellSize = getGridSize(data, width, height);
    int borderWidth = cellSize / 2;
    int offX = getGridStartX(width, height) + borderWidth;
    int offY = getGridStartY(width, height) + borderWidth;
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
    g.setColor(new Color(0, 0, 0, 16));
    g.drawLine(0, 0, width, height);
    g.drawLine(0, height, width, 0);
  }

  void updateBoard(BoardData boardData)
  {
    if(data != null && boardData != null)
    {
      if(data.getSize() != boardData.getSize())
      {
        throw new IllegalArgumentException(
            "expected size " + data.getSize() + ", but got " + boardData.getSize());
      }
    }
    this.data = boardData;
    SwingUtilities.invokeLater(this::repaint);
  }

}
