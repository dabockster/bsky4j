package bsky4j.api.graph;

import bsky4j.model.atproto.graph.GraphNode;
import bsky4j.model.atproto.graph.GraphEdge;
import java.util.List;

public interface GraphResource {
    List<GraphNode> getNodes();
    List<GraphEdge> getEdges();
    void addNode(GraphNode node);
    void addEdge(GraphEdge edge);
    void removeNode(GraphNode node);
    void removeEdge(GraphEdge edge);
}
