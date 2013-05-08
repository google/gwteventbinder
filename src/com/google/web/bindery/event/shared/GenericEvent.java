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

import com.google.gwt.event.shared.GwtEvent;

/**
 * Base class for all events fired on the event bus. Subclasses of this can be
 * listened for in presenters using the {@link EventHandler} annotation.
 * <p>
 * Events (subclasses of this class) should be very simple and immutable value
 * types. In the simplest case for an event that takes no arguments, the body of
 * the class can be entirely blank. In this case, the event is effectively just
 * a tag that is never referenced directly after it is fired.
 * <p>
 * In the slightly more complex case, events can take any number of arguments in
 * their constructor. These arguments should be assigned to final fields for
 * which public accessors should be exposed. Handlers can then access the
 * arguments via the public methods. Events should rarely contains more logic
 * than this and MUST be immutable. Since the same event is passed to each
 * handler and the order in which the handlers will see the events is undefined,
 * mutable events are very dangerous.
 * <p>
 * A complete example of a single-argument event is shown below:
 * <pre>
 * public class ContactsLoadedEvent extends GenericEvent&lt;ContactsLoadedEvent&gt; {
 *
 *   private final List&lt;Contacts&gt; contacts;
 *
 *   public PurchaseActionLoadedEvent(List&lt;Contacts&gt; contacts) {
 *     this.contacts = contacts;
 *   }
 *
 *   public List&lt;Contacts&gt; getContacts() {
 *     return contacts;
 *   }
 * }
 * </pre>
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public abstract class GenericEvent<T extends GenericEvent<T>>
    extends GwtEvent<GenericEventHandler<T>> {

  @Override
  public EventType<T> getAssociatedType() {
    // Since T extends GenericEvent<T>, this cast will always be safe.
    @SuppressWarnings("unchecked")
    Class<T> subClass = (Class<T>) getSubtype().getClass();
    return EventType.getTypeOf(subClass);
  }

  @Override
  protected void dispatch(GenericEventHandler<T> handler) {
    handler.handleEvent(getSubtype());
  }

  private T getSubtype() {
    // Since T extends GenericEvent<T>, this cast will always be safe.
    @SuppressWarnings("unchecked")
    T subType = (T) this;
    return subType;
  }
}
