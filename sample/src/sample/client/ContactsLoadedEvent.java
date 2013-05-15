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

import com.google.web.bindery.event.shared.binder.GenericEvent;

import java.util.List;

/**
 * Event fired when contacts have been loaded from the server and are available
 * to the client.
 */
public class ContactsLoadedEvent extends GenericEvent {

  private final List<String> contactNames;

  public ContactsLoadedEvent(List<String> contactNames) {
    this.contactNames = contactNames;
  }

  public List<String> getContactNames() {
    return contactNames;
  }
}
