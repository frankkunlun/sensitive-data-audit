package com.acme.audit.plugin.servlet;

import com.acme.audit.api.*;import com.acme.audit.core.*;import net.bytebuddy.agent.builder.AgentBuilder;import net.bytebuddy.asm.Advice;import net.bytebuddy.description.type.TypeDescription;import net.bytebuddy.dynamic.DynamicType;import net.bytebuddy.utility.JavaModule;
import java.lang.instrument.Instrumentation;import java.security.ProtectionDomain;
import static net.bytebuddy.matcher.ElementMatchers.*;

/** Instruments both javax and jakarta HttpServlet service entry points without linking servlet APIs. */
public final class ServletInstrumentationPlugin implements AuditInstrumentationPlugin {
    public String pluginId(){return "servlet";}public boolean isEnabled(AgentConfiguration c){return c.isPluginEnabled(pluginId());}
    public void install(Instrumentation inst,AgentRuntimeBridge bridge){try{new AgentBuilder.Default().ignore(nameStartsWith("com.acme.audit.").or(nameStartsWith("net.bytebuddy.")))
        .type(hasSuperType(named("javax.servlet.http.HttpServlet")).or(hasSuperType(named("jakarta.servlet.http.HttpServlet"))))
        .transform(new AgentBuilder.Transformer(){public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,TypeDescription t,ClassLoader l,JavaModule m,ProtectionDomain p){return b.visit(Advice.to(ServletAdvice.class).on(named("service").and(takesArguments(2))));}}).installOn(inst);bridge.instrumentationSucceeded(pluginId());}catch(Throwable e){bridge.instrumentationFailed(pluginId(),e);}}
    public static final class ServletAdvice {
        @Advice.OnMethodEnter(suppress=Throwable.class) public static AuditRuntime.RequestToken enter(@Advice.Argument(0) Object request){AuditRuntime r=RuntimeAccess.get();return r==null?null:r.beginRequest(request);}
        @Advice.OnMethodExit(onThrowable=Throwable.class,suppress=Throwable.class) public static void exit(@Advice.Enter AuditRuntime.RequestToken token,@Advice.Argument(1) Object response,@Advice.Thrown Throwable error){AuditRuntime r=RuntimeAccess.get();if(r!=null)r.endRequest(token,response,error);}
    }
}
