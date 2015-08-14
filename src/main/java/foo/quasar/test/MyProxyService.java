package foo.quasar.test;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@SuspendableProxyClass
public class MyProxyService {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @PreAuthorize("hasPermission(#bar, 'aasdad')")
    @Suspendable
    public String myMethod(String bar) {

        log.info("----------------------- DEBUG STACK TRACE ---------------------");

        for (StackTraceElement ste : new RuntimeException("TEST ERROR").fillInStackTrace().getStackTrace()) {
            log.info("STACK -> {}", ste.toString());
        }

        log.info("----------------------- DEBUG STACK TRACE ---------------------");

        try {
            Fiber.sleep(1000);
        } catch (InterruptedException | SuspendExecution ex) {
            throw new AssertionError();
        }

        return "After sleep on " + Fiber.currentFiber().getName();
    }
}
