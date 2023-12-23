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

import java.awt.Color;
import java.awt.Graphics2D;

import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;

/**
 *
 * @author nigjo
 */
public class GridBoard implements OceanBoardPainter
{
  @Override
  public int getPosition()
  {
    return 100;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
    int cellSize = data.getCellSize();
    int offX = data.getOffsetX();
    int offY = data.getOffsetY();
    int cells = data.getBoard().getSize() + 1;
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

}
