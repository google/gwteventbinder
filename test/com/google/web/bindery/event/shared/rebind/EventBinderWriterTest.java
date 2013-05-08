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
package com.google.web.bindery.event.shared.rebind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import com.google.web.bindery.event.shared.EventHandler;
import com.google.web.bindery.event.shared.rebind.EventBinderWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
  @Mock private JClassType genericEventType;
  private EventBinderWriter writer;
  private SourceWriter output;

  @Before
  public void setUp() {
    writer = new EventBinderWriter(logger, genericEventType);
    output = new StringSourceWriter();
  }

  @Test
  public void shouldWriteDoBindEventHandler() throws Exception {
    JClassType eventType1 = newEventType("MyEvent1");
    JClassType eventType2 = newEventType("MyEvent2");
    JMethod method1 = newMethod("method1", eventType1);
    JMethod method2 = newMethod("method2", eventType2);

    when(target.getQualifiedSourceName()).thenReturn("MyTarget");
    when(target.getMethods()).thenReturn(new JMethod[] {method1, method2});

    writer.writeDoBindEventHandlers(target, output);

    assertEquals(join(
        "protected List<HandlerRegistration> doBindEventHandlers("
            + "final MyTarget target, EventBus eventBus) {",
        "  List<HandlerRegistration> registrations = new LinkedList<HandlerRegistration>();",
        "  bind(eventBus, registrations, MyEvent1.class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method1((MyEvent1) event); }",
        "  });",
        "  bind(eventBus, registrations, MyEvent2.class, new GenericEventHandler() {",
        "    public void handleEvent(GenericEvent event) { target.method2((MyEvent2) event); }",
        "  });",
        "  return registrations;",
        "}"), output.toString());
  }

  @Test
  public void shouldFailOnZeroParameters() throws Exception {
    JMethod method = newMethod("myMethod");
    when(target.getMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  @Test
  public void shouldFailOnTwoParameters() throws Exception {
    JMethod method = newMethod("myMethod", mock(JType.class), mock(JType.class));
    when(target.getMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output);
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
    when(target.getMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output);
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
    when(target.getMethods()).thenReturn(new JMethod[] {method});

    try {
      writer.writeDoBindEventHandlers(target, output);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}

    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }
  
  @Test
  public void shouldFailOnAbstractParameter() throws Exception {
    JClassType paramType = newEventType("AbstractEvent");
    when(paramType.isAbstract()).thenReturn(true);
    
    JMethod method = newMethod("myMethod", paramType);
    when(target.getMethods()).thenReturn(new JMethod[] {method});
    
    try {
      writer.writeDoBindEventHandlers(target, output);
      fail("Exception not thrown");
    } catch (UnableToCompleteException expected) {}
    
    verify(logger).log(
        eq(Type.ERROR), contains("myMethod"), isNull(Throwable.class), isNull(HelpInfo.class));
  }

  private JMethod newMethod(String name, JType... params) {
    JMethod method = mock(JMethod.class);
    when(method.getAnnotation(EventHandler.class)).thenReturn(mock(EventHandler.class));
    when(method.getName()).thenReturn(name);
    when(method.getParameterTypes()).thenReturn(params);
    return method;
  }

  private JClassType newEventType(String name) {
    JClassType type = mock(JClassType.class);
    when(type.isClassOrInterface()).thenReturn(type);
    when(type.isAssignableTo(genericEventType)).thenReturn(true);
    when(type.getQualifiedSourceName()).thenReturn(name);
    return type;
  }

  private String join(String... strings) {
    StringBuilder builder = new StringBuilder();
    for (String string : strings) {
      builder.append(string).append('\n');
    }
    return builder.toString();
  }
}
