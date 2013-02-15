/*
* Author : Vijesh M
* Email : mv.vijesh@gmail.com
* Facebook: https://www.facebook.com/mv.vijesh
* Twitter: https://twitter.com/mvvijesh
* Github: https://github.com/vijeshm
 */
package org.vijesh.concentric;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

/**
 * Example of a layout algorithm which places all nodes in a grid.
 * <p>
 * On selecting a root node, the algorithm lays out the graph in the form of concentric circle with the root at the center.
 * The nodes at nth hop away from the root node is placed on the nth concentric circle.
 * <p>
 * This class also defines the properties the user can manipulate: root, distance between circles, speed and convergence rate called coverage.
 * 
 * @author Vijesh M
 */
public class concentric implements Layout {

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    //Flags
    private boolean executing = false;
    //Properties
    private Float speed;
    
    private Float dist;
    private String root;
    private Float coverage;

    public concentric(concentricBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        speed = 10f;
        root = "0.0";
        dist = 100f;
        coverage = 0.6f;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        int circleNumber = 1;
        double theta;
        double sectorAngle;
        
        float xDestn;
        float yDestn;
        float x;
        float y;
        
        ArrayList<Node> nextCircle;
        Node[] neigh;
        int len;
        float m;
        float n;
        
        Node rootNode;
        
        graph.readLock();
        
        ArrayList<Node> nodes = new ArrayList( Arrays.asList( graph.getNodes().toArray() ) );
        
        root = getRoot();
        rootNode = graph.getNode(root);
        
        try {
            x = rootNode.getNodeData().x();
            y = rootNode.getNodeData().y();
            m = getCoverage() * (speed / 10000f);
            n = 1 - m;
            rootNode.getNodeData().setX(n*x);
            rootNode.getNodeData().setY(n*y);
            nodes.remove(rootNode);
        } catch (NullPointerException ex) {
            Logger.getLogger(concentric.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        
        ArrayList<Node> currentCircle = new ArrayList ( Arrays.asList( graph.getNeighbors(rootNode).toArray() ) );
        
        while ( !nodes.isEmpty() )
        {
            theta = 0;
            sectorAngle = 2 * Math.PI / currentCircle.size();
            nextCircle = new ArrayList<Node>();
            
            len = currentCircle.size();
            for(int j=0; j<len; j++)
            {
                x = currentCircle.get(j).getNodeData().x();
                y = currentCircle.get(j).getNodeData().y();
                
                xDestn = (float) (circleNumber * dist * Math.cos(theta));
                yDestn = (float) (circleNumber * dist * Math.sin(theta));
                
                m = getCoverage() * (speed / 10000f);
                n = 1 - m;
                
                currentCircle.get(j).getNodeData().setX( m*xDestn + n*x );
                currentCircle.get(j).getNodeData().setY( m*yDestn + n*y );
                
                nodes.remove(currentCircle.get(j));
                neigh = graph.getNeighbors(currentCircle.get(j)).toArray();
                for (int k=0; k<neigh.length; k++)
                {
                    nextCircle.add(neigh[k]);
                }
                theta += sectorAngle;
            }
            
            Set<Node> currentCircleSet = new HashSet<Node>();            
            len = nextCircle.size();
            for (int j=0; j<len; j++)
            {
                for (int k=0; k<nodes.size(); k++)
                {
                    if (nodes.get(k).equals(nextCircle.get(j)))
                    {
                        currentCircleSet.add(nextCircle.get(j));
                        break;
                    }
                }
            }
            
            currentCircle = new ArrayList<Node>();
            currentCircle.addAll(currentCircleSet);

            //System.out.println("circle Number: " + circleNumber);
            circleNumber += 1;
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
        final String CONCENTRICLAYOUT = "Concentric Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Distance",
                    CONCENTRICLAYOUT,
                    "distance between consecutive concentric circles",
                    "getDist", "setDist"));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Node",
                    CONCENTRICLAYOUT,
                    "the root node to be set as the center",
                    "getRoot", "setRoot"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Speed",
                    CONCENTRICLAYOUT,
                    "How fast are moving nodes",
                    "getSpeed", "setSpeed"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Coverage",
                    CONCENTRICLAYOUT,
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
        this.dist = dist;
    }
    
    public Float getDist() {
        return dist;
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