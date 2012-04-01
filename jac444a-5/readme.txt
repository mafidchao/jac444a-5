Michael Afidchao - JAC444A
Assignment #2
Instructor: Peter Liu

Submitted: April 1, 2012
Also available on github: https://github.com/mafidchao/jac444a-5

This program is based on Application 2: JXMapViewer.

Application 2 original sample code provided by Josh Marinacci:
http://today.java.net/pub/a/today/2007/10/30/building-maps-into-swing-app-with-jxmapviewer.html

JDK compiler version: 1.7
JVM/JRE version: JRE7

Although the JXMapViewer application was originally created in NetBeans, this derived application was developed in Eclipse.
Here are the required libraries used to run the ORIGINAL application (no libraries were added for my application):
appframework-1.0.2.jar
swing-layout-1.0.3.jar
swing-worker-1.1.jar
swing-worker.jar
swingx-bean.jar
swingx-ws-2007_10_14.jar
swingx.jar
MapApp.jar
commons-codec-1.3.jar
commons-httpclient-3.0.1.jar
commons-logging-1.1.jar
jdom.jar
json.jar
jtidy-r7.jar
rome-0.8.jar
xercesImpl.jar

These libraries are contained within the original application source project, located under /lib and /dist directories.

Class files are contained within jac444a-5/build/classes, use the Navigator view to see them in Eclipse.

Running the application in Eclipse:
1. Download and extract Application 2: http://projects.joshy.org/articles/MapApp/MapApp.zip
2. Import the application into a Java Project. See wiki for instructions:
https://matrix.senecac.on.ca:8443/wiki/jac444a/index.php/Assignment_2_Winter_2012#JXMapViewer_Application_.28Application_2.29
It is recommended to use Easy Setup by Chad Pilkey.
                        OR
 2a) Create an empty Java project, set to use JDK 1.7 and JRE7
 2b) Right click the Java Project in the Project Explorer tab. Select Import.
 2c) Under General -> File System, select the MapApp folder that was extracted from MapApp.zip.
 2d) Check the box in the top left to select all resources. Select Yes to All if asked to overwrite any files. Click Finish.
 2e) Right click the Java Project in the Project Explorer tab. Select Build Path -> Configure Build Path. Click the Libraries tab.
 2f) Click the Add JARs button. Add all the JARs found within the following paths. Including the JRE 1.7 library, there should be
     21 entries total. These are the paths:
     /lib
     /lib/swingx-ws-2007_10_14-bin
     /lib/swingx-ws-2007_10_14-bin/cobundle
     /lib/swingx-ws-2007_10_14-bin/optional
     /dist
     /dist/lib
  2g) The project should be set up now. Test it by expanding /src and Right Click the mapapp package. Select Run As -> Java Application.
3. Right click the Java Project in the Project Explorer tab. Select Import.
4. Under General -> Archive File, Import the JAR file a2_mdafidchao.jar.
   Select Yes to All if asked to overwrite any files.
5. Expand /src. Right-click on the mapapp package. Select Run As -> Java Application.

After creating the JAR file, I've imported the JAR file into Eclipse again. I've checked that there's no compilation error.
I will lose 50% if that's not the case.