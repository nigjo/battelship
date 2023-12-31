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
package de.nigjo.battleship;

import java.io.Console;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author nigjo
 */
public enum CliArg
{
  help('?', false),
  id(true),
  backup(false),
  NON_ARG_PARAM
  {
    @Override
    public String toHelpString()
    {
      return "<spielstand>";
    }

  };

  private char shortOption;
  private final boolean hasParam;
  private final boolean mandatory;
  private boolean defined;
  private String param;

  private CliArg()
  {
    this(true);
  }

  private CliArg(boolean hasParam)
  {
    this(hasParam, false);
  }

  private CliArg(boolean hasParam, boolean mandatory)
  {
    this.hasParam = hasParam;
    this.mandatory = mandatory;
  }

  private CliArg(char shortOption)
  {
    this(shortOption, true);
  }

  private CliArg(char shortOption, boolean hasParam)
  {
    this(shortOption, hasParam, false);
  }

  private CliArg(char shortOption, boolean hasParam, boolean mandatory)
  {
    this(hasParam, mandatory);
    this.shortOption = shortOption;
  }

  public boolean isDefined()
  {
    return defined;
  }

  public String getParam()
  {
    return param;
  }

  @Override
  public String toString()
  {
    return "CliArg{"
        + "shortOption=" + shortOption + ", mandatory=" + mandatory
        + ", defined=" + defined
        + ", hasParam=" + hasParam + ", param=" + param + '}';
  }

  public static void parse(String args[])
  {
    CliArg lastArg = null;
    int counter = 0;
    for(String arg : args)
    {
      Logger.getLogger(CliArg.class.getName())
          .log(Level.CONFIG, "{0}: {1}",
              new Object[]
              {
                ++counter,
                arg
              });
      if(arg.charAt(0) == '-')
      {
        if(lastArg != null && lastArg.hasParam)
        {
          throw new IllegalArgumentException("missing param for --" + lastArg.name());
        }
        lastArg = null;
        if(arg.charAt(1) == '-')
        {
          lastArg = Arrays.stream(CliArg.values())
              .filter(a -> arg.substring(2).equals(a.name()))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("unknown option " + arg));
        }
        else
        {
          if(arg.length() == 2)
          {
            char option = arg.charAt(1);
            for(CliArg def : values())
            {
              if(def.shortOption == option)
              {
                lastArg = def;
                break;
              }
            }
          }
        }
        if(lastArg == null)
        {
          throw new IllegalArgumentException("unknown option " + arg);
        }
        if(lastArg.defined)
        {
          throw new IllegalArgumentException("duplicate argument " + arg);
        }
        lastArg.defined = true;
        if(lastArg.hasParam)
        {
          continue;
        }
      }
      else if(lastArg != null)
      {
        lastArg.param = arg;
      }
      else if(NON_ARG_PARAM.defined)
      {
        throw new IllegalArgumentException("unknown argument " + arg);
      }
      else
      {
        NON_ARG_PARAM.defined = true;
        NON_ARG_PARAM.param = arg;
      }
      lastArg = null;
    }
  }

  public String toHelpString()
  {
    StringBuilder help = new StringBuilder();
    if(!mandatory)
    {
      help.append("[");
    }
    help.append("--").append(name());
    if(shortOption != '\0')
    {
      help.append("|-").append(shortOption);
    }
    if(hasParam)
    {
      help.append(" <").append(name()).append(">");
    }
    if(!mandatory)
    {
      help.append("]");
    }
    return help.toString();
  }

  static void showHelp()
  {
    String helpMessage = "java -jar BattleShip.jar";

    AtomicInteger length = new AtomicInteger(0);
    String syntax = Arrays.stream(CliArg.values())
        .map(CliArg::toHelpString)
        .peek(s -> length.getAndUpdate((i) -> Math.max(i, s.length())))
        .reduce(null, (o, n) -> o == null ? n : (o + " " + n));

    ResourceBundle bundle = ResourceBundle.getBundle(CliArg.class.getName());
    String extended = Arrays.stream(CliArg.values())
        .map(arg -> arg.toHelpString()
        + " " + " ".repeat(Math.max(0, length.get() - arg.toHelpString().length()))
        + (bundle.containsKey(arg.name()) ? bundle.getString(arg.name()) : "..."))
        .reduce("\n", (o, n) -> o + "\n" + n);

    helpMessage += " " + syntax + extended;

    if(GraphicsEnvironment.isHeadless())
    {
      Console console = System.console();
      if(console != null)
      {
        console.writer().append(helpMessage);
      }
      else
      {
        System.out.println(helpMessage);
      }
    }
    else
    {
      String uiHelp = "<html><pre>" + helpMessage
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;")
          .replace("\"", "&quot;")
          .replace("\n", "<br>");
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, uiHelp));
    }
  }

  public static void showError(IllegalArgumentException ex)
  {
    showError(ex.getLocalizedMessage());
  }

  public static void showError(String message) throws HeadlessException
  {
    if(!GraphicsEnvironment.isHeadless())
    {
      JOptionPane.showMessageDialog(null, message,
          "Battleship", JOptionPane.ERROR_MESSAGE);
    }
    else
    {
      Console console = System.console();
      if(console != null)
      {
        console.writer().println(message);
      }
      else
      {
        System.err.println(message);
      }
    }
  }
}
