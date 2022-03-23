package si.ib.report;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

@RestController
public class ReportDocxController {

    @GetMapping("/test2")
    @ResponseBody
    String test() {
        return "OK";
    }

    @PostMapping("/report2")
    @ResponseBody
    ResponseEntity<byte[]> report(@RequestBody Map<String, Object> payload) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            System.out.println("VERSION=1");

            Set<String> keySet = payload.keySet();
            ArrayList<String> keys2 = new ArrayList<>();
            keys2.addAll(keySet);
            for (String key : keys2) {
                Object val = payload.get(key);
                if (val instanceof Map) {
                    Map m = (Map) val;
                    String t = "" + m.get("type");
                    if ("image".equals(t)) {
                        replaceImage(payload, key);
                    }
                    if ("chart".equals(t)) {
                        replaceChart(payload, key);
                    }
                    if ("diagram".equals(t)) {
                        replaceDiagram(payload, key);
                    }
                }
            }
            String fn = (String) payload.getOrDefault("template", "template.odt");
            byte[] doc = process(fn, payload);
            org.apache.commons.io.FileUtils.writeByteArrayToFile(new File("last_out.docx"), doc);
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            return new ResponseEntity<>(doc, headers, HttpStatus.OK);
        }
    }

    byte[] process(String filename, Map<String, Object> payload) throws Exception {
        try (FileInputStream is = new FileInputStream(filename);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IXDocReport template = XDocReportRegistry.getRegistry().loadReport(is, filename, TemplateEngineKind.Freemarker, false);
            FieldsMetadata meta = template.createFieldsMetadata();
            for (String key : payload.keySet()) {
                if (payload.get(key) != null && payload.get(key) instanceof IImageProvider) {
                    meta.addFieldAsImage(key);
                }
            }
            IContext context = template.createContext();
            for (String key2 : payload.keySet()) {
                context.put(key2, payload.get(key2));
            }
            template.process(context, out);
            return out.toByteArray();
        }
    }

    void replaceImage(Map<String, Object> payload, String key) throws Exception {
        byte[] data = Hex.decodeHex("" + ((Map) payload.get(key)).get("value"));
        ByteArrayImageProvider is = new ByteArrayImageProvider(data);
        is.setResize(true);
        payload.remove(key);
        payload.put(key, is);
    }

    void replaceChart(Map<String, Object> payload, String key) throws Exception {
        byte[] data = getChart((Map) payload.get(key));
        ByteArrayImageProvider is = new ByteArrayImageProvider(data);
        is.setResize(true);
        payload.remove(key);
        payload.put(key, is);
    }

    void replaceDiagram(Map<String, Object> payload, String key) throws Exception {
        byte[] data = getDiagram((Map) payload.get(key));
        ByteArrayImageProvider is = new ByteArrayImageProvider(data);
        is.setUseImageSize(true);
        is.setWidth(600f);
        is.setResize(true);
        payload.remove(key);
        payload.put(key, is);
    }

    byte[] getChart(Map m) throws Exception {
        String mode = (String) m.get("mode");
        if (mode == null) mode = "PieChart";
        if (mode.equalsIgnoreCase("PieChart")) return (new PieChartMaker()).make(m);
        if (mode.equalsIgnoreCase("BarChart")) return (new BarChartMaker()).make(m);
        throw new Exception("No suitable mode" + mode);
    }

    byte[] getDiagram(Map m) throws IOException {
        try {
            m = (new TransformGraph()).transform(m);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MutableGraph g = mutGraph("example1").setDirected(false);
            g.graphAttrs().add("splines", "ortho");
            g.graphAttrs().add("center", "true");
            String font = (String) m.get("default_font");
            if (font == null) font = "SansSerif";
            g.graphAttrs().add("fontname", font);
            java.util.List<String> nodes = (java.util.List<String>) m.get("nodes");
            Map<String, MutableNode> nodesm = new HashMap<>();
            for (int i = 0; i < nodes.size(); i += 2) {
                String name = nodes.get(i + 1);
                if (name == null) name = " ";
                MutableNode n = mutNode(name);
                n.attrs().add(guru.nidi.graphviz.attribute.Shape.RECTANGLE);
                n.attrs().add("style", "rounded,filled");
                n.attrs().add("fillcolor", "#f8f8f8");
                n.attrs().add(guru.nidi.graphviz.attribute.Font.name(font));
                n.attrs().add(guru.nidi.graphviz.attribute.Color.rgb("#7ca84e"));
                nodesm.put(nodes.get(i), n);
                g.add(n);
            }
            java.util.List<String> edges = (List<String>) m.get("edges");
            for (int i = 0; i < edges.size(); i += 2) {
                MutableNode fn = nodesm.get(edges.get(i));
                MutableNode ln = nodesm.get(edges.get(i + 1));
                fn.addLink(ln);
                fn.links().get(fn.links().size() - 1).attrs().add(guru.nidi.graphviz.attribute.Color.rgb("7ca84e"));
            }
            Graphviz.fromGraph(g)
                    .width(1000)
                    .fontAdjust(.87)
                    .render(Format.PNG)
                    .toOutputStream(baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(ReportDocxController.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
}
