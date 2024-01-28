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
package de.nigjo.battleship.io.internal;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.nigjo.battleship.data.Savegame;

/**
 *
 * @author nigjo
 */
public class SavegameManager implements Closeable
{
  private final Path saveGameFile;
  private final Runnable closer;
  private final ExecutorService updater;
  private volatile boolean checking;

  public SavegameManager(Path saveGameFile, Consumer<Savegame> savegameConsumer)
      throws IOException
  {
    this.saveGameFile = saveGameFile;
    WatchService ws = FileSystems.getDefault().newWatchService();
    WatchKey modKey = saveGameFile.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
    ThreadFactory deamonCreator = (n) ->
    {
      Thread t = new Thread(n, "savegamemanager");
      t.setDaemon(true);
      return t;
    };
    //WatchService ws = saveGameFile.getFileSystem().newWatchService();
    ExecutorService watcher = Executors.newFixedThreadPool(1, deamonCreator);
    updater = Executors.newFixedThreadPool(1, deamonCreator);
    closer = () ->
    {
      modKey.cancel();
      watcher.shutdown();
      updater.shutdown();
    };
    watcher.submit(() ->
    {
      try
      {
        WatchKey key;
        while(null != (key = ws.take()))
        {
          if(!checking)
          {
            updater.submit(this::readSavegame);
          }
          key.reset();
        }
      }
      catch(InterruptedException ex)
      {
        Logger.getLogger(SavegameManager.class.getName())
            .log(Level.SEVERE, ex.toString(), ex);
        closer.run();
      }
    });
    if(Files.exists(saveGameFile))
    {
      if(!checking)
      {
        updater.submit(this::readSavegame);
      }
    }
  }

  public void readSavegame()
  {
    checking = true;
//    try
//    {
//      Savegame savedGame = Savegame.readFromFile(saveGameFile);
//    }
//    catch(IOException ex)
//    {
//      Logger.getLogger(SavegameManager.class.getName()).log(Level.SEVERE, null, ex);
//    }
//    finally
//    {
      checking = false;
//    }
  }

  @Override
  public void close() throws IOException
  {
    if(closer != null)
    {
      closer.run();
    }
  }

}
