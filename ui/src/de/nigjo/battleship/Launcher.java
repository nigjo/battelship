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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.nigjo.battleship.api.StatusDisplayer;
import de.nigjo.battleship.ui.DialogDisplayer;
import de.nigjo.battleship.ui.GameBoard;
import de.nigjo.battleship.ui.StatusLine;
import de.nigjo.battleship.ui.SwingDisplayer;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class Launcher
{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    //<editor-fold defaultstate="collapsed" desc="ensureLaF();">
    if(!GraphicsEnvironment.isHeadless())
    {
      SwingUtilities.invokeLater(Launcher::initUI);
    }
    //</editor-fold>

    initLogger();

    //<editor-fold defaultstate="collapsed" desc="parseCommandLine();">
    try
    {
      CliArg.parse(args);
    }
    catch(IllegalArgumentException ex)
    {
      CliArg.showError(ex);
      System.exit(1);
      return;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="run();">
    if(CliArg.help.isDefined())
    {
      CliArg.showHelp();
    }
    else if(!GraphicsEnvironment.isHeadless())
    {
      initializeGameUI();
    }
    else
    {
      CliArg.showError("Spiel kann nicht ohne UI starten.");
      System.exit(2);
    }
    //</editor-fold>
  }

  private static void initializeGameUI()
  {
    String idFileName =
        CliArg.id.isDefined() ? CliArg.id.getParam() : "battleship.player.id";

    BattleshipGame game = new BattleshipGame(Path.of(idFileName));
    game.updateState(BattleshipGame.STATE_FINISHED);

    Storage.getDefault().put(BattleshipGame.class.getName(), game);

    SwingUtilities.invokeLater(() ->
    {
      Logger.getLogger(Launcher.class.getName()).log(Level.FINEST,
          "creating UI");
      Launcher.createUI();
      if(CliArg.NON_ARG_PARAM.isDefined())
      {
        Logger.getLogger(Launcher.class.getName()).log(Level.FINEST,
            "savegame defined in command line");
        SwingUtilities.invokeLater(() -> Launcher.loadGamefile(game));
      }
      else
      {
        Logger.getLogger(Launcher.class.getName()).log(Level.FINEST,
            "start with random boards");
        game.initRandom();
      }
    });

  }

  private static void loadGamefile(BattleshipGame game)
  {
    try
    {
      Path loadgame = Path.of(CliArg.NON_ARG_PARAM.getParam());
      Logger.getLogger(Launcher.class.getName())
          .log(Level.CONFIG, "loading {0}", loadgame.toAbsolutePath().normalize());
      game.loadSavegame(loadgame);
    }
    catch(RuntimeException ex)
    {
      StatusDisplayer.getDefault().setText(
          ex.getClass().getSimpleName()
          + ": " + ex.getLocalizedMessage());
      ex.printStackTrace(System.err);
    }
    catch(IOException ex)
    {
      StatusDisplayer.getDefault().setText("FEHLER: " + ex.getLocalizedMessage());
    }
  }

  private static void initUI()
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(ReflectiveOperationException | UnsupportedLookAndFeelException ex)
    {
    }

  }

  private static void createUI()
  {
    JFrame frame = new JFrame("Schiffe versenken");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    Storage.getDefault().put(JFrame.class.getName(), frame);
    Storage.getDefault().put(DialogDisplayer.class.getName(), new SwingDisplayer());

    BattleshipGame game =
        Storage.getDefault().get(BattleshipGame.class.getName(), BattleshipGame.class);

    frame.getContentPane().add(new GameBoard(game));
    StatusLine status = new StatusLine();
    Storage.getDefault().put(StatusDisplayer.class.getName(), status);
    frame.getContentPane().add(status, BorderLayout.PAGE_END);

    JMenuBar menu = new JMenuBar();
    frame.setJMenuBar(menu);

    frame.setLocationByPlatform(true);
    frame.pack();
    frame.setVisible(true);

    addObververStatus(game, BattleshipGame.KEY_PLAYER_NUM, 100,
        StatusLine.max("1", "2"));
    addObververStatus(game, BattleshipGame.KEY_PLAYER, 5000,
        StatusLine.max(
            BattleshipGame.PLAYER_SELF,
            BattleshipGame.PLAYER_OPPONENT
        ));
    UnaryOperator<String> stateModifier = s -> s.substring(s.lastIndexOf('.') + 1);
    addObververStatus(game, BattleshipGame.KEY_STATE, 500,
        StatusLine.max(
            stateModifier.apply(BattleshipGame.STATE_ATTACK),
            stateModifier.apply(BattleshipGame.STATE_ATTACKED),
            stateModifier.apply(BattleshipGame.STATE_FINISHED),
            stateModifier.apply(BattleshipGame.STATE_PLACEMENT),
            stateModifier.apply(BattleshipGame.STATE_RESPONSE),
            stateModifier.apply(BattleshipGame.STATE_WAIT_ATTACK),
            stateModifier.apply(BattleshipGame.STATE_WAIT_RESPONSE),
            stateModifier.apply(BattleshipGame.STATE_WAIT_START)
        ), stateModifier);

    StatusDisplayer.getDefault().setText("Willkommen zu Schiffe versenken");
  }

  private static void addObververStatus(BattleshipGame game,
      String key, int pos, int width)
  {
    addObververStatus(game, key, pos, width, UnaryOperator.identity());
  }

  private static void addObververStatus(BattleshipGame game,
      String key, int pos, int width, UnaryOperator<String> statusModifier)
  {
    StatusLine uiStatus = (StatusLine)StatusDisplayer.getDefault();
    uiStatus.createStatus(key, pos, width);
    uiStatus.setStatus(key,
        statusModifier.apply(game.getDataString(key)));
    game.addPropertyChangeListener(key,
        pce -> uiStatus.setStatus(key,
            statusModifier.apply(
                Objects.toString(pce.getNewValue(), null)
            ))
    );
  }

  private static final Logger APP_LOGGER =
      Logger.getLogger(Launcher.class.getPackageName());

  private static void initLogger()
  {
    Storage.getDefault().put(Logger.class.getName(), APP_LOGGER);

    String levelConfig = System.getProperty(APP_LOGGER.getName() + ".level", "INFO");
    Level level = Level.parse(levelConfig);
    APP_LOGGER.setLevel(level);
    APP_LOGGER.setUseParentHandlers(false);

    Handler outhandler = new ConsoleHandler();
    outhandler.setLevel(level);
    Formatter simpleFormatter = new Formatter()
    {
      @Override
      public String format(LogRecord lr)
      {
        String loggerName = lr.getLoggerName();
        if(loggerName == null)
        {
          loggerName = lr.getSourceClassName();
        }
        String simple = loggerName.substring(loggerName.lastIndexOf('.') + 1);

        return lr.getLevel().getName()
            + " [" + simple + "]: "
            + formatMessage(lr) + "\n";
      }
    };
    outhandler.setFormatter(simpleFormatter);
    Launcher.APP_LOGGER.addHandler(outhandler);

    String logfileName = System.getProperty(APP_LOGGER.getName() + ".file");
    if(logfileName != null)
    {
      try
      {
        Path logfile = Path.of(logfileName).toAbsolutePath().normalize();
        FileHandler fileHandler =
            new java.util.logging.FileHandler(logfile.toString());
        fileHandler.setLevel(level);
        fileHandler.setFormatter(simpleFormatter);
        Launcher.APP_LOGGER.addHandler(fileHandler);
      }
      catch(IOException ex)
      {
        throw new UncheckedIOException(ex);
      }
    }
  }
}
