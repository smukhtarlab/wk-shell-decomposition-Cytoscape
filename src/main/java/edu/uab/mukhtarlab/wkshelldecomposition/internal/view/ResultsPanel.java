package edu.uab.mukhtarlab.wkshelldecomposition.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.*;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uab.mukhtarlab.wkshelldecomposition.internal.model.Result;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsPanel extends JPanel implements CytoPanelComponent {


    // Graphical classes
    private JPanel infoPanel;

    private Result result;

    private final CyServiceRegistrar registrar;

    private static final Logger logger = LoggerFactory.getLogger(CyUserLog.class);

    /**
     * Constructor for the Results Panel which displays the clusters in a
     * browser table and explore panels for each cluster.
     *
     */
    public ResultsPanel(
            final CyServiceRegistrar registrar,
            Result result
    ) {

        this.registrar = registrar;
        this.result = result;

        if (isAquaLAF())
            setOpaque(false);

        final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(false);
        layout.setAutoCreateGaps(false);

        infoPanel = getInfoPanel();

        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.CENTER, true)
                        .addComponent(infoPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(infoPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );

        //Mean median degree of all shells
        //of current shell
        //slider to select shell
        //also have text input that slider is synced with so that you could write a shell
    }

    private JPanel getInfoPanel() {
        if (infoPanel == null) {
            infoPanel = new JPanel();
            infoPanel.setBorder(createTitledBorder(""));

            if (isAquaLAF())
                infoPanel.setOpaque(false);

            logger.debug(this.result.toString());

            JLabel kMaxLabel = new JLabel("k-Max: " + ((this.result!=null) ? this.result.getkMax() : ""), JLabel.CENTER);
            kMaxLabel.setHorizontalAlignment(JLabel.RIGHT);
            JLabel kMinLabel = new JLabel("k-Min: " + ((this.result!=null) ? this.result.getkMin() : ""), JLabel.CENTER);
            kMaxLabel.setHorizontalAlignment(JLabel.RIGHT);

            final GroupLayout layout = new GroupLayout(infoPanel);
            infoPanel.setLayout(layout);
            layout.setAutoCreateContainerGaps(true);
            layout.setAutoCreateGaps(false);

            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
                            .addComponent(kMaxLabel)
                            .addComponent(kMinLabel)
                    ).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING, true)
                            //.addComponent(netScopeBtn)
                    )
            );
            layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(kMaxLabel)
                            .addComponent(kMinLabel)
                    ).addGroup(layout.createSequentialGroup()
                            //.addComponent(netScopeBtn)
                    )
            );
        }

        return infoPanel;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.EAST;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getTitle() {
        return "wK-Shells";
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
