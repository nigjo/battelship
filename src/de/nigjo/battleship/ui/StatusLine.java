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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class StatusLine extends JPanel
{
  private static final long SECONDS_TO_FADE = 5l;
  private static final String NBSP = "\u00A0";

  private final JLabel text;
  private final ScheduledExecutorService updater;
  private final AtomicLong lastUpdate;

  public StatusLine()
  {
    super(new FlowLayout(FlowLayout.LEADING, 8, 4));
    text = new JLabel(NBSP);
    add(text);
    lastUpdate = new AtomicLong(System.currentTimeMillis());
    updater = Executors.newSingleThreadScheduledExecutor(r ->
    {
      var t = new Thread(r, "statusupdaterer");
      t.setDaemon(true);
      return t;
    });
  }

  public static StatusLine getDefault()
  {
    return Storage.getDefault()
        .getOrSet(StatusLine.class.getName(), StatusLine.class, StatusLine::new);
  }

  public void setText(String message)
  {
    text.setText((message == null || message.isBlank()) ? NBSP : message);
    lastUpdate.set(System.currentTimeMillis());
    updater.schedule(() ->
    {
      long delta = System.currentTimeMillis() - lastUpdate.get();
      if(SECONDS_TO_FADE * 999l < delta)
      {
        SwingUtilities.invokeLater(() -> text.setText(NBSP));
      }
    }, SECONDS_TO_FADE, TimeUnit.SECONDS);
  }
}
