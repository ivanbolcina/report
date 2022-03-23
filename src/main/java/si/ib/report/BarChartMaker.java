package si.ib.report;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BarChartMaker {

    static String replace(String template, String search, String replace) {
        if (replace == null) replace = "";
        return template.replace(search, replace);
    }

    public byte[] make(Map<String, Object> payload) throws Exception {
        File ftemplate = null;
        File fdata = null;
        File fimg = null;
        File dir = new File("tmp");
        System.out.println(dir.getAbsolutePath());
        try {
            ftemplate = File.createTempFile("chart_", ".tempate", dir);
            fdata = File.createTempFile("chart_", ".data", dir);
            fimg = File.createTempFile("chart_", ".png", dir);
            String template = IOUtils.resourceToString("/templates/BarChart2", UTF_8);
            template = replace(template, "${title}", (String) payload.get("title"));
            template = replace(template, "${result}", fimg.getAbsolutePath());
            template = replace(template, "${data}", fdata.getAbsolutePath());
            List<String> categories = (List<String>) payload.get("categories");
            List<String> colors = (List<String>) payload.get("colors");
            List<Object> values = (List<Object>) payload.get("values");
            String xrange = "-0.6:" + (categories.size() - 0.4);
            template = replace(template, "${x_range}", xrange);
            template = replace(template, "${y_label}", (String) payload.get("y_label"));
            template = replace(template, "${width}", "" + payload.get("width"));
            template = replace(template, "${height}", "" + payload.get("height"));
            String font = (String) payload.get("default_font");
            if (font == null) font = "FreeSans,20";
            template = replace(template, "${font}", font);
            try (FileOutputStream fos = new FileOutputStream(ftemplate)) {
                IOUtils.write(template, fos, UTF_8);
            }
            try (FileOutputStream fos = new FileOutputStream(fdata)) {
                for (int i = 0; i < categories.size(); i++) {
                    String line = categories.get(i) + "|" + values.get(i) + "|" + colors.get(i) + "\n";
                    IOUtils.write(line, fos, UTF_8);
                }
            }
            String line = "gnuplot \"" + ftemplate.getAbsolutePath() + "\"";
            CommandLine cmdLine = CommandLine.parse(line);
            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute(cmdLine);
            try (FileInputStream fis = new FileInputStream(fimg)) {
                return IOUtils.readFully(fis, (int) fimg.length());
            }
        } finally {
            if (ftemplate != null) ftemplate.delete();
            if (fdata != null) fdata.delete();
            if (fimg != null) fimg.delete();
        }
    }

}
