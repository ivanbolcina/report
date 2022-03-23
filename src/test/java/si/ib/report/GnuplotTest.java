package si.ib.report;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import si.ib.report.BarChartMaker;
import si.ib.report.PieChartMaker;
import si.ib.report.ReportDocxController;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GnuplotTest {

    @Test
    public void doPlot() throws Exception{
        BarChartMaker cm=new BarChartMaker();
        Map<String,Object> map=new HashMap<>();
        map.put("title","Po spolih");
        map.put("width","1000");
        map.put("height","1000");
        ArrayList<String> cats=new ArrayList<>();
        cats.add("Moski");
        cats.add("Zenske");
        map.put("categories",cats);
        ArrayList<String> colors=new ArrayList<>();
        colors.add("0xff0000");
        colors.add("0x00ff00");
        map.put("colors",colors);
        ArrayList<String> values=new ArrayList<>();
        values.add("4");
        values.add("5");
        map.put("values",values);
        cm.make(map);
    }

    @Test
    public void doPlot2() throws Exception{
        PieChartMaker cm=new PieChartMaker();
        Map<String,Object> map=new HashMap<>();
        map.put("title","Po spolih");
        map.put("width","1000");
        map.put("height","1000");
        ArrayList<String> cats=new ArrayList<>();
        cats.add("Moski");
        cats.add("Zenske");
        map.put("categories",cats);
        ArrayList<String> colors=new ArrayList<>();
        colors.add("0xff0000");
        colors.add("0x00ff00");
        map.put("colors",colors);
        ArrayList<String> values=new ArrayList<>();
        values.add("4");
        values.add("5");
        map.put("values",values);
        cm.make(map);
    }

    @Test
    public void doGraph() throws  Exception
    {
        ReportDocxController d=new ReportDocxController();
        Map<String,Object> map=new HashMap<>();
        ArrayList<String> nodes=new ArrayList<>();
        nodes.add("1");
        nodes.add("parent");
        nodes.add("2");
        nodes.add("child");
        nodes.add("3");
        nodes.add("c");
        nodes.add("4");
        nodes.add("long long longer");
        for (int i=0;i<6;i++){
            nodes.add("x_"+(i*2));
            nodes.add("Department "+i);
        }
        map.put("nodes",nodes);
        ArrayList<String> edges=new ArrayList<>();
        edges.add("1");
        edges.add("2");
        edges.add("1");
        edges.add("3");
        edges.add("1");
        edges.add("4");
        for (int i=0;i<6;i++){
            edges.add("4");
            edges.add("x_"+(i*2));
        }

        map.put("edges",edges);
     //   map.put("default_font","FreeSans");
        byte[] diagram = d.getDiagram(map);
        IOUtils.write(diagram,new FileOutputStream("test/diatest.png"));
    }
}
