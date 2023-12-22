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

import java.util.Random;

/**
 *
 * @author nigjo
 */
public class BoardData
{
  public static final String KEY_SELF = "BoardData.own";
  public static final String KEY_OPPONENT = "BoardData.opponent";

  public static final int[] GAME_CLASSIC = mkgame(5, 4, 4, 3, 3, 3, 2, 2, 2, 2);
  public static final int[] GAME_SIMPLE = mkgame(5, 4, 3, 3, 2);
  //                                    012345678901234567890123456789
  private static final char[] STATUS = ".-WwEeHh.-NnSsVv".toCharArray();
  public static final int UNKNOWN = 0;
  public static final int SHOOTED_AT = 1; //=1
  public static final int SHIP_START = 1 << 1;
  public static final int SHIP_END = 1 << 2;
  public static final int VERTICAL = 1 << 3;
  public static final int SHIP = SHIP_START | SHIP_END;
  public static final int SHIP_MID_H = SHIP_START | SHIP_END;
  public static final int SHIP_MID_V = SHIP_START | SHIP_END | VERTICAL;
  public static final int SHIP_WEST = SHIP_START;
  public static final int SHIP_EAST = SHIP_END;
  public static final int SHIP_NORTH = SHIP_START | VERTICAL;
  public static final int SHIP_SOUTH = SHIP_END | VERTICAL;
  public static final int ID_SHIFT = 4;//=16
  public static final int MAX_STATUS = (1 << ID_SHIFT) - 1;//=15

  private final int[] board;
  private boolean hasShips;
  private boolean active;
  private final int size;
  private boolean opponent;

  public BoardData(int size)
  {
    this.size = size;
    board = new int[size * size];
  }

  private static int[] mkgame(int... ships)
  {
    return ships;
  }

  public int getSize()
  {
    return size;
  }

  public int shipIdAt(int x, int y)
  {
    return board[y * size + x] >>> ID_SHIFT;
  }

  public int stateAt(int x, int y)
  {
    return board[y * size + x] & MAX_STATUS;
  }

  public void placeShip(int shipIndex,
      int xstart, int ystart, int length, boolean vertical)
  {
    if(active)
    {
      throw new IllegalStateException("board is already in use");
    }
    if(xstart + (vertical ? 0 : length) > size
        || ystart + (vertical ? length : 0) > size)
    {
      throw new IllegalArgumentException("ship can't be places outside board");
    }

    int shipId = (vertical ? SHIP_MID_V : SHIP_MID_H) | (shipIndex << ID_SHIFT);

    int[] backup = new int[board.length];
    System.arraycopy(board, 0, backup, 0, board.length);
    for(int i = 0; i < length; i++)
    {
      int y = vertical ? ystart + i : ystart;
      int x = vertical ? xstart : (xstart + i);

      if((board[y * size + x] & MAX_STATUS) != 0)
      {
        System.arraycopy(backup, 0, board, 0, board.length);
        throw new IllegalArgumentException("ship collides with placed ship");
      }
      board[y * size + x] = shipId;
      if(i == 0)
      {
        board[y * size + x] ^= SHIP_END;
      }
      else if(i == length - 1)
      {
        board[y * size + x] ^= SHIP_START;
      }
    }
    hasShips = true;
  }

  public void markResult(int x, int y, boolean hit)
  {
    if(hasShips)
    {
      throw new IllegalStateException("this is your own board");
    }
    active = true;
    board[y * size + x] &= hit ? SHIP : SHOOTED_AT;
  }

  public boolean shootAt(int x, int y)
  {
    if(!hasShips)
    {
      throw new IllegalStateException("this is not your own board");
    }
    active = true;
    board[y * size + x] &= SHOOTED_AT;

    return (board[y * size + x] & SHIP) > 0;
  }

  @Override
  public String toString()
  {
    //char[] state = "·-SX".toCharArray();

    char[] data = new char[board.length];
    for(int i = 0; i < board.length; i++)
    {
      data[i] = STATUS[board[i] & MAX_STATUS];
    }
    return new String(data);
  }

  public static BoardData parse(String boarddata)
  {
    //·=empty, -=miss, S=Ship, X=hit
    int size = (int)Math.sqrt(boarddata.length());
    if(size * size != boarddata.length())
    {
      throw new IllegalArgumentException("invalid board data length");
    }

    String state = new String(STATUS);
    BoardData data = new BoardData(size);
    for(int i = 0; i < data.board.length; i++)
    {
      data.board[i] = state.indexOf(boarddata.charAt(i));
    }

    return data;
  }

  public static BoardData generateRandom(int size, int... ships)
  {
    return generateRandom(size, System.currentTimeMillis(), ships);
  }

  public static BoardData generateRandom(int size, long seed, int... ships)
  {
    return generateRandom(size, new Random(seed), ships);
  }

  public static BoardData generateRandom(int size, Random rnd, int... ships)
  {
    BoardData data = new BoardData(size);
    int index = 0;
    for(int ship : ships)
    {
      int x = rnd.nextInt(size - ship + 1);
      int y = rnd.nextInt(size - ship + 1);
      boolean vertical = rnd.nextBoolean();
      try
      {
        data.placeShip(++index, x, y, ship, vertical);
      }
      catch(IllegalArgumentException ex)
      {
        //unable to place ship; next try
        return generateRandom(size, rnd, ships);
      }
    }

    return data;
  }

  public void setOpponent(boolean opponent)
  {
    this.opponent = opponent;
  }

  public boolean isOpponent()
  {
    return opponent;
  }

}
