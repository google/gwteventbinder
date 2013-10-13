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
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.web.bindery.event.shared.binder.EventHandler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

  void writeDoBindEventHandlers(JClassType target, SourceWriter writer)
      throws UnableToCompleteException {
    writeBindMethodHeader(writer, target.getQualifiedSourceName());

    // Keep track of the methods we've written handlers for so that we don't write them multiple
    // times when traversing superclasses with overridden methods.
    Set<MethodSignature> signaturesWritten = new HashSet<MethodSignature>();

    // Step through the class and its ancestors, writing a handler for each new method encountered
    JClassType clazz = target;
    do {
      for (JMethod method : clazz.getMethods()) {
        MethodSignature sig = new MethodSignature(method);
        if (method.getAnnotation(EventHandler.class) != null && !signaturesWritten.contains(sig)) {
          writeHandlerForBindMethod(writer, method);
          signaturesWritten.add(sig);
        }
      }
    } while ((clazz = clazz.getSuperclass()) != null);

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

  private void writeHandlerForBindMethod(SourceWriter writer, JMethod method)
      throws UnableToCompleteException {
    if (method.getParameterTypes().length != 1
        || method.getParameterTypes()[0].isClassOrInterface() == null
        || method.getParameterTypes()[0].isClassOrInterface().isAbstract()
        || !method.getParameterTypes()[0].isClassOrInterface().isAssignableTo(genericEventType)) {
      logger.log(Type.ERROR, "Method " + method.getName()
          + " annotated with @EventHandler must have exactly one argument of a concrete type "
          + "assignable to GenericEvent");
      throw new UnableToCompleteException();
    }
    String eventType = method.getParameterTypes()[0].getQualifiedSourceName();
    writer.println("bind(eventBus, registrations, %s.class, new GenericEventHandler() {",
        eventType);
    writer.indentln("public void handleEvent(GenericEvent event) { target.%s((%s) event); }",
        method.getName(), eventType);
    writer.println("});");
  }

  private void writeBindMethodFooter(SourceWriter writer) {
    writer.println("return registrations;");
    writer.outdent();
    writer.println("}");
  }

  /** Holds a method's name and parameter type, allowing them to be compared to check duplicates. */
  private static class MethodSignature {
    final String name;
    final List<String> params;

    MethodSignature(JMethod method) {
      name = method.getName();
      params = new LinkedList<String>();
      for (JType paramType : method.getParameterTypes()) {
        params.add(paramType.getQualifiedSourceName());
      }
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MethodSignature)) {
        return false;
      }
      MethodSignature other = (MethodSignature) obj;
      return name.equals(other.name) && params.equals(other.params);
    }
  }
}
