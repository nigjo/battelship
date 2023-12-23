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
import java.awt.geom.Rectangle2D;

import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;

/**
 *
 * @author nigjo
 */
public class Koordinates implements OceanBoardPainter
{
  @Override
  public int getPosition()
  {
    return 200;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
    int cellSize = data.getCellSize();
    int offX = data.getOffsetX();
    int offY = data.getOffsetY();

    g.setColor(Color.BLACK);
    int boardSize = data.getBoard().getSize();
    for(int i = 0; i < boardSize; i++)
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

}
