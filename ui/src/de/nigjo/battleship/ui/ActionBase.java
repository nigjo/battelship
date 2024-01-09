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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author nigjo
 */
public abstract class ActionBase extends AbstractAction
{
  public ActionBase()
  {
    initAction();
  }

  private void initAction()
  {
    putValue(NAME, getName());
    putValue(LARGE_ICON_KEY, getIcon());
    putValue(SMALL_ICON, getMenuIcon());
  }

  public abstract String getName();

  public abstract List<String> getPaths();

  public abstract int getPosition();

  public Icon getIcon()
  {
    return null;
  }

  public Icon getMenuIcon()
  {
    return null;
  }

  protected Icon loadIcon(String resname)
  {
    return new ImageIcon(getClass().getResource(resname));
  }

  public static interface ContextActionProvider
  {
    public Action createContextAction(Object context);
  }
}
