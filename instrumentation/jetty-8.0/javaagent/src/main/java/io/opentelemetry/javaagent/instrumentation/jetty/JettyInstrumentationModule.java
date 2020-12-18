/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jetty;

import static io.opentelemetry.javaagent.tooling.ClassLoaderMatcher.hasClassesNamed;
import static io.opentelemetry.javaagent.tooling.bytebuddy.matcher.AgentElementMatchers.implementsInterface;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.tooling.InstrumentationModule;
import io.opentelemetry.javaagent.tooling.TypeInstrumentation;
import java.util.List;
import java.util.Map;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(InstrumentationModule.class)
public class JettyInstrumentationModule extends InstrumentationModule {

  public JettyInstrumentationModule() {
    super("jetty", "jetty-8.0");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return singletonList(new HandlerInstrumentation());
  }

  public static class HandlerInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<ClassLoader> classLoaderOptimization() {
      return hasClassesNamed("org.eclipse.jetty.server.Handler");
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
      // skipping built-in handlers, so that for servlets there will be no span started by jetty.
      // this is so that the servlet instrumentation will capture contextPath and servletPath
      // normally, which the jetty instrumentation does not capture since jetty doesn't populate
      // contextPath and servletPath until right before calling the servlet
      // (another option is to instrument ServletHolder.handle() to capture those fields)
      //
      // using nameStartsWith() as there are many built-in handlers, e.g.
      // org.eclipse.jetty.server.handler.HandlerWrapper
      // org.eclipse.jetty.server.handler.ScopedHandler
      // org.eclipse.jetty.server.handler.ContextHandler
      // org.eclipse.jetty.security.SecurityHandler
      // org.eclipse.jetty.servlet.ServletHandler
      return not(nameStartsWith("org.eclipse.jetty."))
          .and(implementsInterface(named("org.eclipse.jetty.server.Handler")));
    }

    @Override
    public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
      return singletonMap(
          named("handle")
              // need to capture doHandle() for handlers that extend built-in handlers excluded
              // above
              .or(named("doHandle"))
              .and(takesArgument(0, named("java.lang.String")))
              .and(takesArgument(1, named("org.eclipse.jetty.server.Request")))
              .and(takesArgument(2, named("javax.servlet.http.HttpServletRequest")))
              .and(takesArgument(3, named("javax.servlet.http.HttpServletResponse")))
              .and(isPublic()),
          JettyHandlerAdvice.class.getName());
    }
  }
}