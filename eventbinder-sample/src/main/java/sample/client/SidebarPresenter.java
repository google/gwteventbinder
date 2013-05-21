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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;

/**
 * Presenter for the application's sidebar, which just shows a button that the
 * user can click on to load contacts.
 */
class SidebarPresenter {

  interface MyEventBinder extends EventBinder<SidebarPresenter> {}
  private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

  private final EventBus eventBus;

  SidebarPresenter(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBinder.bindEventHandlers(this, eventBus);
  }

  public void setView(HasClickHandlers view) {
    view.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg0) {
        eventBus.fireEvent(new ContactScreenOpenedEvent());
      }
    });
  }
}
