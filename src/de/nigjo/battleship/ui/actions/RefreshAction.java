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
package de.nigjo.battleship.ui.actions;

import java.util.List;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import de.nigjo.battleship.ui.ActionBase;
import de.nigjo.battleship.ui.StatusLine;
import de.nigjo.battleship.util.Bundle;

/**
 *
 * @author nigjo
 */
public class RefreshAction extends ActionBase
{
  @Override
  public String getName()
  {
    return Bundle.getMessage(RefreshAction.class, "RefreshAction.name");
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    StatusLine.getDefault().setText("Alles Neu gemacht");
  }

  @Override
  public List<String> getPaths()
  {
    return List.of("Options");
  }

  @Override
  public int getPosition()
  {
    return 4000;
  }

  @Override
  public Icon getIcon()
  {
    return loadIcon("icons8-refresh-24(-hdpi).png");
  }


}
