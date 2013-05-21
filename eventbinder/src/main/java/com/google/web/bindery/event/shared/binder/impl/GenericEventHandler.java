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

import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * The API that a {@link GenericEvent} uses to send events to generated code.
 * Instances of this class will be generated automatically when constructing an
 * EventBinder. Having a single generic handler means that we don't have to
 * define a separate handler interface for each type of event.
 * <p>
 * Instances of this class are instantiated automatically via
 * {@link EventBinder} , so users should not have to reference this class unless
 * they choose to bind events manually.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public interface GenericEventHandler extends com.google.gwt.event.shared.EventHandler {
  void handleEvent(GenericEvent event);
}
