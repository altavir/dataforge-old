/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.plots;

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.XYAdapter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * Аннотация действия может содержать несколько различных описаний рамки. При
 * выполнинии каждый набор данных помещается в ту рамку, имя которой указано в
 * параметре plot_frame_name. Если параметр не указан, используется рамка
 * по-умолчанию
 *
 * @author Alexander Nozik
 */
@TypedActionDef(name = "plotData",
        info = "Scatter plot of given DataSet", inputType = Table.class, outputType = Table.class)
@ValueDef(name = "plotFrameName", def = "default",
        info = "The name of plot frame which should be used for a plot. To be declared in the action input content rather in the action annotation.")
@ValueDef(name = "plotTitle", def = "",
        info = "The default title of plot which should be used if no frame definition found.")
@ValueDef(name = "groupBy", def = "plot_frame_name",
        info = "Defines the parameter which should be used as a frame name for this plot. The value is supposed to be a String, but in practice could be any type which could be converted to a String.")
@NodeDef(name = "plotFrame", multiple = true,
        info = "The description of plot frame", target = "class::hep.dataforge.plots.XYPlotFrame")
@NodeDef(name = "snapshot", info = "Save plot shapshots to file",
        target = "method::hep.dataforge.plots.PlotDataAction.snapshot")
@NodeDef(name = "serialize", info = "Serialize plot to file",
        target = "method::hep.dataforge.plots.PlotDataAction.serialize")
@ValueDef(name = "snapshot", type = "BOOLEAN", def = "false",
        info = "Save plot shapshots to file with default parameters")
@ValueDef(name = "serialize", type = "BOOLEAN", def = "false",
        info = "Serialize plot to file with default parameters")
@NodeDef(name = "adapter", info = "Adapter for data", target = "class::hep.dataforge.tables.XYAdapter")

public class PlotDataAction extends OneToOneAction<Table, Table> {

    private Meta findFrameDescription(Meta meta, String name) {
        //TODO сделать тут возможность подстановки стилей?
        List<? extends Meta> frameDescriptions = meta.getNodes("plotFrame");
        Meta defaultDescription = new MetaBuilder("plotFrame").build();
        for (Meta an : frameDescriptions) {
            String frameName = meta.getString("frameName");
            if ("default".equals(frameName)) {
                defaultDescription = an;
            }
            if (frameName.equals(name)) {
                return an;
            }
        }

        return defaultDescription.getBuilder().putValue("frameTitle", meta.getString("plotTitle", "")).build();
    }

    @Override
    protected Table execute(Context context, Reportable log, String name, Laminate meta, Table input) {
        //initializing plot plugin if necessary
        if (!context.provides("plots")) {
            context.loadPlugin(new PlotsPlugin());
        }
        PlotHolder holder = (PlotsPlugin) context.pluginManager().getPlugin("plots");

        PlotFrame frame;

        String groupBy = meta.getString("groupBy");
        String frame_name = meta.getString(groupBy, "default");
        if (holder.hasPlotFrame(frame_name)) {
            frame = holder.getPlotFrame(frame_name);
        } else {
            frame = holder.buildPlotFrame(frame_name, findFrameDescription(meta, frame_name));
        }
        XYAdapter adapter = new XYAdapter(meta.getNode("adapter", Meta.buildEmpty("adapter")));

        frame.add(PlottableData.plot(name, meta, input, adapter));

        if (meta.hasNode("snapshot")) {
            snapshot(context, name, frame, meta.getNode("snapshot"));
        } else if (meta.getBoolean("snapshot", false)) {
            snapshot(context, name, frame, MetaBuilder.buildEmpty("snapshot"));
        }

        if (meta.hasNode("serialize")) {
            serialize(context, name, frame, meta.getNode("serialize"));
        } else if (meta.getBoolean("serialize", false)) {
            serialize(context, name, frame, MetaBuilder.buildEmpty("serialize"));
        }

        return input;
    }

    private final Map<String, Runnable> snapshotTasks = new HashMap<>();
    private final Map<String, Runnable> serializeTasks = new HashMap<>();

    @Override
    protected void afterAction(Context context, String name, Table res, Laminate meta) {
        // это необходимо сделать, чтобы снапшоты и сериализация выполнялись после того, как все графики построены
        snapshotTasks.values().stream().forEach((r) -> r.run());
        snapshotTasks.clear();
        serializeTasks.values().stream().forEach((r) -> r.run());
        serializeTasks.clear();
        super.afterAction(context, name, res, meta);
    }

    @ValueDef(name = "width", type = "NUMBER", def = "800", info = "The width of the snapshot in pixels")
    @ValueDef(name = "height", type = "NUMBER", def = "600", info = "The height of the snapshot in pixels")
    @ValueDef(name = "name", info = "The name of snapshot file or ouputstream (provided by context). By default equals frame name.")
    private synchronized void snapshot(Context context, String plotName, PlotFrame frame, Meta snapshotCfg) {
        if (frame instanceof JFreeChartFrame) {
            JFreeChartFrame jfcFrame = (JFreeChartFrame) frame;
            String fileName = snapshotCfg.getString("name", plotName) + ".png";
            snapshotTasks.put(fileName, () -> {
                logger().info("Saving plot snapshot to file: {}", fileName);
                OutputStream stream = buildActionOutput(context, fileName);
                jfcFrame.toPNG(stream, snapshotCfg);
            });
        } else {
            logger().error("The plot frame does not provide snapshot capabilities. Ignoring 'snapshot' option.");
        }
    }

    @ValueDef(name = "name", info = "The name of serialization file or ouputstream (provided by context). By default equals frame name.")
    private synchronized void serialize(Context context, String plotName, PlotFrame frame, Meta snapshotCfg) {
        if (frame instanceof JFreeChartFrame) {
            JFreeChartFrame jfcFrame = (JFreeChartFrame) frame;
            String fileName = snapshotCfg.getString("name", plotName) + ".jfc";
            serializeTasks.put(fileName, () -> {
                logger().info("Saving serialized plot to file: {}", fileName);
                OutputStream stream = buildActionOutput(context, fileName);
                try {
                    ObjectOutputStream ostr = new ObjectOutputStream(stream);
                    ostr.writeObject(jfcFrame.getChart());
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass()).error("IO error during serialization", ex);
                }
            });

        } else {
            logger().error("The plot frame does not provide serialization capabilities.");
        }
    }
}
