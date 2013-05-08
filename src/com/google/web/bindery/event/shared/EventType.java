/*
 * Copyright 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.web.bindery.event.shared;

import com.google.gwt.event.shared.GwtEvent.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around GWT's event type that lets us write
 * {@literal EventType<SomeEvent>} rather than
 * {@literal Type<GenericEventHandler<SomeEvent>>}. Users shouldn't need to
 * reference this class directly.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventType<T extends GenericEvent<T>> extends Type<GenericEventHandler<T>> {

  private static final Map<Class<?>, EventType<?>> TYPE_MAP = new HashMap<Class<?>, EventType<?>>();

  /**
   * Creates a new EventType for the given event class. Repeated invocations of
   * this method for the same type will return the same object. This method is
   * called by generated {@link EventBinder}s and shouldn't normally have to be
   * called directly by users.
   */
  public static <T extends GenericEvent<T>> EventType<T> getTypeOf(Class<T> clazz) {
    if (!TYPE_MAP.containsKey(clazz)) {
      TYPE_MAP.put(clazz, new EventType<T>());
    }

    // We guarantee that the value in the map is a class corresponding to the string key
    @SuppressWarnings("unchecked")
    EventType<T> type = (EventType<T>) TYPE_MAP.get(clazz);
    return type;
  }

  private EventType() {}
}
