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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.binder.subpackage.SomeActivity;

/**
 * End-to-end test of {@link EventBinder} and associated classes.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventBinderTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "com.google.web.bindery.event.EventBinder";
  }

  public void testEventBinder() {
    EventBus eventBus = new SimpleEventBus();
    TestPresenter presenter = new TestPresenter();
    TestPresenter.MyEventBinder binder = GWT.create(TestPresenter.MyEventBinder.class);
    binder.bindEventHandlers(presenter, eventBus);

    // Test one event
    assertEquals(0, presenter.firstEventsHandled);
    eventBus.fireEvent(new FirstEvent());
    assertEquals(1, presenter.firstEventsHandled);
    assertEquals(1, presenter.firstEventsWithoutParameterHandled);
    assertEquals(1, presenter.firstAndSecondEventsHandled);

    // Test another event twice
    assertEquals(0, presenter.secondEventsHandled);
    eventBus.fireEvent(new SecondEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(2, presenter.secondEventsHandled);
    assertEquals(3, presenter.firstAndSecondEventsHandled);
  }

  public void testEventBinder_unbindEventHandlers() {
    EventBus eventBus = new SimpleEventBus();
    TestPresenter presenter = new TestPresenter();
    TestPresenter.MyEventBinder binder = GWT.create(TestPresenter.MyEventBinder.class);
    HandlerRegistration registration = binder.bindEventHandlers(presenter, eventBus);
    assertEquals(0, presenter.firstEventsHandled);
    assertEquals(0, presenter.firstEventsWithoutParameterHandled);
    assertEquals(0, presenter.secondEventsHandled);

    // Before unregistering
    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(1, presenter.firstEventsHandled);
    assertEquals(1, presenter.firstEventsWithoutParameterHandled);
    assertEquals(1, presenter.secondEventsHandled);
    assertEquals(2, presenter.firstAndSecondEventsHandled);

    // After unregistering
    registration.removeHandler();
    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(1, presenter.firstEventsHandled);
    assertEquals(1, presenter.firstEventsWithoutParameterHandled);
    assertEquals(1, presenter.secondEventsHandled);
    assertEquals(2, presenter.firstAndSecondEventsHandled);

    // After re-registering
    binder.bindEventHandlers(presenter, eventBus);
    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    assertEquals(2, presenter.firstEventsHandled);
    assertEquals(2, presenter.firstEventsWithoutParameterHandled);
    assertEquals(2, presenter.secondEventsHandled);
    assertEquals(4, presenter.firstAndSecondEventsHandled);
  }

  public void testEventBinder_withLegacyEventBus() {
    com.google.gwt.event.shared.EventBus eventBus =
        new com.google.gwt.event.shared.SimpleEventBus();
    TestPresenter presenter = new TestPresenter();
    TestPresenter.MyEventBinder binder = GWT.create(TestPresenter.MyEventBinder.class);
    binder.bindEventHandlers(presenter, eventBus);

    assertEquals(0, presenter.firstEventsHandled);
    eventBus.fireEvent(new FirstEvent());
    assertEquals(1, presenter.firstEventsHandled);
  }

  public void testEventBinder_withHandlersInSuperclass() {
    EventBus eventBus = new SimpleEventBus();
    SubPresenter presenter = new SubPresenter();
    SubPresenter.MyEventBinder binder = GWT.create(SubPresenter.MyEventBinder.class);
    binder.bindEventHandlers(presenter, eventBus);

    eventBus.fireEvent(new FirstEvent());
    eventBus.fireEvent(new SecondEvent());
    eventBus.fireEvent(new ThirdEvent());

    // FirstEvent has a handler in both classes, so it should be handled twice
    assertEquals(1, presenter.firstEventsHandled);
    assertEquals(1, presenter.firstEventsWithoutParameterHandled);
    assertEquals(1, presenter.subclassFirstEventsHandled);

    // SecondEvent's handler is overridden in the subclass, so it should only be handled there
    assertEquals(0, presenter.secondEventsHandled);
    assertEquals(1, presenter.subclassSecondEventsHandled);

    // ThirdEvent is only handled in the superclass
    assertEquals(1, presenter.thirdEventsHandled);

    // First+Second events are handled in superclass
    assertEquals(2, presenter.firstAndSecondEventsHandled);
  }

  // https://github.com/google/gwteventbinder/issues/28
  public void testEventBinder_inDifferentPackage() {
    EventBus eventBus = new SimpleEventBus();
    TestPresenter presenter = new TestPresenter();
    SomeActivity.SomeEventBinder binder = GWT.create(SomeActivity.SomeEventBinder.class);
    binder.bindEventHandlers(presenter, eventBus);

    // Test one event
    assertEquals(0, presenter.firstEventsHandled);
    eventBus.fireEvent(new FirstEvent());
    assertEquals(1, presenter.firstEventsHandled);
  }

  public static class TestPresenter {
    interface MyEventBinder extends EventBinder<TestPresenter> {}

    int firstEventsHandled;
    int secondEventsHandled;
    int thirdEventsHandled;
    int firstAndSecondEventsHandled;
    int firstEventsWithoutParameterHandled;

    @EventHandler
    public void onFirstEvent(FirstEvent e) {
      firstEventsHandled++;
    }

    @EventHandler
    public void onSecondEvent(SecondEvent e) {
      secondEventsHandled++;
    }

    @EventHandler
    public void onThirdEvent(ThirdEvent e) {
      thirdEventsHandled++;
    }

    @EventHandler(handles = {FirstEvent.class, SecondEvent.class})
    public void onFirstAndSecondEvent(GenericEvent event) {
      firstAndSecondEventsHandled++;
    }

    @EventHandler(handles = {FirstEvent.class})
    public void onFirstEventWithoutParameter() {
        firstEventsWithoutParameterHandled++;
    }
  }

  static class SubPresenter extends TestPresenter {
    interface MyEventBinder extends EventBinder<SubPresenter> {}

    int subclassFirstEventsHandled;
    int subclassSecondEventsHandled;

    @EventHandler
    void onFirstEventAgain(FirstEvent e) {
      subclassFirstEventsHandled++;
    }

    @Override
    @EventHandler
    public void onSecondEvent(SecondEvent e) {
      subclassSecondEventsHandled++;
    }
  }

  public static class FirstEvent extends GenericEvent {}
  public static class SecondEvent extends GenericEvent {}
  public static class ThirdEvent extends GenericEvent {}
}
