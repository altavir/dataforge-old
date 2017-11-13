package hep.dataforge.workspace;

import hep.dataforge.cache.CachePlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.workspace.tasks.PipeTask;
import hep.dataforge.workspace.tasks.Task;
import hep.dataforge.workspace.tasks.TaskModel;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class WorkspaceTest {
    private Workspace wsp;
    private AtomicInteger counter = new AtomicInteger();

    {
        Context context = Global.getContext("TEST");
//        context.getPluginManager().load(BasicIOManager.class);

        CachePlugin cache = context.loadFeature("cache", CachePlugin.class);
        cache.configureValue("fileCache.enabled", false);

        Task<Integer> task1 = new PipeTask<Integer, Integer>("test1", Integer.class, Integer.class) {
            @Override
            protected void buildModel(TaskModel.Builder model, Meta meta) {
                model.data("*");
            }

            @Override
            protected Integer result(Context context, String name, Integer input, Laminate meta) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter.incrementAndGet();
                return input + meta.getInt("a", 2);
            }
        };

        Task<Integer> task2 = new PipeTask<Integer, Integer>("test2", Integer.class, Integer.class) {
            @Override
            protected void buildModel(TaskModel.Builder model, Meta meta) {
                model.dependsOn("test1", meta);
            }

            @Override
            protected Integer result(Context context, String name, Integer input, Laminate meta) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter.incrementAndGet();
                return input * meta.getInt("b", 2);
            }
        };


        wsp = new BasicWorkspace.Builder()
                .setContext(context)
                .staticData("data_1", 1)
                .staticData("data_2", 2)
                .staticData("data_3", 3)
                .task(task1)
                .task(task2)
                .build();
    }


    @Test(timeout = 900)
    public void testExecution() throws Exception {
        DataNode<?> res = wsp.runTask("test2", Meta.empty());
        res.computeAll();
        assertEquals(6, res.getCheckedData("data_1", Integer.class).get().longValue());
        assertEquals(8, res.getCheckedData("data_2", Integer.class).get().longValue());
        assertEquals(10, res.getCheckedData("data_3", Integer.class).get().longValue());
    }

    @Test
    public void testCaching() throws Exception {
        counter.set(0);
        DataNode<?> res = wsp.runTask("test2", Meta.empty()).computeAll();
        res = wsp.runTask("test2", Meta.empty()).computeAll();
        assertEquals(6, counter.get());
        assertEquals(
                4,
                wsp.runTask(
                        "test2",
                        new MetaBuilder().putValue("a", 0)).getCheckedData("data_2", Integer.class
                ).get().longValue()
        );
    }
}