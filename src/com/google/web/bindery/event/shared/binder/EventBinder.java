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
package com.google.web.bindery.event.shared.binder;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * API for generated code that connects an EventBus to some event handlers. Use
 * GWT.create() to get instances of this interface. The generated class will
 * search the target's methods for any annotated with {@link EventHandler} and
 * register a handler with the event bus that calls that method. The first and
 * only parameter of the annotated methods must specify the type of event to
 * handle; the method's name is ignored.
 * <p>
 * Users of this interface should define an interface which extends
 * EventBinder, and invoke bindEventHandlers on an instance of that interface in
 * the class's constructor:
 *
 * <pre>
 * interface MyEventBinder extends EventBinder&lt;MyClass&gt; {}
 * private static MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
 *
 * public MyClass(EventBus eventBus) {
 *   eventBinder.bindEventHandlers(this, eventBus);
 * }
 * 
 * {@literal @}EventHandler
 * void onContactsLoaded(ContactsLoadedEvent event) {
 *   // Interesting stuff goes here...
 * }
 * </pre>
 *
 * @param <T> type of object being bound, which should be the same as the type
 *        enclosing this interface
 * @author ekuefler@google.com (Erik Kuefler)
 */
public interface EventBinder<T> {

  /**
   * Connects an event bus to each event handler method on a target object.
   * After this method returns, whenever the event bus delivers an event, it
   * will call the handler with the same event type on the given target.
   *
   * @param target class to search for {@link EventHandler}-annotated methods
   * @param eventBus event bus on which handlers for the annotated methods
   *        should be registered
   * @return a registration that can be used to unbind all handlers registered
   *        via this call
   */
  public HandlerRegistration bindEventHandlers(T target, EventBus eventBus);
}
