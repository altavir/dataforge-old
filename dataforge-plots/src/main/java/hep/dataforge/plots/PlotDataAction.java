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

import hep.dataforge.actions.ActionResult;
import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataSet;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
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
        description = "Scatter plot of given DataSet", inputType = DataSet.class, outputType = DataSet.class)
@ValueDef(name = "xName", def = "x",
        info = "The name of X Value in the DataSet")
@ValueDef(name = "yName", def = "y",
        info = "The name of Y Value in the DataSet")
@ValueDef(name = "xErrName", def = "xErr",
        info = "The name of X error Value in the DataSet (currently not working)")
@ValueDef(name = "yErrName", def = "yErr",
        info = "The name of Y error Value in the DataSet (currently not working)")
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

public class PlotDataAction extends OneToOneAction<DataSet, DataSet> {

    private final PlotHolder holder;
//    private final static DescriptorBuilder frameDescriptor = new DescriptorBuilder(XYPlotFrame.class);

    public PlotDataAction(Context context, Meta annotation) {
        super(context, annotation);
        //initializing plots plugin if it is not present
        if(!context.provides("plots")){
            context.loadPlugin(new PlotsPlugin());
        }
        holder = (PlotsPlugin) context.pluginManager().getPlugin("hep.dataforge:plots");
    }

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
    protected DataSet execute(Logable log, Meta meta, DataSet input){
        Meta finder = readMeta(input.meta());
        PlotFrame frame;

        String groupBy = finder.getString("groupBy");
        String frame_name = finder.getString(groupBy, "default");
        if (holder.hasPlotFrame(frame_name)) {
            frame = holder.getPlotFrame(frame_name);
        } else {
            frame = holder.buildPlotFrame(frame_name, findFrameDescription(finder, frame_name));
        }

        frame.add(new PlottableData(input,
                finder.getString("xName"),
                finder.getString("yName"),
                finder.getString("xErrName"),
                finder.getString("yErrName")));

        if (finder.hasNode("snapshot")) {
            snapshot(log, frame, finder.getNode("snapshot"));
        } else if (finder.getBoolean("snapshot", false)) {
            snapshot(log, frame, MetaBuilder.buildEmpty("snapshot"));
        }

        if (finder.hasNode("serialize")) {
            serialize(log, frame, finder.getNode("serialize"));
        } else if (finder.getBoolean("serialize", false)) {
            serialize(log, frame, MetaBuilder.buildEmpty("serialize"));
        }

        return input;
    }

    private final Map<String, Runnable> snapshotTasks = new HashMap<>();
    private final Map<String, Runnable> serializeTasks = new HashMap<>();

    @Override
    protected void afterAction(ActionResult output) throws ContentException {
        // это необходимо сделать, чтобы снапшоты и сериализация выполнялись после того, как все графики построены

        for (Runnable r : snapshotTasks.values()) {
            r.run();
        }
        snapshotTasks.clear();
        for (Runnable r : serializeTasks.values()) {
            r.run();
        }
        serializeTasks.clear();
        super.afterAction(output); //To change body of generated methods, choose Tools | Templates.
    }

    @ValueDef(name = "width", type = "NUMBER", def = "800", info = "The width of the snapshot in pixels")
    @ValueDef(name = "height", type = "NUMBER", def = "600", info = "The height of the snapshot in pixels")
    @ValueDef(name = "name", info = "The name of snapshot file or ouputstream (provided by context). By default equals frame name.")
    private synchronized void snapshot(Logable log, PlotFrame frame, Meta snapshotCfg) {
        if (frame instanceof JFreeChartFrame) {
            JFreeChartFrame jfcFrame = (JFreeChartFrame) frame;
            String fileName = snapshotCfg.getString("name", jfcFrame.getName()) + ".png";
            snapshotTasks.put(fileName, () -> {
                log.log("Saving plot snapshot to file: {}", fileName);
                OutputStream stream = buildActionOutput(fileName);
                jfcFrame.toPNG(stream, snapshotCfg);
            });
        } else {
            log.logError("The plot frame does not provide snapshot capabilities. Ignoring 'snapshot' option.");
            LoggerFactory.getLogger(getClass()).error("For the moment only JFreeChart snapshots are supported.");
        }
    }

    @ValueDef(name = "name", info = "The name of serialization file or ouputstream (provided by context). By default equals frame name.")
    private synchronized void serialize(Logable log, PlotFrame frame, Meta snapshotCfg) {
        if (frame instanceof JFreeChartFrame) {
            JFreeChartFrame jfcFrame = (JFreeChartFrame) frame;
            String fileName = snapshotCfg.getString("name", jfcFrame.getName()) + ".jfc";
            serializeTasks.put(fileName, () -> {
                log.log("Saving serialized plot to file: {}", fileName);
                OutputStream stream = buildActionOutput(fileName);
                try {
                    ObjectOutputStream ostr = new ObjectOutputStream(stream);
                    ostr.writeObject(jfcFrame.getChart());
                } catch (IOException ex) {
                    LoggerFactory.getLogger(getClass()).error("IO error during serialization", ex);
                }
            });

        } else {
            log.logError("The plot frame does not provide serialization capabilities.");
            LoggerFactory.getLogger(getClass()).error("For the moment only JFreeChart serialization is supported.");
        }
    }
 }