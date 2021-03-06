package org.vijesh.concentric;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * Layout builder for the Concentric layout
 * @author Vijesh M
 */
@ServiceProvider(service = LayoutBuilder.class)
public class concentricBuilder implements LayoutBuilder {

    @Override
    public String getName() {
        return "Concentric Layout";
    }

    @Override
    public LayoutUI getUI() {
        return new LayoutUI() {

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public JPanel getSimplePanel(Layout layout) {
                return null;
            }

            @Override
            public int getQualityRank() {
                return -1;
            }

            @Override
            public int getSpeedRank() {
                return -1;
            }
        };
    }

    @Override
    public Layout buildLayout() {
        return new concentric(this);
    }
}