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
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.DataPlot;
import hep.dataforge.tables.Adapters;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.ValuesAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@NodeDef(name = "plotFrame", multiple = true,
        info = "The description of plot frame", from = "class::hep.dataforge.plots.XYPlotFrame")
//@NodeDef(name = "snapshot", info = "Save plot shapshots to file",
//        target = "method::hep.dataforge.plots.PlotDataAction.snapshot")
//@NodeDef(name = "serialize", info = "Serialize plot to file",
//        target = "method::hep.dataforge.plots.PlotDataAction.serialize")
//@ValueDef(name = "snapshot", type = "BOOLEAN", def = "false",
//        info = "Save plot shapshots to file with default parameters")
//@ValueDef(name = "serialize", type = "BOOLEAN", def = "false",
//        info = "Serialize plot to file with default parameters")
@NodeDef(name = "adapter", info = "Adapter for data")

public class PlotDataAction extends OneToOneAction<Table, Table> {

    private final Map<String, Runnable> snapshotTasks = new HashMap<>();
    private final Map<String, Runnable> serializeTasks = new HashMap<>();

    private Meta findFrameDescription(Meta meta, String name) {
        //TODO сделать тут возможность подстановки стилей?
        List<? extends Meta> frameDescriptions = meta.getMetaList("plotFrame");
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

        return defaultDescription.getBuilder().putValue("title", meta.getString("plotTitle", "")).build();
    }

    @Override
    protected Table execute(Context context, String name, Table input, Laminate meta) {
        //initializing plot plugin if necessary
        PlotPlugin holder = PlotUtils.getPlotManager(context);

        PlotFrame frame;

        String groupBy = meta.getString("groupBy");
        String frame_name = meta.getString(groupBy, "default");
        if (holder.hasPlotFrame(frame_name)) {
            frame = holder.getPlotFrame(frame_name);
        } else {
            frame = holder.getPlotFrame(frame_name, findFrameDescription(meta, frame_name));
        }
        ValuesAdapter adapter = Adapters.buildAdapter(meta.getMeta("adapter", Meta.empty()));

        DataPlot plottableData = DataPlot.plot(name, adapter, input);
        plottableData.configure(meta);
        frame.add(plottableData);

//        if (meta.hasMeta("snapshot")) {
//            snapshot(name, frame, meta.getMeta("snapshot"));
//        } else if (meta.getBoolean("snapshot", false)) {
//            snapshot(name, frame, MetaBuilder.buildEmpty("snapshot"));
//        }
//
//        if (meta.hasMeta("serialize")) {
//            serialize(name, frame, meta.getMeta("serialize"));
//        } else if (meta.getBoolean("serialize", false)) {
//            serialize(name, frame, MetaBuilder.buildEmpty("serialize"));
//        }

        return input;
    }

    @Override
    protected void afterAction(Context context, String name, Table res, Laminate meta) {
        // это необходимо сделать, чтобы снапшоты и сериализация выполнялись после того, как все графики построены
//        snapshotTasks.values().stream().forEach((r) -> r.run());
//        snapshotTasks.clear();
//        serializeTasks.values().stream().forEach((r) -> r.run());
//        serializeTasks.clear();
        super.afterAction(context, name, res, meta);
    }

//    @ValueDef(name = "width", type = "NUMBER", def = "800", info = "The width of the snapshot in pixels")
//    @ValueDef(name = "height", type = "NUMBER", def = "600", info = "The height of the snapshot in pixels")
//    @ValueDef(name = "name", info = "The name of snapshot file or ouputstream (provided by context). By default equals frame name.")
//    private synchronized void snapshot(String plotName, PlotFrame frame, Meta snapshotCfg) {
//        frame.snapshot(snapshotCfg);
//    }
//
//    @ValueDef(name = "name", info = "The name of serialization file or ouputstream (provided by context). By default equals frame name.")
//    private synchronized void serialize(String plotName, PlotFrame frame, Meta snapshotCfg) {
//        if (frame instanceof JFreeChartFrame) {
//            JFreeChartFrame jfcFrame = (JFreeChartFrame) frame;
//            String fileName = snapshotCfg.getString("name", plotName) + ".jfc";
//            serializeTasks.put(fileName, () -> {
//                logger().info("Saving serialized plot to file: {}", fileName);
//                OutputStream stream = buildActionOutput(fileName);
//                try {
//                    ObjectOutputStream ostr = new ObjectOutputStream(stream);
//                    ostr.writeObject(jfcFrame.getChart());
//                } catch (IOException ex) {
//                    LoggerFactory.getLogger(getClass()).error("IO error during serialization", ex);
//                }
//            });
//
//        } else {
//            logger().error("The plot frame does not provide serialization capabilities.");
//        }
//    }
}
