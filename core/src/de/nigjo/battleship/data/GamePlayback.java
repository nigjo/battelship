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

import java.util.List;

/**
 *
 * @author nigjo
 */
public class GamePlayback
{
  private final Savegame savegame;
  private int playerid;
  private KeyManager playerKeys;

  private GamePlayback(Savegame savegame)
  {
    this.savegame = savegame;
  }

  public static GamePlayback from(Savegame savegame)
  {
    return new GamePlayback(savegame);
  }

  public GamePlayback asPlayer(int playerid)
  {
    this.playerid = playerid;
    return this;
  }

  public GamePlayback with(KeyManager playerKeys)
  {
    this.playerKeys = playerKeys;
    return this;
  }

  public void to(BoardData data)
  {
    List<Savegame.Record> records = validateInput();

    for(Savegame.Record record : records)
    {
      if(record.getPlayerid() != playerid)
      {
        continue;
      }

      String encoded = record.getPayload();
      switch(record.getKind())
      {
        case Savegame.Record.VERSION:
        case Savegame.Record.CONFIG:
        case Savegame.Record.PLAYER:
        case Savegame.Record.BOARD:
        case Savegame.Record.MESSAGE:
          //ignore here
          break;
        case Savegame.Record.ATTACK:
          if(!data.isOpponent())
          {
            String payload = playerKeys.decode(encoded);
            String[] pair = payload.split(",");

            data.shootAt(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
          }
          break;

        case Savegame.Record.RESULT:
          if(data.isOpponent())
          {
            String payload = playerKeys.decode(encoded);
            String[] pair = payload.split(",");
            boolean hit = Boolean.parseBoolean(pair[2]);

            data.markResult(Integer.parseInt(pair[0]),
                Integer.parseInt(pair[1]), hit);
          }
          break;
      }
    }
  }

  private List<Savegame.Record> validateInput() throws IllegalArgumentException
  {
    if(savegame == null)
    {
      throw new IllegalArgumentException("missing savegame data");
    }
    if(playerKeys == null)
    {
      throw new IllegalArgumentException("missing key manager");
    }
    if(playerid <= 0)
    {
      throw new IllegalArgumentException("missing player id");
    }

    List<Savegame.Record> records = savegame.records();
    Savegame.Record first = records.get(0);
    if(!Savegame.Record.VERSION.equals(first.getKind()))
    {
      throw new IllegalStateException("no game version found.");
    }
    int version = Integer.parseInt(first.getPayload());
    if(version != 0)
    {
      throw new IllegalStateException("unknown game version " + version);
    }

    return records;
  }

}
