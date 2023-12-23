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
import java.nio.file.Path;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.nigjo.battleship.ui.DialogDisplayer;
import de.nigjo.battleship.ui.GameBoard;
import de.nigjo.battleship.ui.StatusLine;
import de.nigjo.battleship.ui.SwingDisplayer;
import de.nigjo.battleship.ui.actions.LoadGameAction;
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
    game.initRandom();
    game.updateState(BattleshipGame.STATE_FINISHED);

    Storage.getDefault().put(BattleshipGame.class.getName(), game);

    SwingUtilities.invokeLater(Launcher::createUI);

    if(CliArg.NON_ARG_PARAM.isDefined())
    {
      SwingUtilities.invokeLater(Launcher::loadGamefile);
    }
  }

  private static void loadGamefile()
  {
    try
    {
      LoadGameAction.loadGame(Path.of(CliArg.NON_ARG_PARAM.getParam()));
    }
    catch(RuntimeException ex)
    {
      StatusLine.getDefault().setText(
          ex.getClass().getSimpleName()
          + ": " + ex.getLocalizedMessage());
      ex.printStackTrace(System.err);
    }
    catch(IOException ex)
    {
      StatusLine.getDefault().setText("FEHLER: " + ex.getLocalizedMessage());
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

    frame.getContentPane().add(new GameBoard(game.getGamedata()));
    frame.getContentPane().add(StatusLine.getDefault(), BorderLayout.PAGE_END);

    JMenuBar menu = new JMenuBar();
    frame.setJMenuBar(menu);

    frame.setLocationByPlatform(true);
    frame.pack();
    frame.setVisible(true);

    StatusLine.getDefault().setText("Willkommen zu Schiffe versenken");
  }

}
