package com.zjgsu.util;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SocialGroupUtility extends JPanel {
    public int number = 1;

    class DataGenerator extends Timer implements ActionListener {

        /*
         * Invoked when an action occurs.
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent actionevent) {
            double total = DistributedSpectrumAccess.transitionData();
            System.out.println("迭代第" + number++ + "次:" + total);
            addTotalObservation(total);
        }

        DataGenerator(int i) {
            super(i, null);
            addActionListener(this);
        }
    }

    public SocialGroupUtility(int maxItemAge) {
        super(new BorderLayout());
        total = new TimeSeries("social group utility", Millisecond.class);
        total.setMaximumItemAge(maxItemAge);

        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        timeseriescollection.addSeries(total);

        DateAxis dateaxis = new DateAxis("Time");
        NumberAxis numberaxis = new NumberAxis("social group utility");
        dateaxis.setTickLabelFont(new Font("SansSerif", 0, 12));
        numberaxis.setTickLabelFont(new Font("SansSerif", 0, 12));
        dateaxis.setLabelFont(new Font("SansSerif", 0, 14));
        numberaxis.setLabelFont(new Font("SansSerif", 0, 14));
        dateaxis.setAutoRange(true);
        dateaxis.setLowerMargin(0.0D);
        dateaxis.setUpperMargin(0.0D);
        dateaxis.setTickLabelsVisible(true);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(
                true, false);
        xylineandshaperenderer.setSeriesPaint(0, Color.RED); // 改变第一个Series的颜色             
        xylineandshaperenderer.setSeriesStroke(0, new BasicStroke(1F, 0, 2));

        XYPlot xyplot = new XYPlot(timeseriescollection, dateaxis, numberaxis, xylineandshaperenderer);
        xyplot.setBackgroundPaint(Color.BLACK); // 改变背景颜色                                    
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(1D, 1D, 1D, 1D));

        JFreeChart jfreechart = new JFreeChart("Social Group Utility", new Font(
                "SansSerif", 1, 24), xyplot, true);
        jfreechart.setBackgroundPaint(Color.white);

        ChartPanel chartpanel = new ChartPanel(jfreechart, true);
        chartpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(10, 10, 10, 10), BorderFactory
                .createLineBorder(Color.black)));

        add(chartpanel);
    }

    private void addTotalObservation(double d) {
        total.add(new Millisecond(), d);
    }

    /**
     * 绘制曲线
     */
    public static void plot() {
        JFrame jframe = new JFrame("Social Group Utility");
        SocialGroupUtility sgumDemo = new SocialGroupUtility(10000);
        jframe.getContentPane().add(sgumDemo, "Center");
        jframe.setBounds(200, 120, 1000, 500);
        jframe.setVisible(true);

        (sgumDemo.new DataGenerator(10)).start(); // 数据生成速率

        jframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                System.exit(0);
            }
        });
    }

    private TimeSeries total;
}