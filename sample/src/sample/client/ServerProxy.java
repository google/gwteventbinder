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
package sample.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import java.util.LinkedList;

/**
 * Proxy for a contacts server. This proxy stands in on the event bus on behalf
 * of the server, converting events to (fake) server calls, and firing other
 * events after the server returns information.
 */
class ServerProxy {

  interface MyEventBinder extends EventBinder<ServerProxy> {}
  private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

  private final EventBus eventBus;

  ServerProxy(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBinder.bindEventHandlers(this, eventBus);
  }

  @EventHandler
  void onContactsScreenOpened(ContactScreenOpenedEvent event) {
    // Pretend to make a server request
    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      @Override
      public boolean execute() {
        LinkedList<String> results = new LinkedList<String>();
        results.add("John Doe");
        results.add("Jane Doe");
        eventBus.fireEvent(new ContactsLoadedEvent(results));
        return false;
      }
    }, 1000);
  }
}
