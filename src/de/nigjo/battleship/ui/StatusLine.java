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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
  private JPanel statusBlock;

  public StatusLine()
  {
    super(new BorderLayout(8, 4));
    setBorder(BorderFactory
        .createMatteBorder(1, 0, 0, 0, UIManager.getColor("controlShadow")));
    text = new JLabel(NBSP);
    add(text, BorderLayout.CENTER);
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
    Logger.getLogger(StatusLine.class.getName()).log(Level.INFO, "{0}", message);
    lastUpdate.set(System.currentTimeMillis());
    updater.schedule(() ->
    {
      SwingUtilities.invokeLater(() ->
      {
        long delta = System.currentTimeMillis() - lastUpdate.get();
        if(SECONDS_TO_FADE * 999l < delta)
        {
          text.setText(NBSP);
          lastUpdate.set(0l);
        }
      });
    }, SECONDS_TO_FADE, TimeUnit.SECONDS);
  }

  public static int max(String... texts)
  {
    int max = 0;
    for(String text : texts)
    {
      JLabel tmp = new JLabel(text);
      int width = tmp.getPreferredSize().width;
      if(max < width)
      {
        max = width;
      }
    }
    return max;
  }

  public void setStatus(String name, String message)
  {
    if(!labels.containsKey(name))
    {
      Logger.getLogger(StatusLine.class.getName())
          .log(Level.WARNING, "status for {0} not found", name);
    }
    else
    {
      JLabel status = labels.get(name);
      status.setText((message == null || message.isBlank()) ? NBSP : message);
      Logger.getLogger(StatusLine.class.getName()).log(Level.FINE, "{0}: {1}",
          new Object[]
          {
            name, message
          });
    }
  }

  private Map<Integer, String> positions;
  private Map<String, JLabel> labels;

  public void createStatus(String name, int position, int width)
  {
    if(labels == null)
    {
      labels = new HashMap<>();
      positions = new TreeMap<>();
    }
    positions.put(position, name);
    JLabel status = new JLabel(name, JLabel.RIGHT);
    status.setForeground(UIManager.getColor("textInactiveText"));
    status.setToolTipText(name);
    status.setPreferredSize(
        new java.awt.Dimension(width + 4, status.getPreferredSize().height));
    status.setBorder(BorderFactory
        .createMatteBorder(0, 1, 0, 0, UIManager.getColor("controlShadow")));
    labels.put(name, status);

    if(statusBlock == null)
    {
      statusBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
      add(statusBlock, BorderLayout.LINE_END);
    }
    int maxWidth = labels.values().stream()
        .map(JLabel::getPreferredSize)
        .mapToInt(d -> d.width)
        .sum()
        + (labels.size()) * 4;
    statusBlock.setPreferredSize(
        new Dimension(maxWidth, status.getPreferredSize().height));

    statusBlock.removeAll();
    positions.values().stream()
        .map(labels::get)
        .forEach(statusBlock::add);
  }
}
