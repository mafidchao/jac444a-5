/*
 * MapView.java
 * 
 * Original sample code provided by Josh Marinacci - http://today.java.net/pub/a/today/2007/10/30/building-maps-into-swing-app-with-jxmapviewer.html
 * 
 * JAC444A - Assignment 2
 * Additions made by:
 * Michael Afidchao
 * 062-699-103
 * 
 */



package mapapp;

import java.awt.Color;
import java.awt.Graphics2D;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//import javax.swing.Timer;
//import javax.swing.Icon;
//import javax.swing.JDialog;
//import javax.swing.JFrame;
import javax.swing.*;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.mapviewer.wms.WMSService;
import org.jdesktop.swingx.mapviewer.wms.WMSTileFactory;

/**
 * The application's main frame.
 */
public class MapView extends FrameView {
    // Variables added by Michael Afidchao
    private JToolBar jtbToolbar;
    private JPanel byNamePanel;
    private JPanel byCoordPanel;    
    private JPanel containerPanel;
    
    private JLabel jlbCountry;
    private JLabel jlbCity;
    private JLabel jlbAddress;
    private JLabel jlbByName;
    private JComboBox jcbCountry;
    private JComboBox jcbCity;
    private JTextField jtfAddress;
    private JButton jbtByName;	

    public MapView(SingleFrameApplication app) {
        super(app);

        initComponents();
        
        //WMSService wms = new WMSService();
        //wms.setBaseUrl("http://132.156.10.87/cgi-bin/atlaswms_en?REQUEST=GetCapabilities");
//        wms.setLayer();
        //jXMapKit1.setTileFactory(new WMSTileFactory(wms));

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox(ActionEvent e) {
        if (aboutBox == null) {
            JFrame mainFrame = MapApp.getApplication().getMainFrame();
            aboutBox = new MapAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MapApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jXMapKit1 = new org.jdesktop.swingx.JXMapKit();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jXMapKit1.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        jXMapKit1.setDataProviderCreditShown(true);
        jXMapKit1.setName("jXMapKit1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(mapapp.MapApp.class).getContext().getActionMap(MapView.class, this);
        jButton1.setAction(actionMap.get("goChicago")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("addWaypoint")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        
        //*** code inserted by Michael Afidchao ***
        jtbToolbar = new JToolBar("Show/Hide Panel");
        initToolBar (jtbToolbar);  //initialize tool bar properties with method
        containerPanel = new JPanel();
        byNamePanel = new JPanel();
        byCoordPanel = new JPanel();
        
        jlbCountry = new JLabel("Country");
        jlbCity = new JLabel("City");
        jlbAddress = new JLabel("Address");
        jcbCountry = new JComboBox();
        jcbCity = new JComboBox();
        jtfAddress = new JTextField();
        
        byNamePanel.setLayout(new BoxLayout(byNamePanel, BoxLayout.PAGE_AXIS));
        
        byNamePanel.add(jlbCountry);
        byNamePanel.add(jlbCity);
        byNamePanel.add(jlbAddress);
        byNamePanel.add(jcbCountry);
        byNamePanel.add(jcbCity);
        byNamePanel.add(jtfAddress);
        
        
        containerPanel.add(byNamePanel);
        containerPanel.add(byCoordPanel);
        // *** end code insertion ***       
        

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
            .add(jtbToolbar)
            .add(mainPanelLayout.createSequentialGroup()            	
                //.add(jButton1)               
                .add(containerPanel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                //.add(jButton2)                
                .add(183, 183, 183))
            .add(jXMapKit1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .add(jXMapKit1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtbToolbar)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)                	
                    //.add(jButton1)              
                    .add(containerPanel)))
                    //.add(jButton2)))
        );        

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(mapapp.MapApp.class).getContext().getResourceMap(MapView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 228, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );
               
        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    
    //inserted by Michael Afidchao
    //add buttons to the JToolBar that shows/hides the panel
    protected void initToolBar(JToolBar toolbar)
    {
    	class ToolBarHandler implements ActionListener
    	{
    		public void actionPerformed (ActionEvent e)
    		{
    			containerPanel.setVisible(!containerPanel.isVisible());
    		}
    	}
    	
    	JButton button = new JButton("Show/Hide Controls");
    	button.addActionListener(new ToolBarHandler());
    	toolbar.add(button);
    	toolbar.setFloatable(false);    	
    }

    @org.jdesktop.application.Action
    public void goChicago() {
        // put your action code here
        //jXMapKit1.setCenterPosition(new GeoPosition(41.881944,-87.627778));
        jXMapKit1.setAddressLocation(new GeoPosition(41.881944,-87.627778));
    }

    @org.jdesktop.application.Action
    public void addWaypoint() {
        Set<Waypoint> waypoints = new HashSet<Waypoint>();
        waypoints.add(new Waypoint(41.881944,-87.627778));
        waypoints.add(new Waypoint(40.716667,-74));
        
        WaypointPainter painter = new WaypointPainter();
        painter.setWaypoints(waypoints);
        painter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                g.setColor(Color.RED);
                g.drawLine(-5,-5,+5,+5);
                g.drawLine(-5,+5,+5,-5);
                return true;
            }
        });
        
        jXMapKit1.getMainMap().setOverlayPainter(painter);
        // put your action code here
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private org.jdesktop.swingx.JXMapKit jXMapKit1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;            
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
