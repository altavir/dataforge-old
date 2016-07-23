/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import org.junit.Test;

/**
 *
 * @author Alexander Nozik
 */
public class AbstractGoalTest {

    @Test
    public void testComplete() throws InterruptedException {
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("canceled");
                throw new RuntimeException(ex);
            }
            System.out.println("finished");
            return "my delayed result";
        });
        future.whenComplete((res, err) -> {
            System.out.println(res);
            if (err != null) {
                System.out.println(err);
            }
        });

        future.complete("my firs result");
        future.complete("my second result");
    }

    @Test
    public void testCacel() throws InterruptedException {
        FutureTask future = new FutureTask(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                System.out.println("canceled");
                throw new RuntimeException(ex);
            }
            System.out.println("finished");
            return "my delayed result";
        });
        future.run();
        future.cancel(true);
        Thread.sleep(500);
    }
    
    
    

}
