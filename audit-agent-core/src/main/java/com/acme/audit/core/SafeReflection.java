package com.acme.audit.core;

import java.lang.reflect.Method;

final class SafeReflection {
    private SafeReflection(){}
    static Object call(Object target,String method,Object... args){
        if(target==null)return null; try { Method found=null; for(Method m:target.getClass().getMethods()){if(m.getName().equals(method)&&m.getParameterTypes().length==args.length){found=m;break;}}
            if(found==null)return null; return found.invoke(target,args); } catch(Throwable ignored){return null;}
    }
    static String text(Object target,String method,Object... args){Object v=call(target,method,args);return v==null?null:String.valueOf(v);}
    static int integer(Object target,String method,int fallback){Object v=call(target,method);return v instanceof Number?((Number)v).intValue():fallback;}
}
