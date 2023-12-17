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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

/**
 *
 * @author nigjo
 */
public class KeyManager
{
  private PrivateKey own;
  private PrivateKey playerKey;

  /**
   * Erstellt einen neuen Schlüssel
   */
  public KeyManager(Path privateStore)
  {
  }

  private void loadFromPrivate(Path privateStore)
  {
  }

  private void generateNew(Path privateStore)
  {
    try
    {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(128);
      KeyPair pair = gen.generateKeyPair();
      PublicKey publicKey = pair.getPublic();
      byte[] encoded = publicKey.getEncoded();
      String playerKey = Base64.getEncoder().encodeToString(encoded);

      KeyFactory.getInstance("RSA");

    }
    catch(NoSuchAlgorithmException ex)
    {
      throw new IllegalStateException(ex);
    }
  }

  public KeyManager(String storedKey)
  {
  }

  public String encode(String message)
  {
    try
    {
      Cipher encryptCipher = Cipher.getInstance("RSA");
      encryptCipher.init(Cipher.ENCRYPT_MODE, playerKey);
      byte[] sourceMessage = message.getBytes(StandardCharsets.UTF_8);
      byte[] data = encryptCipher.doFinal(sourceMessage);
      return Base64.getEncoder().encodeToString(data);
    }
    catch(GeneralSecurityException ex)
    {
      Logger.getLogger(KeyManager.class.getName()).log(Level.SEVERE, null, ex);
      return message;
    }
  }
}
