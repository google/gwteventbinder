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
package com.google.web.bindery.event.gwt.rebind.binder;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes implementations of
 * {@link com.google.web.bindery.event.shared.binder.impl.AbstractEventBinder}. The
 * generated class implements the single abstract doBindEventHandlers method by
 * calling bind() for each method in the target annotated with
 * {@link EventHandler}.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
class EventBinderWriter {

  private final TreeLogger logger;
  private final JClassType genericEventType;

  EventBinderWriter(TreeLogger logger, JClassType genericEventType) {
    this.logger = logger;
    this.genericEventType = genericEventType;
  }

  void writeDoBindEventHandlers(JClassType target, SourceWriter writer, TypeOracle typeOracle)
      throws UnableToCompleteException {
    writeBindMethodHeader(writer, target.getQualifiedSourceName());
    for (JMethod method : target.getInheritableMethods()) {
      EventHandler annotation = method.getAnnotation(EventHandler.class);
      if (annotation != null) {
        writeHandlerForBindMethod(annotation, writer, method, typeOracle);
      }
    }
    writeBindMethodFooter(writer);
  }

  private void writeBindMethodHeader(SourceWriter writer, String targetName) {
    writer.println("protected List<HandlerRegistration> doBindEventHandlers("
        + "final %s target, EventBus eventBus) {",
        targetName);
    writer.indent();
    writer.println(
        "List<HandlerRegistration> registrations = new LinkedList<HandlerRegistration>();");
  }

  private void writeHandlerForBindMethod(EventHandler annotation, SourceWriter writer,
      JMethod method, TypeOracle typeOracle) throws UnableToCompleteException {
    JClassType eventParameter = null;
    if (method.getParameterTypes().length == 1) {
      eventParameter = method.getParameterTypes()[0].isClassOrInterface();
    }
    if (annotation.handles().length == 0 && !isAConcreteGenericEvent(eventParameter)) {
      logger.log(Type.ERROR, "Method " + method.getName()
          + " annotated with @EventHandler without event classes must have exactly "
          + "one argument of a concrete type assignable to GenericEvent");
      throw new UnableToCompleteException();
    }

    List<String> eventTypes = new ArrayList<String>();
    if (annotation.handles().length != 0) {
      for (Class<? extends GenericEvent> event : annotation.handles()) {
        String eventTypeName = event.getCanonicalName();
        JClassType eventClassType = typeOracle.findType(eventTypeName);
        if (eventClassType == null) {
          logger.log(Type.ERROR, "Can't resolve " + eventTypeName);
          throw new UnableToCompleteException();
        }
        if (eventParameter != null && !eventClassType.isAssignableTo(eventParameter)) {
          logger.log(Type.ERROR, "Event " + eventTypeName + " isn't assignable to "
              + eventParameter.getName() + " in method: " + method.getName());
          throw new UnableToCompleteException();
        }
        eventTypes.add(eventClassType.getQualifiedSourceName());
      }
    } else {
      eventTypes.add(eventParameter.getQualifiedSourceName());
    }

    for (String eventType : eventTypes) {
      writer.println("bind(eventBus, registrations, %s.class, new GenericEventHandler() {",
          eventType);
      if (eventParameter != null) {
        writer.indentln("public void handleEvent(GenericEvent event) { target.%s((%s) event); }",
            method.getName(), eventType);
      } else {
        writer.indentln("public void handleEvent(GenericEvent event) { target.%s(); }", 
            method.getName());
      }
      writer.println("});");
    }
  }

  private boolean isAConcreteGenericEvent(JClassType param) {
    return param != null && !param.isAbstract() && param.isAssignableTo(genericEventType);
  }

  private void writeBindMethodFooter(SourceWriter writer) {
    writer.println("return registrations;");
    writer.outdent();
    writer.println("}");
  }
}
