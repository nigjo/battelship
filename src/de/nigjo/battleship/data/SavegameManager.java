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

import de.nigjo.battleship.data.Savegame;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  public SavegameManager(Path saveGameFile, Consumer<Savegame> savegame)
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
    try
    {
      var fis = new FileInputStream(saveGameFile.toFile());
      FileLock lock = fis.getChannel().lock();
      try(BufferedReader in = new BufferedReader(
          new InputStreamReader(fis, StandardCharsets.UTF_8)))
      {
        Savegame savedgame = new Savegame();
        String zeile;
        String comment = null;
        while(null != (zeile = in.readLine()))
        {
          if(zeile.isBlank())
          {
            continue;
          }
          if(zeile.startsWith(";"))
          {
            if(comment == null)
            {
              comment = zeile.substring(1);
            }
            else
            {
              comment += "\n" + zeile.substring(1);
            }
          }
          else{
            int colon = zeile.indexOf(':');
            if(colon<0)
              continue;
            String cmd = zeile.substring(0,colon);
          }
        }
      }
      catch(UncheckedIOException uioe)
      {
        throw uioe.getCause();
      }
      finally
      {
        lock.release();
      }
    }
    catch(IOException ex)
    {
      Logger.getLogger(SavegameManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally
    {
      checking = false;
    }
  }

  @Override
  public void close() throws IOException
  {
    if(closer != null)
    {
      closer.run();
    }
  }

  private void parseLine(Savegame savedgame, String storedLine)
  {
    if(storedLine.startsWith(";"))
    {

    }
  }

  public static class Record
  {
    public static final String VERSION = "VERSION";
    public static final String PLAYER = "PLAYER";
    public static final String BOARD = "BOARD";
    public static final String SHOOT = "SHOOT";
    public static final String OUTCOME = "OUTCOME";
    private final String kind;
    private final int playerid;
    private final List<String> payload;
    private String comment;

    public Record(String kind, int playerid, String... payload)
    {
      this.kind = kind;
      this.playerid = playerid;
      this.payload = Arrays.asList(payload);
    }

    public String getKind()
    {
      return kind;
    }

    public int getPlayerid()
    {
      return playerid;
    }

    public List<String> getPayload()
    {
      return payload;
    }

    public String getComment()
    {
      return comment;
    }

    public void setComment(String comment)
    {
      this.comment = comment;
    }

  }

}
