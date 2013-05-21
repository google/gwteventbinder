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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

/**
 * Presenter for the main contacts screen, which shows a list of contacts once
 * they've been loaded from the server.
 */
class ContactsPresenter {

  interface MyEventBinder extends EventBinder<ContactsPresenter> {}
  private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

  private HasWidgets view;

  ContactsPresenter(EventBus eventBus) {
    eventBinder.bindEventHandlers(this, eventBus);
  }

  void setView(HasWidgets view) {
    this.view = view;
  }

  @EventHandler
  void onContactsScreenOpened(ContactScreenOpenedEvent event) {
    view.clear();
    view.add(new Label("Please wait..."));
  }

  @EventHandler
  void onContactsLoaded(ContactsLoadedEvent event) {
    view.clear();
    for (String contactName : event.getContactNames()) {
      view.add(new Label(contactName));
    }
  }
}
