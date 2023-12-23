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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;

/**
 *
 * @author nigjo
 */
public class SimpleShipsPainter implements OceanBoardPainter
{
  @Override
  public int getPosition()
  {
    return 1000;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
    int cellSize = data.getCellSize();
    int offX = data.getOffsetX() + cellSize;
    int offY = data.getOffsetY() + cellSize;

    int shipSize = (int)(cellSize * .8f);
    int shipOff = (int)((cellSize - shipSize) / 2f + .5f);

    Area shipSouth = new Area();
    shipSouth.add(new Area(
        new Ellipse2D.Float(shipOff, shipOff, shipSize, shipSize)));
    shipSouth.add(new Area(
        new Rectangle2D.Float(shipOff, shipOff, shipSize, shipSize / 2)));
    shipSouth.transform(
        AffineTransform.getTranslateInstance(cellSize / -2., cellSize / -2.));

    Area shipNorth = shipSouth.createTransformedArea(
        AffineTransform.getRotateInstance(Math.toRadians(180.)));
    Area shipEast = shipSouth.createTransformedArea(
        AffineTransform.getRotateInstance(Math.toRadians(-90.)));
    Area shipWest = shipSouth.createTransformedArea(
        AffineTransform.getRotateInstance(Math.toRadians(90.)));

    g.setColor(Color.LIGHT_GRAY);
    int size = data.getBoard().getSize();
    for(int y = 0; y < size; y++)
    {
      for(int x = 0; x < size; x++)
      {
        int state = data.getBoard().stateAt(x, y);
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
            cell.fillRect(
                shipOff - cellSize / 2, shipOff - cellSize / 2,
                shipSize, shipSize);
          }
          cell.dispose();
        }
      }
    }
  }

}
