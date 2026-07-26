package com.acme.audit.plugin.security;

import com.acme.audit.api.*;import java.lang.reflect.Method;import java.util.*;

/** Reflection-only resolver so the agent never requires Spring Security on the application class path. */
public final class SpringSecurityUserResolver implements UserContextResolver {
    public int order(){return 100;}
    public UserContext resolve(Object request){try{ClassLoader cl=Thread.currentThread().getContextClassLoader();Class<?> holder=Class.forName("org.springframework.security.core.context.SecurityContextHolder",false,cl);Object context=holder.getMethod("getContext").invoke(null);Object auth=call(context,"getAuthentication");if(auth==null||!Boolean.TRUE.equals(call(auth,"isAuthenticated")))return null;String name=String.valueOf(call(auth,"getName"));if("anonymousUser".equals(name))return null;Set<String> roles=new LinkedHashSet<String>();Object authorities=call(auth,"getAuthorities");if(authorities instanceof Iterable)for(Object a:(Iterable<?>)authorities){Object role=call(a,"getAuthority");if(role!=null)roles.add(String.valueOf(role));}return new UserContext(name,name,null,roles,true);}catch(Throwable ignored){return null;}}
    private static Object call(Object target,String name)throws Exception{Method m=target.getClass().getMethod(name);return m.invoke(target);}
}
