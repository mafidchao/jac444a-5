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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
//import javax.swing.Timer;
//import javax.swing.Icon;
//import javax.swing.JDialog;
//import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

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
    private JPanel waypointPanel;
    private JPanel containerPanel;
        
    private JLabel jlbCountry;
    private JLabel jlbCity;
    private JLabel jlbAddress;
    private JLabel jlbByName;
    private JComboBox<String> jcbCountry;
    private JComboBox<String> jcbCity;
    private JTextField jtfAddress;
    private JButton jbtByName; //, jbtByCountry, jbtByCity, jbtByAddress;
    
    private JLabel jlbLatitude, jlbLongitude, jlbByCoord;
    //private JTextField jtfLatitude, jtfLongitude;
    private JFormattedTextField jtfLatitude, jtfLongitude;
    private JButton jbtSearchCoord, jbtByCoord;
    
    //private JButton jbtPrevWp, jbtNextWp;
    private JLabel jlbPrevNextWp, jlbWaypoint, jlbWpLat, jlbWpLong;
    private JSpinner jspPrevNextWp;
    private JButton jbtDeleteWp, jbtAddWp, jbtGoToWp;
    private JButton jbtSave;
    private JButton jbtLoad;
    
    private SpinnerNumberModel waypointModel;    
    //private Set<Waypoint> waypointList = new HashSet<Waypoint>();
    private ArrayList<Waypoint> waypointList;
    private ArrayList<Waypoint> countryList;
    private ArrayList<Waypoint> cityList;

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
        waypointPanel = new JPanel();
                
        jlbCountry = new JLabel("Country");
        jlbCity = new JLabel("City");
        jlbAddress = new JLabel("Address");
        jlbByName = new JLabel("Search by Name");
        jbtByName = new JButton ("Search Address");
        //jbtByCountry = new JButton ("Country");
        //jbtByCity = new JButton ("City");
        //jbtByAddress = new JButton ("Address");
        jcbCountry = new JComboBox<String>();
        jcbCity = new JComboBox<String>();
        jtfAddress = new JTextField();
                
        jlbByCoord = new JLabel("Search by Coordinates");
        jlbLongitude = new JLabel("Longitude");
        jlbLatitude = new JLabel("Latitude");
        
        //jtfLongitude = new JTextField(20);
        //jtfLatitude = new JTextField(20);
        jtfLongitude = new JFormattedTextField(new NumberFormatter(new DecimalFormat("0.000000")));
        jtfLatitude = new JFormattedTextField(new NumberFormatter(new DecimalFormat("0.000000")));
        jtfLatitude.setColumns(20);
        jtfLongitude.setColumns(20);
        
        jbtSearchCoord = new JButton("Display Current Location Info");
        jbtByCoord = new JButton("Go to Coordinates");
        
        jlbWaypoint = new JLabel ("Waypoints Management");
        jlbPrevNextWp = new JLabel("Waypoint");
        jlbWpLat = new JLabel ("Latitude: n/a");
        jlbWpLong = new JLabel ("Longitude: n/a");
        
        waypointList = new ArrayList<Waypoint>();
        countryList = new ArrayList<Waypoint>();
        cityList = new ArrayList<Waypoint>();
        //waypointList.add(new Waypoint(0, 0));
        //waypointModel = new SpinnerListModel(waypointList);
        waypointModel = new SpinnerNumberModel(0,0,0,1);
        jspPrevNextWp = new JSpinner(waypointModel);        
        jbtGoToWp = new JButton("Go to Waypoint");
        jbtAddWp = new JButton("Add Waypoint");
        jbtDeleteWp = new JButton("Delete Waypoint");
        jbtSave = new JButton("Save Waypoints File");
        jbtLoad = new JButton("Load Waypoints File");
        
        //jspPrevNextWp.setSize(100, jspPrevNextWp.getSize().height);
        jspPrevNextWp.setPreferredSize(new Dimension(50, 20));
        
        //byNamePanel.setLayout(new BoxLayout(byNamePanel, BoxLayout.Y_AXIS));        
        byNamePanel.setLayout(new BorderLayout());
        //byCoordPanel.setLayout(new BoxLayout(byCoordPanel, BoxLayout.Y_AXIS));
        byCoordPanel.setLayout(new BorderLayout());
        //waypointPanel.setLayout(new BoxLayout(waypointPanel, BoxLayout.Y_AXIS));
        waypointPanel.setLayout(new BorderLayout());
        containerPanel.setLayout(new GridLayout(1, 3));
        
        byNamePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        byCoordPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        waypointPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        containerPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        containerPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        
        //set up the "By Name" panel (Country, City, Address)
        JPanel tmpjp1 = new JPanel();
        JPanel tmpjp2 = new JPanel();
        JPanel tmpjp3 = new JPanel();        
        JPanel tmpjp4 = new JPanel();
        JPanel tmpjp5 = new JPanel();        
        JPanel tmpCenter = new JPanel();

        
        /*tmpjp1.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp2.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp2a.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp2b.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp3.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp3a.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp3b.setBorder(BorderFactory.createLineBorder(Color.black));        
        tmpjp4.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp4a.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp4b.setBorder(BorderFactory.createLineBorder(Color.black));        
        tmpjp5.setBorder(BorderFactory.createLineBorder(Color.black));*/
        
        //tmpjp2.setLayout(new GridBagLayout());
        tmpjp2.setLayout(new GridLayout(0,2));
        tmpjp3.setLayout(new GridLayout(0,2));
        tmpjp4.setLayout(new GridLayout(0,2));
        //tmpjp2.setLayout(new BoxLayout(tmpjp2, BoxLayout.X_AXIS));        
        //tmpjp3.setLayout(new BoxLayout(tmpjp3, BoxLayout.X_AXIS));
        //tmpjp4.setLayout(new BoxLayout(tmpjp4, BoxLayout.X_AXIS));
        tmpCenter.setLayout(new BoxLayout(tmpCenter, BoxLayout.Y_AXIS));

        
        /*tmpjp2a.setMaximumSize(new Dimension(100, 40));
        tmpjp3a.setMaximumSize(new Dimension(100, 40));
        tmpjp4a.setMaximumSize(new Dimension(100, 40));*/
        //tmpjp2a.setAlignmentX(Component.RIGHT_ALIGNMENT);
        //tmpjp2a.setAlignmentY(10);
        //jlbCountry.setAlignmentX(Component.LEFT_ALIGNMENT);
        //jcbCountry.setPreferredSize(new Dimension(10, 20));
        //jcbCountry.setMaximumSize(new Dimension(100, 20));
        //jlbCountry.setPreferredSize(new Dimension(100, jlbCountry.getSize().height));
        jlbCountry.setHorizontalAlignment(SwingConstants.LEFT);
        jlbCity.setHorizontalAlignment(SwingConstants.LEFT);
        jlbAddress.setHorizontalAlignment(SwingConstants.LEFT);
        //jcbCountry.setMaximumSize(new Dimension(4, 5));
        tmpjp2.setMaximumSize(new Dimension(200, 20));
        tmpjp3.setMaximumSize(new Dimension(200, 20));
        tmpjp4.setMaximumSize(new Dimension(200, 20));
        //tmpjp3.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        //tmpjp4.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        //tmpCenter.setMaximumSize(new Dimension(50, 200));
        
        //jcbCountry.setAlignmentX(10);
        
        
        //jlbCity.setPreferredSize(new Dimension(100, 10));
        //jlbCity.setHorizontalAlignment(SwingConstants.RIGHT);
        //jlbAddress.setPreferredSize(new Dimension(100, jlbAddress.getSize().height));
        //jlbAddress.setHorizontalAlignment(SwingConstants.RIGHT);
        
        //jcbCity.setMaximumSize(new Dimension(100, 20));
        //jcbCountry.setMaximumSize(new Dimension(100, 20));
        //jtfAddress.setMaximumSize(new Dimension(100, 20));
        
        tmpjp1.add(jlbByName);
        
        tmpjp2.add(jlbCountry);
        //tmpjp2.add(new JLabel(" "));
        tmpjp2.add(jcbCountry);
        //tmpjp2.add(new JLabel(" "));
        
        
        tmpjp3.add(jlbCity);
        //tmpjp3.add(new JLabel(" "));
        tmpjp3.add(jcbCity);
        //tmpjp3.add(new JLabel(" "));
        
        tmpjp4.add(jlbAddress);
        //tmpjp4.add(new JLabel(" "));
        tmpjp4.add(jtfAddress);
        //tmpjp4.add(new JLabel(" "));
        
        tmpjp5.add(jbtByName);
        
        tmpCenter.add(tmpjp2);
        tmpCenter.add(new JPanel());
        tmpCenter.add(tmpjp3);
        tmpCenter.add(new JPanel());
        tmpCenter.add(tmpjp4);
        
        byNamePanel.add(tmpjp1, BorderLayout.PAGE_START);        
        byNamePanel.add(tmpCenter, BorderLayout.CENTER);
        byNamePanel.add(tmpjp5, BorderLayout.PAGE_END);
         
        //set up the Latitude/Longitude panel
        tmpjp1 = new JPanel();
        tmpjp2 = new JPanel();
        tmpjp3 = new JPanel();
        tmpjp4 = new JPanel();
        tmpjp5 = new JPanel();
        tmpCenter = new JPanel();
        tmpCenter.setLayout(new BoxLayout(tmpCenter, BoxLayout.Y_AXIS));

        /*tmpjp1.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp2.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp3.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp4.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp5.setBorder(BorderFactory.createLineBorder(Color.black));*/
        
        tmpjp1.add(jlbByCoord);
        tmpjp2.add(jlbLatitude);
        tmpjp2.add(jtfLatitude);
        tmpjp3.add(jlbLongitude);
        tmpjp3.add(jtfLongitude);
        tmpjp4.add(jbtSearchCoord);
        tmpjp5.add(jbtByCoord);
        
        tmpCenter.add(tmpjp2);
        tmpCenter.add(tmpjp3);
        tmpCenter.add(tmpjp4);
        
        byCoordPanel.add(tmpjp1, BorderLayout.PAGE_START);
        //byCoordPanel.add(tmpjp2);
        //byCoordPanel.add(tmpjp3);
        byCoordPanel.add(tmpCenter, BorderLayout.CENTER);
        byCoordPanel.add(tmpjp5, BorderLayout.PAGE_END);
        
        
        //set up the Waypoint panel
        tmpjp1 = new JPanel();
        tmpjp2 = new JPanel();
        tmpjp3 = new JPanel();
        tmpjp4 = new JPanel();
        tmpjp5 = new JPanel();
        tmpCenter = new JPanel();
        tmpCenter.setLayout(new BoxLayout(tmpCenter, BoxLayout.Y_AXIS));        
        
        /*tmpjp1.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp2.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp3.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp4.setBorder(BorderFactory.createLineBorder(Color.black));
        tmpjp5.setBorder(BorderFactory.createLineBorder(Color.black));*/
        
        tmpjp1.add(jlbWaypoint);
        tmpjp2.add(jlbPrevNextWp);
        tmpjp2.add(jspPrevNextWp);
        tmpjp2.add(jbtGoToWp);
        tmpjp3.add(jlbWpLat);
        tmpjp3.add(jlbWpLong);
        tmpjp4.add(jbtAddWp);
        tmpjp4.add(jbtDeleteWp);
        tmpjp5.add(jbtLoad);
        tmpjp5.add(jbtSave);
        
        tmpCenter.add(tmpjp2);
        tmpCenter.add(tmpjp3);
        tmpCenter.add(tmpjp4);
        
        waypointPanel.add(tmpjp1, BorderLayout.PAGE_START);
        //waypointPanel.add(tmpjp2);
        //waypointPanel.add(tmpjp3);
        waypointPanel.add(tmpCenter, BorderLayout.CENTER);
        waypointPanel.add(tmpjp5, BorderLayout.PAGE_END);
        
        
        //add all panels to the main container panel
        containerPanel.add(byNamePanel);
        containerPanel.add(byCoordPanel);
        containerPanel.add(waypointPanel);
        
        //add the event handlers
        LatLongBtnHandler latlongbtnListener = new LatLongBtnHandler();
        jbtByCoord.addActionListener(latlongbtnListener);
        AddWaypointHandler addwpbtnListener = new AddWaypointHandler();
        jbtAddWp.addActionListener(addwpbtnListener);
        GoToWaypointHandler gotowpbtnListener = new GoToWaypointHandler();
        jbtGoToWp.addActionListener(gotowpbtnListener);
        WPSpinnerHandler wpspinnerListener = new WPSpinnerHandler();
        jspPrevNextWp.addChangeListener(wpspinnerListener);
        DeleteWaypointHandler delwpbtnListener = new DeleteWaypointHandler();
        jbtDeleteWp.addActionListener(delwpbtnListener);
        SaveFileHandler savewpbtnListener = new SaveFileHandler();
        jbtSave.addActionListener(savewpbtnListener);
        LoadFileHandler loadwpbtnListener = new LoadFileHandler();
        jbtLoad.addActionListener(loadwpbtnListener);
        jbtByName.addActionListener(new ByAddressBtnHandler());
        jbtSearchCoord.addActionListener(new DisplayInfoBtnHandler());
        LocationCBHandler cbListener = new LocationCBHandler();
        jcbCountry.addItemListener(cbListener);
        jcbCity.addItemListener(cbListener);
      
        jXMapKit1.getMainMap().addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent arg0) {				 				 
				 //jXMapKit allows for Longitude to go outside -+180, reset the position when looping to avoid this
				 if (jXMapKit1.getCenterPosition().getLongitude() > 180) 
					 jXMapKit1.setCenterPosition(new GeoPosition(jXMapKit1.getCenterPosition().getLatitude(), -180));
				 else if (jXMapKit1.getCenterPosition().getLongitude() < -180) 
					 jXMapKit1.setCenterPosition(new GeoPosition(jXMapKit1.getCenterPosition().getLatitude(), 180));
				 statusMessageLabel.setText(jXMapKit1.getCenterPosition().toString());				
			}

			public void mouseMoved(MouseEvent arg0) {
				 statusMessageLabel.setText(jXMapKit1.getCenterPosition().toString());				
			}        	
        });
        
        //add list of countries from the country.txt file
        jcbCountry.addItem("");
        InputStream is = this.getClass().getResourceAsStream("country.txt");
        Scanner tmpScan = new Scanner(is);
        int endL = Integer.parseInt(tmpScan.nextLine());
        for (int i = 0; i < endL; i++)
        {
        	jcbCountry.addItem(tmpScan.nextLine());
			double tmpLat = Double.parseDouble(tmpScan.nextLine());
			double tmpLong = Double.parseDouble(tmpScan.nextLine());

			Waypoint wp = new Waypoint(tmpLat, tmpLong);
			countryList.add(wp);			
        }
        tmpScan.close();
        
        
        //add list of cities from the city.txt file
        jcbCity.addItem("");
        is = this.getClass().getResourceAsStream("city.txt");
        tmpScan = new Scanner(is);
        endL = Integer.parseInt(tmpScan.nextLine());
        for (int i = 0; i < endL; i++)
        {
        	jcbCity.addItem(tmpScan.nextLine());
			double tmpLat = Double.parseDouble(tmpScan.nextLine());
			double tmpLong = Double.parseDouble(tmpScan.nextLine());

			Waypoint wp = new Waypoint(tmpLat, tmpLong);
			cityList.add(wp);			
        }
        tmpScan.close();
        
        //remove default waypoint
        drawWaypoints();
 
        
        // *** end code insertion ***       
        
        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
            .add(jtbToolbar)
            .add(mainPanelLayout.createSequentialGroup()            	
                //.add(jButton1)               
                .add(containerPanel))
                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                //.add(jButton2)                
                //.add(183, 183, 183))
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
    	button.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));    	
    	button.addActionListener(new ToolBarHandler());
    	toolbar.add(button);
    	toolbar.setFloatable(false);    	
    }
    
    /* inserted by Michael Afidchao
     * handle the boundaries of Latitude co-ordinates
     * ensure that latitude is within -/+90 
     */
    public double checkLatitude(double lat)
    {
    	if (lat < -90)
    		lat = -90;
    	else if (lat > 90)
    		lat = 90;
    	return lat;
    }
    
    /* inserted by Michael Afidchao
     * handle the boundaries of longitude co-ordinates
     * ensure that longitude is within -/+180
     */
    public double checkLongitude(double lon)
    {
    	if (lon < -180)
    		lon = -180;
    	else if (lon > 180)
    		lon = 180;
    	return lon;
    }
    
    /* inserted by Michael Afidchao
     * draw the waypoints on the overlay
     */
    public void drawWaypoints() {
        Set<Waypoint> waypoints = new HashSet<Waypoint>();
        waypoints.addAll(waypointList);        
        
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
    }

    
    /**
     * inserted by Michael Afidchao 
     * Event Handler for the Search by Address button
     * uses the Nominatim tool on OpenStreetMap.org: http://wiki.openstreetmap.org/wiki/Nominatim#Reverse_Geocoding_.2F_Address_lookup 
     * HTTP Request code provided by: http://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
     */
    class ByAddressBtnHandler implements ActionListener
    {
    	public void actionPerformed (ActionEvent e){
    		URL address;
    		try
    		{
    			//yahoo = new URL("http://www.yahoo.com/");
                //address = new URL("http://nominatim.openstreetmap.org/reverse?format=xml&lat=52.5487429714954&lon=-1.81602098644987&addressdetails=1");
    			//http://nominatim.openstreetmap.org/search?q=Toronto,+ON,+Canada&format=xml
    			//address = new URL("http://nominatim.openstreetmap.org/search/" + jtfAddress.getText().trim() + "?format=xml");
    			//address = new URL("http://nominatim.openstreetmap.org/search/135%20pilkington%20avenue,%20birmingham?format=xml");
    			//address = new URL("http://nominatim.openstreetmap.org/search/70%20the%20pond%20road,Toronto?format=xml");
    			String tmpSearch = jtfAddress.getText().trim();
    			tmpSearch = tmpSearch.replaceAll(" ", "+");  //replace all blank spaces with a + to create a proper URL
    			address = new URL("http://nominatim.openstreetmap.org/search?q=" + tmpSearch + "&format=xml");
    			URLConnection tmpConn = address.openConnection();		    		
    			BufferedReader in = new BufferedReader(new InputStreamReader(tmpConn.getInputStream()));
    			
    			String tmpInput;
    			double tmpLat = 999.0, tmpLong = 999.0;    //0.0 is a valid location, despite nothing existing there (yet)
    			while ((tmpInput = in.readLine()) != null && tmpLat != 0.0) {    				
    				//look for the first line that contains the latitude/longitude
    				System.out.println(tmpInput);
    				if (tmpInput.indexOf("lat='") > -1)
    				{
    					tmpLat = Double.parseDouble(tmpInput.substring(tmpInput.indexOf("lat='") + 5, tmpInput.indexOf("lon='") - 2));
    					tmpLong = Double.parseDouble(tmpInput.substring(tmpInput.indexOf("lon='") + 5, tmpInput.indexOf("display_name") - 2));
    					System.out.println (tmpLat + " " + tmpLong);
    					System.out.println (tmpInput.substring(tmpInput.indexOf("lat='") + 5, tmpInput.indexOf("lon='") - 2));
    					System.out.println (tmpInput.substring(tmpInput.indexOf("lon='") + 5, tmpInput.indexOf("display_name") - 2));
    				}
    			}
    			in.close();
    			
    			//if no lat/long was found, let's just throw an exception 
    			if (tmpLat == 999.0)
    				throw new Exception();
    			
    			jXMapKit1.setCenterPosition(new GeoPosition(tmpLat, tmpLong));
    			statusMessageLabel.setText(jXMapKit1.getCenterPosition().toString());	
    				
    			System.out.println(address.toString());
    			} 
    			catch (Exception ex)
    			{
    				JOptionPane.showMessageDialog( null, "Co-ordinates could not be found for address:\n" + jtfAddress.getText()
    						, "Invalid address", JOptionPane.INFORMATION_MESSAGE );
    			}
    			
           
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for jumping to locations when selecting an item from the Country/City combo boxes
     */
    class LocationCBHandler implements ItemListener {
    	@SuppressWarnings("unchecked")
		public void itemStateChanged (ItemEvent e)
    	{
    		int tmpIndex = ((JComboBox<String>)e.getSource()).getSelectedIndex();
    		if (e.getSource() == jcbCountry && e.getStateChange() == ItemEvent.SELECTED && tmpIndex > 0)
    		{    	    			
   				jXMapKit1.setCenterPosition(countryList.get(tmpIndex - 1).getPosition());
   				((JComboBox<String>)e.getSource()).setSelectedIndex(0);
   				statusMessageLabel.setText(jXMapKit1.getCenterPosition().toString());	
    		} else if  (e.getSource() == jcbCity && e.getStateChange() == ItemEvent.SELECTED && tmpIndex > 0){
   				jXMapKit1.setCenterPosition(cityList.get(tmpIndex - 1).getPosition());
   				((JComboBox<String>)e.getSource()).setSelectedIndex(0);
   				statusMessageLabel.setText(jXMapKit1.getCenterPosition().toString());	    		
    		}
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Display Address Info button
     */
    class DisplayInfoBtnHandler implements ActionListener
    {
    	public void actionPerformed (ActionEvent e){
    		URL address;
    		String tmpStr = "";
    		try
    		{    			
    			double tmpLat, tmpLong;
    			tmpLat = jXMapKit1.getCenterPosition().getLatitude();
    			tmpLong = jXMapKit1.getCenterPosition().getLongitude();
    			
    			String tmpSearch = jtfAddress.getText().trim();
    			tmpSearch = tmpSearch.replaceAll(" ", "+");  //replace all blank spaces with a + to create a proper URL
    			
    			//http://nominatim.openstreetmap.org/reverse?format=xml&lat=52.5487429714954&lon=-1.81602098644987&addressdetails=1
    			address = new URL("http://nominatim.openstreetmap.org/reverse?format=xml&lat=" + tmpLat + "&lon=" + tmpLong + "&addressdetails=1");
    			
    			URLConnection tmpConn = address.openConnection();		    		
    			BufferedReader in = new BufferedReader(new InputStreamReader(tmpConn.getInputStream()));
    			
    			String tmpInput;
    			//double tmpLat = 999.0, tmpLong = 999.0;    //0.0 is a valid location, despite nothing existing there (yet)
    			//while ((tmpInput = in.readLine()) != null && tmpLat != 0.0) {    				
    			while ((tmpInput = in.readLine()) != null) {
    				System.out.println (tmpInput);
    				//check if we have entered the <addressparts> block
    				if (tmpInput.contains("<addressparts>"))
    				{
    					//extract only certain elements of the block: number, street, city, state and country
   						//tmpStr += tmpInput;
    					if (tmpInput.contains("<house_number>"))
    						tmpStr += tmpInput.substring(tmpInput.indexOf("<house_number>") + 14, tmpInput.indexOf("</house_number>")) + "\n";
    					if (tmpInput.contains("<road>"))
    						tmpStr += tmpInput.substring(tmpInput.indexOf("<road>") + 6, tmpInput.indexOf("</road>")) + "\n";
    					if (tmpInput.contains("<city>"))
    						tmpStr += tmpInput.substring(tmpInput.indexOf("<city>") + 6, tmpInput.indexOf("</city>")) + "\n";
    					if (tmpInput.contains("<state>"))
    						tmpStr += tmpInput.substring(tmpInput.indexOf("<state>") + 7, tmpInput.indexOf("</state>")) + "\n";
    					if (tmpInput.contains("<country>"))
    						tmpStr += tmpInput.substring(tmpInput.indexOf("<country>") + 9, tmpInput.indexOf("</country>")) + "\n";
   						
    				}
    				    				
    			}
    			in.close();
    			
    			System.out.println (tmpStr);
    
    			//if no <addressparts> was found, let's just throw an exception 
    			if (tmpStr.equals(""))
    				throw new Exception();
    			
    			jXMapKit1.setCenterPosition(new GeoPosition(tmpLat, tmpLong));
    			statusMessageLabel.setText(jXMapKit1.getCenterPosition().toString());	
    				
    			System.out.println(address.toString());
    			} 
    			catch (Exception ex)
    			{
    				System.out.println (ex.getMessage());
    				// jtfAddress.setText("Invalid address");
    				tmpStr = "No address data available";
    			}    			
    		JOptionPane.showMessageDialog( null, tmpStr, "Reverse Geocode", JOptionPane.INFORMATION_MESSAGE );
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Latitude/Longitude search button
     */
    class LatLongBtnHandler implements ActionListener
    {
    	public void actionPerformed (ActionEvent e)
    	{
    		//set null and blank values to 0
    		if (jtfLatitude.getText() == null || jtfLatitude.getText().trim().equals(""))       		
    			jtfLatitude.setText("0");    		
    		if (jtfLongitude.getText() == null || jtfLongitude.getText().trim().equals(""))
    			jtfLongitude.setText("0");
    		
    		//handle the boundaries of latitude/longitude
    		jtfLatitude.setText(Double.toString(checkLatitude(Double.parseDouble(jtfLatitude.getText()))));
    		jtfLongitude.setText(Double.toString(checkLongitude(Double.parseDouble(jtfLongitude.getText()))));

    		jXMapKit1.setCenterPosition(new GeoPosition(Double.parseDouble(jtfLatitude.getText()), Double.parseDouble(jtfLongitude.getText())));
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Waypoint addition button
     */
    class AddWaypointHandler implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		waypointList.add(new Waypoint(jXMapKit1.getCenterPosition()));
    		waypointModel.setMaximum(Integer.parseInt(waypointModel.getMaximum().toString()) + 1);
    		
            drawWaypoints();           
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Waypoint deletion button
     */
    class DeleteWaypointHandler implements ActionListener
    {
    	public void actionPerformed (ActionEvent e)
    	{
    		int tmpWp = Integer.parseInt(waypointModel.getNumber().toString());
    		if (tmpWp != 0)   		
    		{
    			waypointList.remove(tmpWp - 1);
    			
    			//modify the spinner so the current value is the previous one 
    			//and decrease the maximum value
    			waypointModel.setValue(tmpWp - 1);
    			waypointModel.setMaximum(Integer.parseInt(waypointModel.getMaximum().toString()) - 1);
    			
    			drawWaypoints();
    		}
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Go To Waypoint button
     */
    class GoToWaypointHandler implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		int tmpWp = Integer.parseInt(waypointModel.getNumber().toString());
    		if (tmpWp != 0)
    			jXMapKit1.setCenterPosition(waypointList.get(tmpWp - 1).getPosition());
    	}
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Save Waypoints File button
     */
    class SaveFileHandler implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		JFileChooser fc = new JFileChooser();
    		int tmpReturn = fc.showSaveDialog(null);
    		if (tmpReturn == JFileChooser.APPROVE_OPTION)
    		{
    			File filename = fc.getSelectedFile();
    			try
    			{
    				Writer fileOut = new OutputStreamWriter (new FileOutputStream(filename));
    				try
    				{
    					fileOut.write(waypointList.size() + "\n");
    					for (int i = 0; i < waypointList.size(); i++)
    					{    					
    					
    						fileOut.write(waypointList.get(i).getPosition().getLatitude() + "\n");
    						fileOut.write(waypointList.get(i).getPosition().getLongitude() + "\n");
    					}
    				}
    				catch (IOException ex)  //catch exceptions thrown by bad file output writing
    				{
    					  JOptionPane.showMessageDialog( null, ex.getMessage(), "Error with file output", JOptionPane.INFORMATION_MESSAGE );
    				}
    				finally{
    					fileOut.close();
    				}    		    			
    			} 
    			catch (IOException ex) //catch exception thrown by invalid opening of file for output
    			{    			
    				JOptionPane.showMessageDialog( null, ex.getMessage(), "Error opening file", JOptionPane.INFORMATION_MESSAGE );
    			}    			
    		}
    	}
    
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Load Waypoints File button
     */
    class LoadFileHandler implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		JFileChooser fc = new JFileChooser();
    		int tmpReturn = fc.showSaveDialog(null);
    		
    		int tmpSize;
    		ArrayList<Waypoint> tmpWpList = new ArrayList<Waypoint>();
    		
    		if (tmpReturn == JFileChooser.APPROVE_OPTION)    		
    		{
    			File filename = fc.getSelectedFile();
    			try
    			{
    				Scanner tmpScan = new Scanner(new FileInputStream(filename));

    				try
    				{
    					//the first line should be a number that stores the amount of
    					//waypoints stored in the file
    					tmpSize = Integer.parseInt(tmpScan.nextLine());
    					
    					for (int i = 0; i < tmpSize; i++)
    					{
    						double tmpLat = Double.parseDouble(tmpScan.nextLine());
    						double tmpLong = Double.parseDouble(tmpScan.nextLine());

    						Waypoint wp = new Waypoint(tmpLat, tmpLong);
    						tmpWpList.add(wp);    						
    					}
    					
    					//set the waypoint list to the loaded list and reset the spinner
    					//value to 0 and maximum value to the new maximum
    					waypointList = tmpWpList;
    					waypointModel.setValue(0);
    					waypointModel.setMaximum(tmpSize);
    					
    					drawWaypoints();
    				} 
    				catch (Exception ex)  //catch all possible exceptions after successful file open
    				{    				
    					JOptionPane.showMessageDialog( null, ex.getMessage(), "Error with file reading", JOptionPane.INFORMATION_MESSAGE );
    				}
    				finally{
    					tmpScan.close();
    				}    				    			
    			} catch (IOException ex) //catch exception thrown by invalid file opening for input
    			{    			
    				JOptionPane.showMessageDialog( null, ex.getMessage(), "Error opening file", JOptionPane.INFORMATION_MESSAGE );
    			}
    		}    		
    	}
    
    }
    
    /* inserted by Michael Afidchao
     * Event Handler for the Waypoint Spinner
     * alters the Lat/Long waypoint labels
     */
    class WPSpinnerHandler implements ChangeListener
    {
    	public void stateChanged(ChangeEvent e)
    	{
    		int tmpWp = Integer.parseInt(waypointModel.getNumber().toString());
    		if (tmpWp == 0)
    		{
    			jlbWpLat.setText("Latitude: n/a");
    			jlbWpLong.setText("Longitude: n/a");
    		}
    		else
    		{
    			DecimalFormat tmpFormat = new DecimalFormat("0.000000");    			
    			jlbWpLat.setText("Latitude: " + tmpFormat.format(waypointList.get(tmpWp - 1).getPosition().getLatitude()));
    			jlbWpLong.setText("Longitude: " + tmpFormat.format(waypointList.get(tmpWp - 1).getPosition().getLongitude()));
    		}    		
    	}    
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
