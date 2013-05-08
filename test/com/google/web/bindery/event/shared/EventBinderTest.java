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

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * End-to-end test of {@link EventBinder} and associated classes.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventBinderTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "com.google.web.bindery.event.shared.EventBinder";
  }

  public void testEventBinder() {
    EventBus eventBus = new SimpleEventBus();
    TestPresenter presenter = new TestPresenter(eventBus);

    // Test one event
    assertEquals(0, presenter.firstEventsHandled);
    eventBus.fireEvent(new FirstEvent());
    assertEquals(1, presenter.firstEventsHandled);

    // Test another event twice
    assertEquals(0, presenter.secondEventsHandled);
    eventBus.fireEvent(new SecondEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(2, presenter.secondEventsHandled);
  }

  public void testEventBinder_unbindEventHandlers() {
    EventBus eventBus = new SimpleEventBus();
    TestPresenter presenter = new TestPresenter(eventBus);
    assertEquals(0, presenter.firstEventsHandled);
    assertEquals(0, presenter.secondEventsHandled);

    // Before unregistering
    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(1, presenter.firstEventsHandled);
    assertEquals(1, presenter.secondEventsHandled);

    // After unregistering
    presenter.handlerRegistration.removeHandler();
    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(1, presenter.firstEventsHandled);
    assertEquals(1, presenter.secondEventsHandled);

    // After re-registering
    presenter.eventBinder.bindEventHandlers(presenter, eventBus);
    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(2, presenter.firstEventsHandled);
    assertEquals(2, presenter.secondEventsHandled);
  }

  public void testEventBinder_withLegacyEventBus() {
    com.google.gwt.event.shared.EventBus eventBus =
        new com.google.gwt.event.shared.SimpleEventBus();
    TestPresenter presenter = new TestPresenter(eventBus);

    assertEquals(0, presenter.firstEventsHandled);
    eventBus.fireEvent(new FirstEvent());
    assertEquals(1, presenter.firstEventsHandled);
  }

  interface MyEventBinder extends EventBinder<TestPresenter> {}

  static class TestPresenter {
    private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

    private final HandlerRegistration handlerRegistration;

    private int firstEventsHandled;
    private int secondEventsHandled;

    public TestPresenter(EventBus eventBus) {
      handlerRegistration = eventBinder.bindEventHandlers(this, eventBus);
    }

    @EventHandler
    void onFirstEvent(FirstEvent e) {
      firstEventsHandled++;
    }

    @EventHandler
    void onSecondEvent(SecondEvent e) {
      secondEventsHandled++;
    }
  }

  static class FirstEvent extends GenericEvent {}
  static class SecondEvent extends GenericEvent {}
}
