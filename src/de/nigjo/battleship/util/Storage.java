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
package de.nigjo.battleship.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author nigjo
 */
public class Storage
{
  private final Map<String, Object> data;

  private static Storage instance;

  public static Storage getDefault()
  {
    if(instance == null)
    {
      instance = new Storage();
    }
    return instance;
  }

  public Storage()
  {
    this.data = new HashMap<>();
  }

  public void put(String key, Object value)
  {
    if(value == null)
    {
      data.remove(key);
    }
    else
    {
      data.put(key, value);
    }
  }

  public String getString(String key)
  {
    return get(key, String.class);
  }

  public int getInt(String key, int def)
  {
    Integer val = get(key, Integer.class);
    if(val == null)
    {
      return def;
    }
    return val;
  }

  public boolean getBoolean(String key, boolean def)
  {
    Boolean val = get(key, Boolean.class);
    if(val == null)
    {
      return def;
    }
    return val;
  }

  public <T> T get(String key, Class<T> type)
  {
    return type.cast(data.get(key));
  }

  public <T> T get(String key, Class<T> type, Supplier<T> def)
  {
    Object value = data.get(key);
    if(value == null)
    {
      return def.get();
    }
    return type.cast(value);
  }

  public <T> T getOrSet(String key, Class<T> type, Supplier<T> def)
  {
    Object value = data.get(key);
    if(value == null)
    {
      T defVal = def.get();
      if(defVal != null)
      {
        data.put(key, defVal);
      }
      return defVal;
    }
    return type.cast(value);
  }

  public <T> T get(Class<T> type)
  {
    return get(type.getName(), type);
  }

  public <T> Optional<T> find(Class<T> type)
  {
    T val = get(type);
    if(val == null)
    {
      return Optional.empty();
    }
    else
    {
      return Optional.of(val);
    }
  }

  public <T> Collection<T> getAll(Class<T> type)
  {
    return data.values().stream()
        .filter(type::isInstance)
        .map(type::cast)
        .collect(Collectors.toUnmodifiableSet());
  }
}
