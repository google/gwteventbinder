package com.google.web.bindery.event.shared.binder.subpackage;

import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventBinderTest;

/** This class exists to test that EventBinder works when referencing a class in a different package. */
public class SomeActivity {
  public interface SomeEventBinder extends EventBinder<EventBinderTest.TestPresenter> {}
}
