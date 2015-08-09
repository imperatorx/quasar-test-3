package foo.quasar.test;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class MyServiceImpl implements MyService{

    @Override
    @Suspendable
    @PreAuthorize("hasPermission(#bar, 'aasdad')")
    public String foo(String bar) {
        
        try {
            Fiber.sleep(1000);
            return "After sleep on "+Fiber.currentFiber().getName();
        } catch (SuspendExecution|InterruptedException e) {
            throw new AssertionError("This should not happen",e);
        }
        
    }

    
    
}
