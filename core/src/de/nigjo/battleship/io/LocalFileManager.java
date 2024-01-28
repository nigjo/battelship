/*
 * Copyright 2024 nigjo.
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
package de.nigjo.battleship.io;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.nigjo.battleship.api.SavegameStorage;
import de.nigjo.battleship.internal.SavegameLoader;
import de.nigjo.battleship.io.internal.BackupManager;

/**
 *
 * @author nigjo
 */
public class LocalFileManager implements SavegameStorage
{
  private final Path saveGameFile;
  private BufferedReader in;

  /**
   *
   * @param saveGameFile
   */
  public LocalFileManager(Path saveGameFile)
  {
    this.saveGameFile = saveGameFile;
  }

  public Path getSaveGameFile()
  {
    return saveGameFile;
  }

  @Override
  public Stream<String> getLines()
  {
    Logger.getLogger(SavegameLoader.class.getName())
        .log(Level.INFO, "loading game from {0}", saveGameFile.toAbsolutePath());
    try
    {
      var fis = new FileInputStream(saveGameFile.toFile());
      in = new BufferedReader(
          new InputStreamReader(fis, StandardCharsets.UTF_8));

      return in.lines();

    }
    catch(IOException ex)
    {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public void doneRead()
  {
    try
    {
      in.close();
    }
    catch(IOException ex)
    {
      Logger.getLogger(LocalFileManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void storeLines(Stream<String> lines)
  {
    try
    {
      var fos = new FileOutputStream(saveGameFile.toFile());
      FileLock lock = fos.getChannel().lock();
      try(BufferedWriter out = new BufferedWriter(
          new OutputStreamWriter(fos, StandardCharsets.UTF_8)))
      {
        String lastLine = null;
        Iterator<String> iterator = lines.iterator();
        while(iterator.hasNext())
        {
          lastLine = iterator.next();
          out.write(lastLine);
          out.newLine();
        }
        //TODO:setIoStorage(storage);

        BackupManager.backup(this, lastLine);
      }
      finally
      {
        if(lock.isValid())
        {
          lock.release();
        }
      }
    }
    catch(IOException ex)
    {
      throw new UncheckedIOException(ex);
    }
  }

}
