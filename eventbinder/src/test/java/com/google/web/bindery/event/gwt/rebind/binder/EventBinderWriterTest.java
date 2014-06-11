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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link EventBinderWriter}. Most functionality should be covered by
 * EventBinderTest; this should only check the basic output format and error
 * cases.
 * 
 * @author ekuefler@google.com (Erik Kuefler)
 */
@RunWith(MockitoJUnitRunner.class)
public class EventBinderWriterTest {

  @Mock private JClassType target;
  @Mock private TreeLogger logger;

  private JClassType genericEventType;
  private Map<Class<? extends GenericEvent>, JClassType> eventTypes;
  private TypeOracle typeOracle;
  private EventBinderWriter writer;
  private SourceWriter output;

  @Before
  public void setUp() {
    eventTypes = new HashMap<Class<? extends GenericEvent>, JClassType>();

    typeOracle = createTypeOracle();
    genericEventType = getEventType(GenericEvent.class);
    writer = new EventBinderWriter(logger, genericEventType);
    output = new StringSourceWriter();
  }

  @Test
  public void shouldWriteDoBindEventHandler() throws Exception {
    JClassType eventType1 = getEventType(MyEvent1.class);
    JClassType eventType2 = getEventType(MyEvent2.class);
    JMethod method1 = newMethod("method1", eventType1);
    JMethod method2 = newMethod("method2", eventType2);
    JMethod method3 = newMethod("method3", new JType[] {genericEventType},
        new Class[] {MyEvent1.class, MyEvent2.class});
    JMethod method4 = newMethod("method4", new JType[] {},
        new Class[] {MyEvent1.class});

    when(target.getQualifiedSourceName()).thenReturn("MyTarget");
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method1, method2, method3, method4});

    writer.writeDoBindEventHandlers(target, output, typeOracle);

    assertEquals(join(
        "protected List<HandlerRegistration> doBindEventHandlers("
            + "final MyTarget target, EventBus eventBus) {",
        "  List<HandlerRegistration> registrations = new LinkedList<HandlerRegistration>();",
        "  bind(eventBus, registrations, " + className(MyEvent1.class) + ".class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method1((" + className(MyEvent1.class) + ") event); }",
        "  });",
        "  bind(eventBus, registrations, " + className(MyEvent2.class) +".class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method2((" + className(MyEvent2.class) + ") event); }",
        "  });",
        "  bind(eventBus, registrations, " + className(MyEvent1.class) + ".class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method3((" + className(MyEvent1.class) +") event); }",
        "  });",
        "  bind(eventBus, registrations, " + className(MyEvent2.class) + ".class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method3((" + className(MyEvent2.class) + ") event); }",
        "  });",
        "  bind(eventBus, registrations, " + className(MyEvent1.class) + ".class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method4(); }",
        "  });",
        "  return registrations;",
        "}"), output.toString());
  }

  @Test
  public void shouldFailOnZeroParametersWithoutEvents() throws Exception {
    JMethod method = newMethod("myMethod");
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output, typeOracle);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  @Test
  public void shouldFailOnTwoParameters() throws Exception {
    JMethod method = newMethod("myMethod", mock(JType.class), mock(JType.class));
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output, typeOracle);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  @Test
  public void shouldFailOnInvalidParameter() throws Exception {
    JClassType paramType = mock(JClassType.class);
    when(paramType.isAssignableTo(genericEventType)).thenReturn(false);
    when(paramType.isClassOrInterface()).thenReturn(paramType);

    JMethod method = newMethod("myMethod", paramType);
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output, typeOracle);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  @Test
  public void shouldFailForEventWhichIsNotAssignableToParameter() throws Exception {
    JClassType eventType1 = getEventType(MyEvent1.class);

    JMethod method = newMethod("myMethod", new JType[] {eventType1}, new Class[] {MyEvent2.class});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output, typeOracle);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  @Test
  public void shouldFailOnPrimitiveParameter() throws Exception {
    JClassType paramType = mock(JClassType.class);
    when(paramType.isClassOrInterface()).thenReturn(null);

    JMethod method = newMethod("myMethod", paramType);
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output, typeOracle);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }
  
  @Test
  public void shouldFailOnAbstractParameter() throws Exception {
    JClassType paramType = getEventType(AbstractEvent.class);
    when(paramType.isAbstract()).thenReturn(true);
    
    JMethod method = newMethod("myMethod", paramType);
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});
    
    try {
      writer.writeDoBindEventHandlers(target, output, typeOracle);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}
    
    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  @SuppressWarnings("unchecked")
  private JMethod newMethod(String name, JType... params) {
    return newMethod(name, params, new Class[0]);
  }

  @SuppressWarnings("unchecked")
  private JMethod newMethod(String name, JType[] params, Class[] events) {
    EventHandler eventHandler = mock(EventHandler.class);
    when(eventHandler.handles()).thenReturn(events);

    JMethod method = mock(JMethod.class);
    when(method.getAnnotation(EventHandler.class)).thenReturn(eventHandler);
    when(method.getName()).thenReturn(name);
    when(method.getParameterTypes()).thenReturn(params);
    return method;
  }

  private JClassType getEventType(Class<? extends GenericEvent> event) {
    if (eventTypes.containsKey(event)) {
      return eventTypes.get(event);
    }
    JClassType type = mock(JClassType.class);
    eventTypes.put(event, type);

    when(type.isClassOrInterface()).thenReturn(type);
    when(type.isAssignableTo(getEventType(GenericEvent.class))).thenReturn(true);
    when(type.getOracle()).thenReturn(typeOracle);
    when(type.getQualifiedSourceName()).thenReturn(className(event));
    return type;
  }

  private TypeOracle createTypeOracle() {
    TypeOracle typeOracle = mock(TypeOracle.class);
    when(typeOracle.findType(anyString())).then(new Answer<JClassType>() {
      @Override
      public JClassType answer(InvocationOnMock invocationOnMock) throws Throwable {
        String parameter = (String) invocationOnMock.getArguments()[0];
        Class<? extends GenericEvent> klass;
        try {
          klass = (Class<? extends GenericEvent>) Class.forName(parameter);
        } catch (ClassNotFoundException ex) {
          char[] klassName = parameter.toCharArray();
          klassName[parameter.lastIndexOf('.')] = '$';
          klass = (Class<? extends GenericEvent>) Class.forName(String.valueOf(klassName));
        }
        return getEventType(klass);
      }
    });
    return typeOracle;
  }

  private String join(String... strings) {
    StringBuilder builder = new StringBuilder();
    for (String string : strings) {
      builder.append(string).append('\n');
    }
    return builder.toString();
  }

  private String className(Class<? extends GenericEvent> event) {
    return event.getName().replace('$', '.');
  }

  public static class MyEvent1 extends GenericEvent {}

  public static class MyEvent2 extends GenericEvent {}

  public static abstract class AbstractEvent extends GenericEvent {}
}
