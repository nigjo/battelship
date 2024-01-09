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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
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
  private final List<OceanBoardPainter> painters;
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

    Map<Integer, OceanBoardPainter> sorter = new TreeMap<>();
    ServiceLoader<OceanBoardPainter> services =
        ServiceLoader.load(OceanBoardPainter.class);
    services.forEach(s -> sorter.put(s.getPosition(), s));
    painters = new ArrayList<>(sorter.values());
  }

  public final class Data
  {
    private final int width;
    private final int height;
    private final int offX;
    private final int offY;
    private final int cellSize;

    private Data(int width, int height)
    {
      this.width = width;
      this.height = height;

      cellSize = calcGridSize();
      int borderWidth = cellSize / 2;
      offX = calcGridStartX() + borderWidth;
      offY = calcGridStartY() + borderWidth;
    }

    public BoardData getBoard()
    {
      return data;
    }

    public JComponent getContext()
    {
      return OceanBoard.this;
    }

    public int getHeight()
    {
      return height;
    }

    public int getWidth()
    {
      return width;
    }

    public int getOffsetX()
    {
      return offX;
    }

    public int getOffsetY()
    {
      return offY;
    }

    private int calcGridStartX()
    {
      if(width <= height)
      {
        return 0;
      }
      return (width - height) / 2;
    }

    private int calcGridStartY()
    {
      if(height <= width)
      {
        return 0;
      }
      return (height - width) / 2;
    }

    public int getCellSize()
    {
      return cellSize;
    }

    private int calcGridSize()
    {
      int maxSize = Math.min(width, height);
      return maxSize / (data.getSize() + 2);
    }
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
        Data painterData = new Data(size.width, size.height);
        for(Painter<Data> painter : painters)
        {
          if(painter != null)
          {
            painter.paint(work, painterData, painterData.width, painterData.height);
          }
        }
      }
      finally
      {
        work.dispose();
      }
    }
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
