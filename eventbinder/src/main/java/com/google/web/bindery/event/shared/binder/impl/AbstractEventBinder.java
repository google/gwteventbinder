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
package com.google.web.bindery.event.shared.binder.impl;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import java.util.List;

/**
 * Base class containing code shared by all generated {@link EventBinder}s.
 * Users shouldn't need to reference this class directly.
 *
 * @param <T> type of object being bound
 * @author ekuefler@google.com (Erik Kuefler)
 */
public abstract class AbstractEventBinder<T> implements EventBinder<T> {

  @Override
  public final HandlerRegistration bindEventHandlers(T target, EventBus eventBus) {
    final List<HandlerRegistration> registrations = doBindEventHandlers(target, eventBus);
    return new HandlerRegistration() {
      @Override
      public void removeHandler() {
        for (HandlerRegistration registration : registrations) {
          registration.removeHandler();
        }
        registrations.clear();
      }
    };
  }

  /**
   * Implemented by EventBinderGenerator to do the actual work of binding event handlers on the
   * target.
   */
  protected abstract List<HandlerRegistration> doBindEventHandlers(T target, EventBus eventBus);

  /**
   * Registers the given handler for the given event class on the given event bus. Factored out
   * into a method here instead of generated directly in order to simplify the generated code and
   * save a little space.
   */
  protected final <U extends GenericEvent> void bind(
      EventBus eventBus,
      List<HandlerRegistration> registrations,
      Class<U> type,
      GenericEventHandler handler) {
    registrations.add(eventBus.addHandler(GenericEventType.getTypeOf(type), handler));
  }
}
