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

  public static final int UNKNOWN = 0;
  public static final int SHOOTED_AT = 1;
  public static final int SHIP = 1 << 1;
  public static final int HITTED_SHIP = SHIP | SHOOTED_AT;
  public static final int SHIP_VERTICAL = SHIP | 1 << 2;
  public static final int SHIP_END = SHIP | 1 << 3;
  public static final int SHIP_START = SHIP | 1 << 4;
  public static final int SHIP_WEST = SHIP_START;
  public static final int SHIP_EAST = SHIP_END;
  public static final int SHIP_NORTH = SHIP_START | SHIP_VERTICAL;
  public static final int SHIP_SOUTH = SHIP_END | SHIP_VERTICAL;
  public static final int ID_SHIFT = 6;
  public static final int MAX_STATUS = (1 << ID_SHIFT) - 1;

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
    if(xstart + length > size || ystart + length > size)
    {
      throw new IllegalArgumentException("ship can't be places outside board");
    }

    int shipId = (vertical ? SHIP_VERTICAL : SHIP) | (shipIndex << ID_SHIFT);

    int[] backup = new int[board.length];
    System.arraycopy(board, 0, backup, 0, board.length);
    for(int i = 0; i < length; i++)
    {
      int y = vertical ? ystart + i : ystart;
      int x = vertical ? xstart : (xstart + i);

      if((board[y * size + x] & SHIP) != 0)
      {
        System.arraycopy(backup, 0, board, 0, board.length);
        throw new IllegalArgumentException("ship collides with placed ship");
      }
      board[y * size + x] = shipId;
      if(i == 0)
      {
        board[y * size + x] |= SHIP_START;
      }
      else if(i == length - 1)
      {
        board[y * size + x] |= SHIP_END;
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
    board[y * size + x] &= hit ? HITTED_SHIP : SHOOTED_AT;
  }

  public boolean shootAt(int x, int y)
  {
    if(!hasShips)
    {
      throw new IllegalStateException("this is not your own board");
    }
    active = true;
    board[y * size + x] &= SHOOTED_AT;

    return (board[y * size + x] & SHIP) == SHIP;
  }

  @Override
  public String toString()
  {
    char[] state = "·-SX".toCharArray();

    char[] data = new char[size * size];
    for(int y = 0; y < size; y++)
    {
      for(int x = 0; x < size; x++)
      {
        data[y * size + x] = state[board[y * size + x] & HITTED_SHIP];
      }
    }
    return new String(data);
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
      int x = rnd.nextInt(size - ship);
      int y = rnd.nextInt(size - ship);
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
