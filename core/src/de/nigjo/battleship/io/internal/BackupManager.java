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

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import de.nigjo.battleship.data.Savegame.Record;
import de.nigjo.battleship.io.LocalFileManager;

/**
 *
 * @author nigjo
 */
public class BackupManager
{
  private static boolean active = false;

  public static void setActive(boolean active)
  {
    BackupManager.active = active;
  }

  public static void backup(LocalFileManager manager, String lastLine) throws IOException
  {
    if(active)
    {
      Path savegame = manager.getSaveGameFile();
      Path filename = savegame.getFileName();
      String basefile = filename.getFileName().toString();
      Path archive = filename.resolveSibling(basefile + ".zip");

      addToExisting(archive, savegame, lastLine);
    }
  }

  private static void addToExisting(Path archive, Path savegame, String lastLine)
      throws IOException
  {
    String basefile = savegame.getFileName().toString();
    String basename = basefile.substring(0, basefile.lastIndexOf('.'));
    String ext = basefile.substring(basefile.lastIndexOf('.'));

    URI resource = URI.create("jar:" + archive.toUri());
    try(FileSystem zipfs = FileSystems.newFileSystem(
        resource, Map.of("create", "true")))
    {
      int maxnum = 0;
      for(Path root : zipfs.getRootDirectories())
      {
        int localMax = Files.list(root)
            .filter(c -> !Files.isDirectory(c))
            .map(Path::getFileName)
            .map(Path::toString)
            .filter(name -> name.matches(Pattern.quote(basename) + "-\\d+-.*"))
            .map(n
                -> n.substring(basename.length() + 2,
                n.indexOf('-', basename.length() + 2)))
            .mapToInt(Integer::parseInt)
            .max().orElse(maxnum);
        if(maxnum < localMax)
        {
          maxnum = localMax;
        }
      }
      //var recs = savegame.allRecords();
      //Savegame.Record lastRec = Savegame.recs.get(recs.size() - 1);
      Record lastRec = Record.parseLine(lastLine);
      String backupname = basename
          + "-" + String.format("%04d", maxnum + 1)
          + "-player" + lastRec.getPlayerid()
          + "-" + lastRec.getKind()
          + ext;
      Path copy = zipfs.getPath("/", backupname);
      Files.copy(savegame, copy);
//      try(BufferedWriter out = Files.newBufferedWriter(copy, StandardCharsets.UTF_8))
//      {
//        for(Savegame.Record record : recs)
//        {
//          out.write(record.toString());
//          out.newLine();
//        }
//      }
    }
  }

}
