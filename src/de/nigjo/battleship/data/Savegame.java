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

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author nigjo
 */
public class Savegame
{
  private final List<Record> records;
  private Path filename;

  private Savegame()
  {
    records = new ArrayList<>();
  }

  public static Savegame createNew(int... ships)
  {
    Savegame game = new Savegame();
    game.addRecord(new Record(Record.VERSION, 1, "0"));
    String shipsList = IntStream.of(ships)
        .boxed()
        .reduce((String)null,
            (s, i) -> s == null ? ("" + i) : (s + "," + i),
            (a, s) -> a + "," + s);
    game.addRecord(new Record(Record.SHIPS, 1, shipsList));
    return game;
  }

  public void addRecord(Record record)
  {
    if(record.getPlayerid() != 1
        && record.getPlayerid() != 2)
    {
      throw new IllegalArgumentException("invalid player number " + record.getPlayerid());
    }
    this.records.add(record);
    if(filename != null)
    {
      try
      {
        storeToFile(filename);
      }
      catch(IOException ex)
      {
        throw new UncheckedIOException(ex);
      }
    }
  }

  public Stream<Record> records(int player, String kind)
  {
    return this.records.stream()
        .filter(r -> r.getPlayerid() == player)
        .filter(r -> kind.equals(r.getKind()));
  }

  public static Savegame readFromFile(Path savegameFile) throws IOException
  {
    var fis = new FileInputStream(savegameFile.toFile());
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
        else
        {
          Savegame.Record record = parseLine(zeile);
          if(record != null)
          {
            savedgame.addRecord(record);
          }
        }
      }

      savedgame.setFilename(savegameFile);

      return savedgame;
    }
    catch(UncheckedIOException uioe)
    {
      throw uioe.getCause();
    }
  }

  private static Savegame.Record parseLine(String storedLine)
  {
    if(storedLine.startsWith(";") || !Character.isUpperCase(storedLine.charAt(0)))
    {
      return null;
    }

    if(storedLine.matches("^[A-Z]+:\\d,.*"))
    {
      String command = storedLine.substring(0, storedLine.indexOf(':'));
      String player =
          storedLine.substring(storedLine.indexOf(':') + 1, storedLine.indexOf(','));
      String payload = storedLine.substring(storedLine.indexOf(',') + 1);
      return new Savegame.Record(command, Integer.parseInt(player), payload);
    }
    return null;
  }

  public void storeToFile(Path savegameFile) throws IOException
  {
    var fos = new FileOutputStream(savegameFile.toFile());
    FileLock lock = fos.getChannel().lock();
    try(BufferedWriter out = new BufferedWriter(
        new OutputStreamWriter(fos, StandardCharsets.UTF_8)))
    {
      for(Record record : this.records)
      {
        out.write(record.toString());
        out.newLine();
      }
      setFilename(savegameFile);
    }
    finally
    {
      if(lock.isValid())
      {
        lock.release();
      }
    }
  }

  private void setFilename(Path savegameFile)
  {
    this.filename = savegameFile;
  }

  public static class Record
  {
    /**
     * Version of this Savegame. The version defines what commands are known and what
     * encription is used.
     */
    public static final String VERSION = "VERSION";
    /**
     * Any text that my describe the match. All comments are ignored by the game. Comments
     * are not encrypted.
     */
    public static final String MESSAGE = "MESSAGE";
    /**
     * Public key of a player. The Key is BASE64 encoded and prefixed by the playerid and
     * a comma.
     */
    public static final String PLAYER = "PLAYER";
    /**
     * Unencrypted list of used ships. Must be defined by player 1 and has to exist
     * exactly once in a savegame.
     */
    public static final String SHIPS = "SHIPS";
    /**
     * Encrypted board of a player. Each player stores its own board with its own public
     * key. So the player itself can decrpyt only its own board.
     */
    public static final String BOARD = "BOARD";
    public static final String ATTACK = "ATTACK";
    public static final String RESULT = "RESULT";

    private final String kind;
    private final int playerid;
    private final String payload;

    public Record(String kind, int playerid, String payload)
    {
      this.kind = kind;
      this.playerid = playerid;
      this.payload = payload;
    }

    public String getKind()
    {
      return kind;
    }

    public int getPlayerid()
    {
      return playerid;
    }

    public String getPayload()
    {
      return payload;
    }

    @Override
    public String toString()
    {
      return kind + ":" + playerid + "," + String.join("\\n", payload);
    }

  }

}
