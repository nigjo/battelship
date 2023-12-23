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

import java.awt.Graphics2D;

import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;

/**
 *
 * @author nigjo
 */
public class AttackResults implements OceanBoardPainter
{
  @Override
  public int getPosition()
  {
    return 10000;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
//    int cellSize = getGridSize(data, width, height);
//    int borderWidth = cellSize / 2;
//    int offX = getGridStartX(width, height) + borderWidth;
//    int offY = getGridStartY(width, height) + borderWidth;
//    int size = data.getSize();
//    for(int y = 0; y < size; y++)
//    {
//      for(int x = 0; x < size; x++)
//      {
//        int state = data.stateAt(x, y);
//
//      }
//    }
  }

}
