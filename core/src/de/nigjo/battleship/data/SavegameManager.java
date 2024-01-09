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
package de.nigjo.battleship.data;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import de.nigjo.battleship.BattleshipGame;
import de.nigjo.battleship.ui.StatusLine;
import de.nigjo.battleship.ui.actions.LoadGameAction;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public class SavegameManager implements Closeable
{
  private static volatile int counter;

  private final Path saveGameFile;
  private final Runnable closer;
  private Runnable reloader;
  private final ScheduledExecutorService updater;
  private volatile boolean checking;
  private final Loader savegameLoader;
  private boolean opponentActive;

  public static interface Loader
  {
    public void loadSavegame(Path gamefile) throws IOException;
  }

  public SavegameManager(Path savegameFile, Loader savegameLoader)
      throws IOException
  {
    this.saveGameFile = savegameFile.toAbsolutePath().normalize();
    WatchService ws = FileSystems.getDefault().newWatchService();

    WatchKey modKey = this.saveGameFile.getParent()
        .register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
    ThreadFactory deamonCreator = (n) ->
    {
      Thread t = new Thread(n, "savegamemanager-" + (++counter));
      t.setDaemon(true);
      return t;
    };
    //WatchService ws = saveGameFile.getFileSystem().newWatchService();
    //ExecutorService watcher = Executors.newFixedThreadPool(1, deamonCreator);
    updater = Executors.newSingleThreadScheduledExecutor(deamonCreator);
    closer = () ->
    {
      try
      {
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.FINE, "closing checkes for {0}", this.saveGameFile);
        modKey.cancel();
        updater.shutdown();
        ws.close();
      }
      catch(IOException ex)
      {
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.SEVERE, ex.toString(), ex);
      }
    };
    Logger.getLogger(SavegameManager.class.getName())
        .log(Level.FINE, "starting checkes for {0}", this.saveGameFile);
    Thread watcher = new Thread(() ->
    {
      try
      {
        WatchKey key;
        while(null != (key = ws.take()))
        {
          if(!checking)
          {
            boolean changed = false;
            for(WatchEvent<?> evt : key.pollEvents())
            {
              Object context = evt.context();
              if(context instanceof Path)
              {
                Path p = (Path)context;
                if(!p.isAbsolute())
                {
                  p = this.saveGameFile.getParent()
                      .resolve(p)
                      .toAbsolutePath().normalize();
                }
                if(this.saveGameFile.equals(p)
                    && StandardWatchEventKinds.ENTRY_MODIFY.equals(evt.kind()))
                {
                  changed = true;
                  //updater.submit(this::reloadSavegame);
                }
              }
            }
            if(changed && opponentActive)
            {
              if(reloader == null)
              {
                Logger.getLogger(SavegameManager.class.getName())
                    .log(Level.FINE, "change detected.");
                StatusLine.getDefault().setText("Gegner hat gespielt.");
                reloader = () ->
                {
                  reloader = null;
                  Logger.getLogger(SavegameManager.class.getName())
                      .log(Level.FINE, "reload savegame");
                  StatusLine.getDefault().setText("Spiel wird aktualisiert...");
                  SwingUtilities.invokeLater(this::reloadSavegame);
                };
                updater.schedule(reloader, 1, TimeUnit.SECONDS);
              }
              else
              {
                Logger.getLogger(SavegameManager.class.getName())
                    .log(Level.FINE, "change detected. already waiting to update");
              }
            }
          }
          key.reset();
        }
      }
      catch(ClosedWatchServiceException ex)
      {
        //closed externaly
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.FINE, "closed externaly");
      }
      catch(InterruptedException ex)
      {
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.SEVERE, ex.toString(), ex);
        closer.run();
      }
    }, "changewatcher-" + counter);
    watcher.setDaemon(true);
    watcher.start();
    Storage.getDefault().get(BattleshipGame.class)
        .addPropertyChangeListener(BattleshipGame.KEY_PLAYER,
            pce -> opponentActive =
            BattleshipGame.PLAYER_OPPONENT.equals(pce.getNewValue()));

//    if(Files.exists(saveGameFile))
//    {
//      if(!checking)
//      {
//        Logger.getLogger(SavegameManager.class.getName())
//            .log(Level.FINE, "ini for {0}", saveGameFile);
//        updater.submit(this::readSavegame);
//      }
//    }
    this.savegameLoader = savegameLoader;
  }

  private void reloadSavegame()
  {
    checking = true;
    SwingUtilities.invokeLater(() ->
    {
      try
      {
        savegameLoader.loadSavegame(saveGameFile);
      }
      catch(IOException ex)
      {
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.WARNING, ex.toString(), ex);
      }
      finally
      {
        checking = false;
      }
    });
    //Savegame savedGame = Savegame.readFromFile(saveGameFile);
  }

  @Override
  public void close() throws IOException
  {
    if(closer != null)
    {
      if(!updater.isShutdown())
      {
        closer.run();
      }
    }
  }

  public static void register(BattleshipGame game, Path savegameFile) throws IOException
  {
    SavegameManager old = game.getData(SavegameManager.class);
    if(old != null)
    {
      if(old.saveGameFile.equals(savegameFile))
      {
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.FINER, "same savegame as before");
        return;
      }
      Logger.getLogger(SavegameManager.class.getName())
          .log(Level.FINER, "unregister old manager");
      try
      {
        game.putData(SavegameManager.class.getName(), null);
        old.close();
      }
      catch(IOException ex)
      {
        //sollte nicht passieren. eigentlich.
        throw new UncheckedIOException(ex);
      }
    }
    Logger.getLogger(SavegameManager.class.getName())
        .log(Level.FINER, "register new manager");
    SavegameManager manager = new SavegameManager(
        savegameFile, LoadGameAction::loadGame);
    game.putData(SavegameManager.class.getName(), manager);
  }

}
