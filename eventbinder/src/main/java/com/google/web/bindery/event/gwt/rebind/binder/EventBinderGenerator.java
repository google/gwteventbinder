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

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.GenericEvent;
import com.google.web.bindery.event.shared.binder.impl.AbstractEventBinder;
import com.google.web.bindery.event.shared.binder.impl.GenericEventHandler;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Generator for {@link EventBinder}. Takes care of the ugly parts of creating 
 * the source writer and then delegates to {@link EventBinderWriter}. This class
 * is used by the GWT compiler and should not be referenced directly by users.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventBinderGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {
    try {
      JClassType eventBinderType = context.getTypeOracle().getType(typeName);
      JClassType targetType = getTargetType(eventBinderType, context.getTypeOracle());
      SourceWriter writer = createSourceWriter(logger, context, eventBinderType, targetType);
      if (writer != null) { // Otherwise the class was already created
        new EventBinderWriter(
            logger,
            context.getTypeOracle().getType(GenericEvent.class.getCanonicalName()))
                .writeDoBindEventHandlers(targetType, writer, context.getTypeOracle());
        writer.commit(logger);
      }
      return getFullyQualifiedGeneratedClassName(eventBinderType);
    } catch (NotFoundException e) {
      logger.log(Type.ERROR, "Error generating " + typeName, e);
      throw new UnableToCompleteException();
    }
  }

  private JClassType getTargetType(JClassType interfaceType, TypeOracle typeOracle) {
    JClassType[] superTypes = interfaceType.getImplementedInterfaces();
    JClassType eventBinderType = typeOracle.findType(EventBinder.class.getCanonicalName());
    if (superTypes.length != 1
        || !superTypes[0].isAssignableFrom(eventBinderType)
        || superTypes[0].isParameterized() == null) {
      throw new IllegalArgumentException(
          interfaceType + " must extend EventBinder with a type parameter");
    }
    return superTypes[0].isParameterized().getTypeArgs()[0];
  }

  private SourceWriter createSourceWriter(
      TreeLogger logger,
      GeneratorContext context,
      JClassType eventBinderType,
      JClassType targetType) {
    String simpleName = getSimpleGeneratedClassName(eventBinderType);
    String packageName = eventBinderType.getPackage().getName();
    ClassSourceFileComposerFactory composer =
        new ClassSourceFileComposerFactory(packageName, simpleName);

    composer.setSuperclass(AbstractEventBinder.class.getCanonicalName()
        + "<" + targetType.getQualifiedSourceName() + ">");
    composer.addImplementedInterface(eventBinderType.getName());

    composer.addImport(EventBinder.class.getCanonicalName());
    composer.addImport(EventBus.class.getCanonicalName());
    composer.addImport(GenericEvent.class.getCanonicalName());
    composer.addImport(GenericEventHandler.class.getCanonicalName());
    composer.addImport(HandlerRegistration.class.getCanonicalName());
    composer.addImport(LinkedList.class.getCanonicalName());
    composer.addImport(List.class.getCanonicalName());

    PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
    return (printWriter != null) ? composer.createSourceWriter(context, printWriter) : null;
  }

  private String getSimpleGeneratedClassName(JClassType eventBinderType) {
    return eventBinderType.getName().replace('.', '_') + "Impl";
  }

  private String getFullyQualifiedGeneratedClassName(JClassType eventBinderType) {
    return new StringBuilder()
        .append(eventBinderType.getPackage().getName())
        .append('.')
        .append(getSimpleGeneratedClassName(eventBinderType))
        .toString();
  }
}
