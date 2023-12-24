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

import java.io.IOException;
import java.io.UncheckedIOException;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;

/**
 *
 * @author nigjo
 */
public class AttackResults implements OceanBoardPainter
{
  private final BufferedImage hit;
  private BufferedImage miss;

  public AttackResults()
  {
    try
    {
      hit = ImageIO
          .read(AttackResults.class.getResource("icons8-explosion-24(-hdpi).png"));
      miss = ImageIO.read(AttackResults.class.getResource("icons8-water-24(-hdpi).png"));
    }
    catch(IOException ex)
    {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public int getPosition()
  {
    return 10000;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
    int cellSize = data.getCellSize();
    int offX = data.getOffsetX() + cellSize;
    int offY = data.getOffsetY() + cellSize;
    BoardData board = data.getBoard();

    Graphics work = g.create();
    work.setColor(Color.RED);

    int size = board.getSize();
    for(int y = 0; y < size; y++)
    {
      for(int x = 0; x < size; x++)
      {
        int state = board.stateAt(x, y);
        if((state & BoardData.SHOOTED_AT) == BoardData.SHOOTED_AT)
        {
          int cX = offX + x * cellSize;
          int cY = offY + y * cellSize;

          Image icon;
          if(0 != (state & BoardData.SHIP))
          {
            icon = hit;
          }
          else
          {
            icon = miss;
          }
          if(icon != null && cellSize > 12)
          {
            int icoWidth = icon.getWidth(null);
            if(cellSize < icoWidth + 4)
            {
              icoWidth = cellSize - 4;
              icon = icon
                  .getScaledInstance(icoWidth, icoWidth, BufferedImage.SCALE_SMOOTH);
            }
            else if(cellSize * .75 > icoWidth)
            {
              icoWidth = (int)(cellSize * .75);
              icon = icon
                  .getScaledInstance(icoWidth, icoWidth, BufferedImage.SCALE_SMOOTH);
            }

            int border = (cellSize - icoWidth) / 2;
            work.drawImage(icon, cX + border, cY + border, null);
          }
          else
          {
            work.drawLine(cX, cY, cX + cellSize, cY + cellSize);
            work.drawLine(cX, cY + cellSize, cX + cellSize, cY);
          }
        }
      }
    }

    work.dispose();
  }

}
