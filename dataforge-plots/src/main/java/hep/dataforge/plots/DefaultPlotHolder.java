/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.jfreechart.JFreeChartFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author Alexander Nozik
 */
public class DefaultPlotHolder implements PlotHolder {
    private final Map<String, PlotFrame> frames = new HashMap<>();

    protected synchronized PlotFrame buildFrame(String name, Meta annotation) {
        JFrame frame = new JFrame("DataForge visualisator");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e); //To change body of generated methods, choose Tools | Templates.
                frames.remove(name);
            }

        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(800, 600));
        frame.setContentPane(panel);

        SwingUtilities.invokeLater(() -> {
            frame.pack();
            frame.setVisible(true);
        });
        return new JFreeChartFrame(name, annotation).display(panel);
    }

    @Override
    public synchronized PlotFrame buildPlotFrame(String stage, String name, Meta annotation) {
        if (!frames.containsKey(name)) {

            frames.put(name, buildFrame(name, annotation));

        }
        return frames.get(name);
    }

    @Override
    public PlotFrame getPlotFrame(String stage, String name) throws NameNotFoundException {
        if (!hasPlotFrame(stage, name)) {
            throw new NameNotFoundException(name);
        }
        return frames.get(name);
    }

    @Override
    public boolean hasPlotFrame(String stage, String name) {
        return frames.containsKey(name);
    }
}
