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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.JFrame;

import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class ActionsManager
{
  private static ActionsManager MANAGER;

  public static JFrame getFrame()
  {
    return Storage.getDefault().getOrSet(
        JFrame.class.getName(), JFrame.class, () ->
    {
      for(Object f : JFrame.getFrames())
      {
        if(f instanceof JFrame)
        {
          return (JFrame)f;
        }
      }
      return null;
    });
  }

  private final Map<String, Map<Integer, ActionBase>> actions;

  private ActionsManager()
  {
    actions = new HashMap<>();
  }

  public static ActionsManager getDefault()
  {
    if(MANAGER == null)
    {
      MANAGER = new ActionsManager();
    }
    return MANAGER;
  }

  private void ensureData()
  {
    if(actions.isEmpty())
    {
      ServiceLoader<ActionBase> actionsLoader = ServiceLoader.load(ActionBase.class);
      actionsLoader.forEach(action ->
      {
        action.getPaths().forEach(path ->
        {
          Map<Integer, ActionBase> actionsForPath =
              actions.computeIfAbsent(path, k -> new TreeMap<>());
          actionsForPath.put(action.getPosition(), action);
        });

      });
    }
  }

  public List<Action> getActions(String path)
  {
    ensureData();
    Map<Integer, ActionBase> pathActions = actions.get(path);
    if(pathActions == null)
    {
      return Collections.emptyList();
    }
    return new ArrayList(pathActions.values());
  }

  public List<Action> getContextActions(String path, Object context)
  {
    List<Action> pathActions = getActions(path);
    List<Action> contextActions = new ArrayList<>();
    for(Action pathAction : pathActions)
    {
      if(pathAction instanceof ActionBase.ContextActionProvider)
      {
        Action contextAction =
            ((ActionBase.ContextActionProvider)pathAction).createContextAction(context);
        contextActions.add(contextAction);
      }
      else
      {
        contextActions.add(pathAction);
      }
    }

    if(contextActions.isEmpty())
    {
      return Collections.emptyList();
    }
    return contextActions;
  }
}
