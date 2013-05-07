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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a method that handles events sent through an {@link EventBus}.
 * The method must have one parameter, which is usually a subclass of
 * {@link AbstractEvent}. The method will start receiving events after it is
 * connected to the EventBus using an {@link EventBinder}. Only the parameter of
 * the method is relevant; the name is ignored. For example, the given method
 * will be invoked whenever a ProfileLoadedEvent is fired on the event bus to
 * which the {@link EventBinder} was bound:
 *
 * <pre>
 * {@literal @}EventHandler
 * void onProfileLoaded(ProfileLoadedEvent event) {
 *   getView().setEmail(event.getProfile().getEmailAddress());
 *   getView().showSignoutMenu();
 * }
 * </pre>
 *
 * Note that an {@link EventBinder} MUST be used to register these annotations,
 * otherwise they will have no effect.
 *
 * @see EventBinder
 * @author ekuefler@google.com (Erik Kuefler)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {}
