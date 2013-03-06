/*
* Author : Vijesh M
* Email : mv.vijesh@gmail.com
* Facebook: https://www.facebook.com/mv.vijesh
* Twitter: https://twitter.com/mvvijesh
* Github: https://github.com/vijeshm
 */
package org.vijesh.heart;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * <p>
 * On selecting a root node, the algorithm lays out the graph in the form of concentric hearts with the root at the center.
 * The nodes at nth hop away from the root node is placed on the nth concentric heart.
 * <p>
 * This class also defines the properties the user can manipulate: root, distance between hearts, speed and convergence rate called coverage.
 * 
 * @author Vijesh M
 */
public class heart implements Layout {

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    //Flags
    private boolean executing = false;
    //Properties
    private Float speed;
    private Float consecutiveDist;
    private String root;
    private Float coverage;

    public heart(heartBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        consecutiveDist = 100f;
        speed = 10f;
        root = "0.0";
        coverage = 0.6f;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        int heartNumber = 1;
        double theta;
        double sectorAngle;
        
        float xDestn;
        float yDestn;
        float x;
        float y;
        
        ArrayList<Node> nextHeart;
        ArrayList<Node> neigh;
        int len;
        float m;
        float n;
        double dist;
        
        Node rootNode;
        
        graph.readLock();
        
        ArrayList<Node> nodes = new ArrayList( Arrays.asList( graph.getNodes().toArray() ) );
        
        if ( nodes.size() == 0 )
        {
            NotifyDescriptor d = new NotifyDescriptor.Message("The Graph is empty", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            endAlgo();
        }
        else
        {
            root = getRoot();
            rootNode = graph.getNode(root);
            if (rootNode == null)
            {
                root = graph.getNodes().toArray()[0].getNodeData().getId();
                rootNode = graph.getNodes().toArray()[0];
                NotifyDescriptor d = new NotifyDescriptor.Message("The specified root node doesnt exist. Choosing " + rootNode.getNodeData().getId() + " as the root node.", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
            try {
                x = rootNode.getNodeData().x();
                y = rootNode.getNodeData().y();
                m = getCoverage() * (speed / 10000f);
                n = 1 - m;
                rootNode.getNodeData().setX(n*x);
                rootNode.getNodeData().setY(n*y);
                nodes.remove(rootNode);
            } catch (NullPointerException ex) {
                Logger.getLogger(heart.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            }
            
            if (graphModel.isDirected())    
            {
                DirectedGraph Dgraph = (DirectedGraph) graph;
                neigh = new ArrayList<Node>( Arrays.asList( Dgraph.getSuccessors(rootNode).toArray() ) );
            }
            else    
            {
                neigh = new ArrayList<Node>( Arrays.asList( graph.getNeighbors(rootNode).toArray() ) );
            }
            
            ArrayList<Node> currentHeart = neigh;        
            while ( !nodes.isEmpty() )
            {
                if(currentHeart.isEmpty())
                {
                    currentHeart = new ArrayList<Node>(nodes);
                }
                
                theta = 0;
                sectorAngle = 2 * Math.PI / currentHeart.size();
                nextHeart = new ArrayList<Node>();
            
                len = currentHeart.size();
                for(int j=0; j<len; j++)
                {
                    x = currentHeart.get(j).getNodeData().x();
                    y = currentHeart.get(j).getNodeData().y();
                    
                    dist = consecutiveDist * ((Math.sin(theta) * Math.sqrt(Math.abs(Math.cos(theta))))/(Math.sin(theta) + 7/5.0) - 2*Math.sin(theta) + 2);
                
                    xDestn = (float) (heartNumber * dist * Math.cos(theta));
                    yDestn = (float) (heartNumber * dist * Math.sin(theta));
                
                    m = getCoverage() * (speed / 10000f);
                    n = 1 - m;
                
                    currentHeart.get(j).getNodeData().setX( m*xDestn + n*x );
                    currentHeart.get(j).getNodeData().setY( m*yDestn + n*y );
                
                    nodes.remove(currentHeart.get(j));
                    if (graphModel.isDirected())
                    {
                        DirectedGraph Dgraph = (DirectedGraph) graph;
                        neigh = new ArrayList<Node>( Arrays.asList( Dgraph.getSuccessors(currentHeart.get(j)).toArray() ) );
                    }
                    else    
                    {
                        neigh = new ArrayList<Node>( Arrays.asList( graph.getNeighbors(currentHeart.get(j)).toArray() ) );
                    }
                    for (int k=0; k<neigh.size(); k++)
                    {
                        nextHeart.add(neigh.get(k));
                    }
                    theta += sectorAngle;
                }
            
                Set<Node> currentHeartSet = new HashSet<Node>();            
                len = nextHeart.size();
                for (int j=0; j<len; j++)
                {
                    for (int k=0; k<nodes.size(); k++)
                    {
                        if (nodes.get(k).equals(nextHeart.get(j)))
                        {
                            currentHeartSet.add(nextHeart.get(j));
                            break;
                        }
                    }
                }
            
                currentHeart = new ArrayList<Node>();
                currentHeart.addAll(currentHeartSet);

                heartNumber += 1;
            }
        }
        
        graph.readUnlock();
    }

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String HEARTLAYOUT = "Heart Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Distance",
                    HEARTLAYOUT,
                    "distance between consecutive concentric hearts",
                    "getDist", "setDist"));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Node",
                    HEARTLAYOUT,
                    "the root node to be set as the center",
                    "getRoot", "setRoot"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Speed",
                    HEARTLAYOUT,
                    "How fast are moving nodes",
                    "getSpeed", "setSpeed"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Coverage",
                    HEARTLAYOUT,
                    "What percentage of distance should the nodes cover while converging",
                    "getCoverage", "setCoverage"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }
    
    public void setDist(Float dist) {
        this.consecutiveDist = dist;
    }
    
    public Float getDist() {
        return consecutiveDist;
    }
    
    public void setCoverage(Float coverage) {
        this.coverage = coverage;
    }
    
    public Float getCoverage() {
        return coverage;
    }
    
    public void setRoot(String root) {
        this.root = root;
    }
    
    public String getRoot() {
        return root;
    }
}