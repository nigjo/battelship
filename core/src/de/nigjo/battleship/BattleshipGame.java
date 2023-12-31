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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.data.Savegame;
import de.nigjo.battleship.util.Storage;

/**
 *
 * @author nigjo
 */
public final class BattleshipGame
{
  private final Storage gamedata;
  private final Consumer<Runnable> stateChangeRunner;
  public static final String KEY_PLAYER = "BattleshipGame.activePlayer";
  public static final String PLAYER_SELF = "self";
  public static final String PLAYER_OPPONENT = "opponent";
  public static final String KEY_PLAYER_NUM = "BattleshipGame.player";
  public static final String KEY_STATE = "BattleshipGame.gamestate";
  public static final String STATE_PLACEMENT = "BattleshipGame.gamestate.placement";
  public static final String STATE_WAIT_START = "BattleshipGame.gamestate.waitForStart";
  public static final String STATE_ATTACK = "BattleshipGame.gamestate.doAttack";
  public static final String STATE_ATTACKED = "BattleshipGame.gamestate.underAttack";
  public static final String STATE_RESPONSE = "BattleshipGame.gamestate.resultOfAttack";
  public static final String STATE_WAIT_ATTACK = "BattleshipGame.gamestate.waitForAttack";
  public static final String STATE_WAIT_RESPONSE =
      "BattleshipGame.gamestate.waitForResult";
  public static final String STATE_FINISHED = "BattleshipGame.gamestate.endOfGame";

  public static final class Config
  {
    private final String key;
    private final String value;

    public Config(String key, String value)
    {
      this.key = key;
      this.value = value;
    }

    public String getKey()
    {
      return key;
    }

    public String getValue()
    {
      return value;
    }
  }

  public BattleshipGame(Path playerId)
  {
    this(playerId, createExecutor());
  }

  private static Consumer<Runnable> createExecutor()
  {
    if(GraphicsEnvironment.isHeadless())
    {
      var service = Executors.newSingleThreadExecutor((r) ->
      {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      });
      return service::execute;
    }
    else
    {
      return SwingUtilities::invokeLater;
    }
  }

  public BattleshipGame(Path playerId, Consumer<Runnable> stateChangeRunner)
  {
    this.gamedata = new Storage();
    gamedata.put(KeyManager.KEY_MANAGER_SELF, new KeyManager(playerId));
    setConfig("size", "10");
    setConfig("ships",
        Arrays.stream(BoardData.GAME_SIMPLE)
            .mapToObj(String::valueOf)
            .reduce((s1, s2) -> s1 + "," + s2)
            .orElseThrow());

    clearBoards();

    //just run-test the keymanager
    validateKeyManager();

    gamedata.addPropertyChangeListener(KEY_STATE, new StateObserver(this));

    this.stateChangeRunner = stateChangeRunner;
  }

  public void setConfig(String key, String value)
  {
    gamedata.put("config." + key, new Config(key, value));
  }

  public Optional<Config> getConfig(String key)
  {
    for(Config config : gamedata.getAll(Config.class))
    {
      if(key.equals(config.getKey()))
      {
        return Optional.of(config);
      }
    }
    return Optional.empty();
  }

  public <T> T getData(Class<T> valueType)
  {
    return gamedata.get(valueType);
  }

  public <T> T getData(String boardkey, Class<T> valueType)
  {
    return gamedata.get(boardkey, valueType);
  }

  public void putData(String key, Object value)
  {
    gamedata.put(key, value);
  }

  public String getDataString(String key)
  {
    return gamedata.getString(key);
  }

  public int getDataInt(String key, int def)
  {
    return gamedata.getInt(key, def);
  }

  public boolean getDataBoolean(String key, boolean def)
  {
    return gamedata.getBoolean(key, def);
  }

  public <T> T getDataOrSet(String key, Class<T> type, Supplier<T> def)
  {
    return gamedata.getOrSet(key, type, def);
  }

  public <T> Optional<T> findData(Class<T> type)
  {
    return gamedata.find(type);
  }

  public <T> Collection<T> getAllData(Class<T> type)
  {
    return gamedata.getAll(type);
  }

