package com.google.web.bindery.event;

import com.google.web.bindery.event.shared.binder.EventBinderTest;
import com.google.web.bindery.event.gwt.rebind.binder.EventBinderWriterTest;

import com.google.gwt.junit.tools.GWTTestSuite;

import junit.framework.Test;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({EventBinderTest.class, EventBinderWriterTest.class})
public class TestSuite {}
