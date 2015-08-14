package foo.quasar.test;

import co.paralleluniverse.fibers.instrument.MethodDatabase;
import co.paralleluniverse.fibers.instrument.SuspendableClassifier;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringClassifier implements SuspendableClassifier {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    Pattern cglibMethodPattern = Pattern.compile("CGLIB\\$([^\\$]+)\\$.*");

    Pattern methodProviderProxyPattern =        Pattern.compile("^[/a-zA-Z0-9]+\\$\\$EnhancerBySpringCGLIB\\$\\$[a-z0-9]+$");
    Pattern invokationProviderProxyPattern =    Pattern.compile("^[/a-zA-Z0-9]+\\$\\$FastClassBySpringCGLIB\\$\\$[a-z0-9]+$");

    String[][] suspendableSupers = {
        {"org/aopalliance/intercept/Joinpoint", "proceed"},
        {"org/aopalliance/intercept/MethodInterceptor", "invoke"},
        {"org/springframework/cglib/reflect/FastClass", "invoke"},
        {"org/springframework/cglib/proxy/MethodInterceptor", "intercept"},};

    String[][] suspendables = {
        {"org/springframework/aop/framework/JdkDynamicAopProxy", "invoke"},
        {"org/springframework/security/access/intercept/AbstractSecurityInterceptor", "credentialsNotFound", "beforeInvocation"},
        {"org/springframework/security/access/intercept/aopalliance/MethodSecurityInterceptor", "invoke"},
        {"org/springframework/aop/framework/ReflectiveMethodInvocation", "proceed", "invokeJoinpoint"},
        {"org/springframework/aop/framework/CglibAopProxy$DynamicAdvisedInterceptor", "intercept"},
        {"org/springframework/aop/framework/CglibAopProxy$CglibMethodInvocation", "invokeJoinpoint"},
        {"org/springframework/aop/support/AopUtils", "invokeJoinpointUsingReflection"},
        {"org/springframework/cglib/proxy/MethodProxy", "invoke"},
        {"org/springframework/cglib/reflect/FastClass", "invoke"},
    };

    @Override
    public MethodDatabase.SuspendableType isSuspendable(
            MethodDatabase db,
            String sourceName, String sourceDebugInfo,
            boolean isInterface, String className, String superClassName, String[] interfaces,
            String methodName, String methodDesc, String methodSignature, String[] methodExceptions
    ) {
        
        
        for (String[] susExtendables : suspendableSupers) {

            if (className.equals(susExtendables[0])) {
                for (int i = 1; i < susExtendables.length; i++) {
                    if (methodName.matches(susExtendables[i])) {
                        return MethodDatabase.SuspendableType.SUSPENDABLE_SUPER;
                    }
                }

            }
        }

        for (String[] susExtendables : suspendables) {

            if (className.equals(susExtendables[0])) {
                for (int i = 1; i < susExtendables.length; i++) {
                    if (methodName.matches(susExtendables[i])) {
                        return MethodDatabase.SuspendableType.SUSPENDABLE;
                    }
                }
            }
        }

        if (superClassName != null && (invokationProviderProxyPattern.matcher(className).matches()
                || methodProviderProxyPattern.matcher(className).matches())) {

            int end = className.indexOf("$$");

            if (end > 0) {

                String unproxiedClassName = className.substring(0, end);

                Class clazz = null;

                try {
                    clazz = Class.forName(unproxiedClassName.replaceAll("/", "."));
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException("Unable to check class " + unproxiedClassName, ex);
                }

                if (clazz.isAnnotationPresent(SuspendableProxyClass.class)) {

                    MethodDatabase.ClassEntry ce = db.getOrLoadClassEntry(unproxiedClassName);

                    if (ce != null && ce.isInstrumented()) {

                        String interfacess = Arrays.asList(interfaces).stream().collect(Collectors.joining(" | "));

                        if (methodName.equals("invoke") && "org/springframework/cglib/reflect/FastClass".equals(superClassName)) {
                            log.info("--> Enabling proxy invocation of FastClass method {}.{} SUPER {} INTERFACES:{}", className, methodName, superClassName, interfacess);
                            return MethodDatabase.SuspendableType.SUSPENDABLE;
                        } else if (db.isMethodSuspendable(unproxiedClassName, methodName, methodDesc, 0) == MethodDatabase.SuspendableType.SUSPENDABLE) {
                            log.info("--> Enabling proxy invocation {}.{}. SUPER {} INTERFACES:{}", className, methodName, superClassName, interfacess);
                            return MethodDatabase.SuspendableType.SUSPENDABLE;
                        } else {
                            log.debug("Will not instrument {}.{}", className, methodName);
                        }
                    }
                } else {
                    log.trace("Not a marked class, will not instrument {}.{}", className, methodName);
                }

            }
        }

        return null;
    }

}