  public void addPropertyChangeListener(String propertyName,
      PropertyChangeListener listener)
  {
    gamedata.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(String propertyName,
      PropertyChangeListener listener)
  {
    gamedata.removePropertyChangeListener(propertyName, listener);
  }

  public void clearBoards()
  {
    Config sizeCfg =
        getDataOrSet("config.size", Config.class,
            () -> new Config("size", String.valueOf(10)));
    int size = Integer.parseInt(sizeCfg.getValue());
    clearBoard(gamedata, size, false);
    clearBoard(gamedata, size, true);
  }

  public void clearBoard(boolean opponent)
  {
    Config sizeCfg =
        getDataOrSet("config.size", Config.class,
            () -> new Config("size", String.valueOf(10)));
    clearBoard(gamedata, Integer.parseInt(sizeCfg.getValue()), opponent);
  }

  private void clearBoard(Storage gamedata, int size, boolean opponent)
  {
    if(opponent)
    {
      BoardData op = new BoardData(size);
      op.setOpponent(true);
      gamedata.put(BoardData.KEY_OPPONENT, op);
    }
    else
    {
      gamedata.put(BoardData.KEY_SELF, new BoardData(size));
    }
  }

  private void validateKeyManager()
  {
    KeyManager km = gamedata.get(KeyManager.KEY_MANAGER_SELF, KeyManager.class);

    validate(km, "BattleShip");
    validate(km, "A".repeat(100));

    String plainBoard =
        this.gamedata.get(BoardData.KEY_SELF, BoardData.class).toString();
    validate(km, plainBoard);
  }

  private static void validate(KeyManager km, String expected)
  {
    String resultE1 = km.encode(expected);
    String resultD1 = km.decode(resultE1);
    if(!expected.equals(resultD1))
    {
      throw new IllegalStateException("expected " + expected + " but got " + resultD1);
    }
  }

  public void initRandom()
  {
    initRandom(System.currentTimeMillis());
  }

  public void initRandom(long seed)
  {
    initRandom(seed, 10, BoardData.GAME_SIMPLE);
  }

  public void initRandom(long seed, int size, int... ships)
  {
    Random rnd = new Random(seed);
    BoardData own = BoardData.generateRandom(size, rnd, ships);
    gamedata.put(BoardData.KEY_SELF, own);
    BoardData opponent = BoardData.generateRandom(size, rnd, ships);
    opponent.setOpponent(true);
    gamedata.put(BoardData.KEY_OPPONENT, opponent);
  }

  public void createNewGame(Path savegameFile) throws IOException
  {
    Savegame savegame = Savegame.createNew();

    Map<String, String> orderedConfig = new TreeMap<>();
    this.getAllData(BattleshipGame.Config.class)
        .forEach(cfg -> orderedConfig.put(cfg.getKey(), cfg.getValue()));
    orderedConfig.forEach(savegame::setConfig);

    KeyManager km = this.getData(KeyManager.KEY_MANAGER_SELF, KeyManager.class);
    savegame.addRecord(Savegame.Record.PLAYER, 1, km.getPublicKey());

    savegame.storeToFile(savegameFile);
    this.putData(Savegame.class.getName(), savegame);

    this.clearBoards();
    this.putData(BattleshipGame.KEY_PLAYER_NUM, 1);

    this.updateState(BattleshipGame.STATE_PLACEMENT);
  }

  public void updateState()
  {
    Logger.getLogger(BattleshipGame.class.getName())
        .log(Level.FINER, "updating next state from current game state");
    int selfId = getDataInt(KEY_PLAYER_NUM, 0);
    Savegame.Record lastAction = getData(Savegame.class).getLastRecord();
    if(Savegame.Record.ATTACK.equals(lastAction.getKind()))
    {
      if(selfId == lastAction.getPlayerid())
      {
        // Es wurde auf uns geschossen. Treffer pruefen.
        updateState(STATE_ATTACKED);
      }
      else
      {
        //Wir haben geschossen. Warten auf Antwort.
        updateState(STATE_WAIT_RESPONSE);
      }
    }
    else if(Savegame.Record.RESULT.equals(lastAction.getKind()))
    {
      if(selfId == lastAction.getPlayerid())
      {
        //Ergebnis unseres Schusses
        updateState(STATE_RESPONSE);
      }
      else
      {
        //Wir haben unser Ergebnis gesendet

        //TODO: Wie kann ich erkennen, dass wir dran sind?
        String[] result = getData(Savegame.class)
            .getAttack(lastAction,
                getData(KeyManager.KEY_MANAGER_SELF, KeyManager.class));
        boolean lastAttackWasHit = Boolean.parseBoolean(result[2]);
        if(lastAttackWasHit)
        {
          updateState(STATE_WAIT_ATTACK);
        }
        else
        {
          updateState(STATE_ATTACK);
        }
      }
    }
    else
    {
      updateState(STATE_WAIT_START);
    }
  }

  public void updateState(String state)
  {
    stateChangeRunner.accept(() -> gamedata.put(BattleshipGame.KEY_STATE, state));
  }

}
