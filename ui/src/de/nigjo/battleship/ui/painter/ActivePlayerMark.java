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

import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.ui.OceanBoard;
import de.nigjo.battleship.ui.OceanBoardPainter;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class ActivePlayerMark implements OceanBoardPainter
{
  private String currentActivePlayer;

  public ActivePlayerMark()
  {
    Storage.getDefault().get(BattleshipGame.class)
        .addPropertyChangeListener(BattleshipGame.KEY_PLAYER,
            pce ->
        {
          currentActivePlayer = (String)pce.getNewValue();
          Logger.getLogger(ActivePlayerMark.class.getName()).log(Level.FINE,
              "active player is ''{0}''.", currentActivePlayer);
        });
  }

  @Override
  public int getPosition()
  {
    return 90;
  }

  @Override
  public void paint(Graphics2D g, OceanBoard.Data data, int width, int height)
  {
    boolean active =
        data.getContext().getName().equals(currentActivePlayer);

    Graphics work = g.create();
    work.setColor(Color.LIGHT_GRAY);
    work.drawString(data.getContext().getName() + "/" + currentActivePlayer, 2, 20);

    if(active)
    {
      work.setColor(new Color(64, 128, 224, 64));
      int cellSize = data.getCellSize();
      int offsetX = data.getOffsetX();
      int offsetY = data.getOffsetY();
      int boardwidth = (data.getBoard().getSize() + 1) * cellSize;
      ((Graphics2D)work).setStroke(new BasicStroke(16f));
      work.drawRect(offsetX, offsetY, boardwidth, boardwidth);
    }

    work.dispose();
  }

}
