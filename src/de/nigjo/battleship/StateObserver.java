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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.nigjo.battleship.BattleshipGame.*;
import de.nigjo.battleship.data.BoardData;
import de.nigjo.battleship.data.KeyManager;
import de.nigjo.battleship.data.Savegame;
import de.nigjo.battleship.ui.StatusLine;

/**
 *
 * @author nigjo
 */
public class StateObserver implements PropertyChangeListener
{
  private final BattleshipGame game;

  public StateObserver(BattleshipGame game)
  {
    this.game = game;
  }

  @Override
  public void propertyChange(PropertyChangeEvent pce)
  {
    Object stateValue = pce.getNewValue();
    Logger.getLogger(StateObserver.class.getName()).log(Level.INFO,
        "next state: {0}", stateValue);
    if(!(stateValue instanceof String))
    {
      return;
    }

    int playerSelf = game.getDataInt(KEY_PLAYER_NUM, 0);

    switch((String)stateValue)
    {
      case "BattleshipGame.gamestate.init":
        break;
      case STATE_PLACEMENT:
        //Es wird darauf gewartet dass die eigenen Schiffe platziert sind.
        // Wird in ShipsPlacer behandelt.
        game.putData(KEY_PLAYER, PLAYER_SELF);
        break;
      case STATE_WAIT_START:
        //Lokal sind die Schiffe platziert.
        //Pruefen, ob beide Spieler ein "volles" Brett haben.
        if(game.getData(BoardData.KEY_SELF, BoardData.class).hasShips()
            && game.getData(BoardData.KEY_OPPONENT, BoardData.class).hasShips())
        {
          if(playerSelf == 1)
          {
            game.putData(KEY_PLAYER, PLAYER_SELF);
            game.updateState(STATE_ATTACK);
          }
          else
          {
            game.putData(KEY_PLAYER, PLAYER_OPPONENT);
            game.updateState(STATE_WAIT_ATTACK);
          }
        }
        break;
      case STATE_ATTACK:
        //Es soll ein Schuss erfolgen.
        //Wird in AttackSelection behandelt.
        game.putData(KEY_PLAYER, PLAYER_SELF);
        break;
      case STATE_WAIT_ATTACK:
        //Warten auf einen Schuss
        StatusLine.getDefault().setText("Warte auf einen Schuß aus dem Gegenergebiet.");
        game.putData(KEY_PLAYER, PLAYER_OPPONENT);
        break;
      case STATE_ATTACKED:
      {
        //TODO: Ergebnis pruefen -> Selber oder nochmal warten.
        game.putData(KEY_PLAYER, PLAYER_SELF);
        checkAttack(playerSelf);

        //TODO: Alle Schiffe getroffen? -> Ende
        break;
      }
      case STATE_WAIT_RESPONSE:
        //Schuss ist erfolgt. Warten auf das Ergebnis
        game.putData(KEY_PLAYER, PLAYER_OPPONENT);
        break;
      case STATE_RESPONSE:
        //TODO: Ergebnis pruefen -> Nochmal Schuss oder warten auf Gegener.
        //TODO: Alle Schiffe getroffen? -> Ende
        boolean hit;
        {
          Savegame savegame = game.getData(Savegame.class);
          Savegame.Record rec = savegame.getLastRecord();
          if(!Savegame.Record.RESULT.equals(rec.getKind()))
          {
            throw new IllegalStateException("last action was no attack");
          }
          String encoded = rec.getPayload();
          KeyManager km = game.getData(KeyManager.KEY_MANAGER_SELF, KeyManager.class);
          String payload = km.decode(encoded);
          String[] split = payload.split(",");
          hit = Boolean.parseBoolean(split[2]);
          game.updateState(hit ? STATE_ATTACK : STATE_WAIT_ATTACK);
        }
        game.putData(KEY_PLAYER, hit ? PLAYER_SELF : PLAYER_OPPONENT);
        break;
      case STATE_FINISHED:
        game.putData(KEY_PLAYER, "none");
        break;
    }
    //repaint();
  }

  private void checkAttack(int playerSelf)
  {
    Savegame savegame = game.getData(Savegame.class);
    Savegame.Record rec = savegame.getLastRecord();
    if(!Savegame.Record.ATTACK.equals(rec.getKind()))
    {
      throw new IllegalStateException("last action was no attack");
    }
    String encoded = rec.getPayload();
    KeyManager km = game.getData(KeyManager.KEY_MANAGER_SELF, KeyManager.class);
    String payload = km.decode(encoded);
    String[] split = payload.split(",");
    BoardData data = game.getData(BoardData.KEY_SELF, BoardData.class);
    int[] pos =
    {
      Integer.parseInt(split[0]), Integer.parseInt(split[1])
    };
    boolean hit = data.shootAt(pos[0], pos[1]);

    StatusLine.getDefault().setText(
        "Schuß auf "
        + ('A' + pos[0]) + (pos[1] + 1)
        + ", " + (hit ? "Treffer" : "Daneben"));

    KeyManager other = game.getData(KeyManager.KEY_MANAGER_OPPONENT, KeyManager.class);
    String response = payload + "," + hit;
    savegame.addRecord(Savegame.Record.RESULT, 3 - playerSelf, other.encode(response));

    if(hit)
    {
      game.updateState(STATE_WAIT_ATTACK);
    }
    else
    {
      game.updateState(STATE_ATTACK);
    }
  }
}
