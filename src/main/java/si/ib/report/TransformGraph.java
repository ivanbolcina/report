package si.ib.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransformGraph {

    public Map transform(Map m) {
        java.util.List<String> nodes = (java.util.List<String>) m.get("nodes");
        java.util.List<String> edges = (java.util.List<String>) m.get("edges");
        Graph g = new Graph();
        //add nodes
        for (int i = 0; i < nodes.size() / 2; i++) {
            Node n = new Node();
            n.id = nodes.get(i * 2);
            n.text = nodes.get(i * 2 + 1);
            g.nodes.add(n);
        }
        //add edges
        for (int i = 0; i < edges.size() / 2; i++) {
            Node p = g.findNode(edges.get(i * 2));
            Node c = g.findNode(edges.get(i * 2 + 1));
            c.parents.add(p);
            p.children.add(c);
        }
        //find roots
        for (Node n : g.nodes) {
            if (n.parents.size() == 0) g.roots.add(n);
        }
        for (Node root : g.roots) {
            filter(g, root, 1);
        }
        Map out = new HashMap();
        ArrayList<String> nn = new ArrayList<String>();
        ArrayList<String> ne = new ArrayList<String>();
        for (Node root : g.roots) {
            output(g, root, nn, ne);
        }
        out.put("nodes", nn);
        out.put("edges", ne);
        return out;
    }

    void filter(Graph g, Node n, int depth) {
        if (depth > 20) return;
        if (n.children.size() > 2) {
            Node fcl = null;
            for (Node c : n.children) {
                if (c.children.size() == 0 && c.parents.size() == 1) {
                    fcl = c;
                    break;
                }
            }
            if (fcl != null) {
                boolean first = true;
                ArrayList<Node> copy = new ArrayList<>();
                copy.addAll(n.children);
                for (Node c : copy) {
                    if (c.children.size() == 0 && c.parents.size() == 1 && c != fcl) {
                        if (first) fcl.text = "\u2022 " + fcl.text + "\\l";
                        first = false;
                        fcl.text += "\u2022 " + c.text + "\\l";
                        n.children.remove(c);
                    }
                }
            }
        }
        for (Node c : n.children) {
            filter(g, c, depth + 1);
        }
    }

    void output(Graph g, Node n, ArrayList<String> nodes, ArrayList<String> edges) {
        nodes.add(n.id);
        nodes.add(n.text);
        for (Node c : n.children) {
            edges.add(n.id);
            edges.add(c.id);
            output(g, c, nodes, edges);
        }
    }

    static class Graph {
        public ArrayList<Node> roots = new ArrayList<>();
        public ArrayList<Node> nodes = new ArrayList<>();

        Node findNode(String id) {
            for (Node cnd : nodes) {
                if (cnd.id.equals(id)) return cnd;
            }
            return null;
        }
    }

    static class Node {
        public String id;
        public String text;
        public ArrayList<Node> parents = new ArrayList<>();
        public ArrayList<Node> children = new ArrayList<>();
    }

}
