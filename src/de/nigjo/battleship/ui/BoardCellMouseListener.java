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

import java.util.function.Consumer;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author nigjo
 */
public class BoardCellMouseListener extends MouseInputAdapter
{
  private boolean inside = false;
  private final Consumer<MouseEvent> updateLocation;
  private final Consumer<MouseEvent> locationSelected;

  public BoardCellMouseListener(
      Consumer<MouseEvent> updateLocation,
      Consumer<MouseEvent> locationSelected)
  {
    this.updateLocation = updateLocation;
    this.locationSelected = locationSelected;
  }

  public void registerListeners(JComponent parent)
  {
    if(parent != null)
    {
      parent.addMouseListener(this);
      parent.addMouseMotionListener(this);
    }
  }

  public void removeListeners(JComponent parent)
  {
    if(parent != null)
    {
      parent.removeMouseListener(this);
      parent.removeMouseMotionListener(this);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
    if(locationSelected != null)
    {
      locationSelected.accept(e);
    }
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    inside = false;
    if(updateLocation != null)
    {
      updateLocation.accept(null);
    }
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    inside = true;
    if(updateLocation != null)
    {
      updateLocation.accept(e);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
    if(inside && updateLocation != null)
    {
      updateLocation.accept(e);
    }
  }

}
