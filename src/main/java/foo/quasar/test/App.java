package foo.quasar.test;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class App {

    static {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args).getBean(App.class).test();
    }

    @Autowired
    MyService myService;

    public void test() throws Exception {

        System.out.println("Outside fiber op");

        new Fiber<Void>() {

            @Override
            protected Void run() throws SuspendExecution, InterruptedException {
        
                System.out.println("Inside fiber op "+myService.getClass().getName());
                
                // set an authentication
                SecurityContextImpl ctx = new SecurityContextImpl();
                ctx.setAuthentication(new TestAuthentication());
                SecurityContextHolder.setContext(ctx);
                
                // call into secured bean
                myService.foo("bar");

                return null;
            }
        }.start().join();
        
        System.out.println("Succesfully completed");
    }
    
    @Bean
    public PermissionEvaluator permissionEvaluator(){
        return new PermissionEvaluator() {

            @Override
            public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                
                System.out.println("Permission evaluator called");
                return true;
            }

            @Override
            public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                System.out.println("Permission evaluator called");
                return true;
            }
        };
    }
}
