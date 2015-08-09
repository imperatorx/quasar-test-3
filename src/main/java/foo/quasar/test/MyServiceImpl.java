package foo.quasar.test;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class MyServiceImpl implements MyService {
    @Override
    @PreAuthorize("hasPermission(#bar, 'aasdad')")
    public String foo(String bar) throws SuspendExecution, InterruptedException {
        Fiber.sleep(1000);
        return "After sleep on "+Fiber.currentFiber().getName();
    }
}
