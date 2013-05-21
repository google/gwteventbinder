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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Initializes the application. Nothing to see here: everything interesting
 * happens in the presenters.
 */
public class SampleEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    // Create the object graph - a real application would use Gin
    SimpleEventBus eventBus = new SimpleEventBus();

    SidebarPresenter sidebarPresenter = new SidebarPresenter(eventBus);
    Button sidebarView = new Button("Contacts");
    sidebarView.getElement().getStyle().setFloat(Style.Float.LEFT);
    sidebarView.getElement().getStyle().setMarginRight(20, Unit.PX);
    sidebarPresenter.setView(sidebarView);
    RootPanel.get().add(sidebarView);

    ContactsPresenter contactsPresenter = new ContactsPresenter(eventBus);
    VerticalPanel contactsView = new VerticalPanel();
    contactsPresenter.setView(contactsView);
    RootPanel.get().add(contactsView);

    // Eagerly bind the server proxy
    ServerProxy server = new ServerProxy(eventBus);
  }
}
