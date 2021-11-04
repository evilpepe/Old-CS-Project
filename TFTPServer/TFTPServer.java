
package TFTPServer;

//Import Statements
import TFTPAccessories.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;

/**
 * @name TFTPServer
 * @description: 
 * 
 * -Methods-
 * main: instantiates an instance of this GUI class
 * start: Called automatically after launch sets up javaFX
 * sendAlert: opens a new window with an alert message that can be customized
 * setDir: Updates the directory the server is poiting towards for sending and recieving files
 * doStart: starts the server thread
 * doStop: stops the server thread
 * log: appends a line of text to taLog
 * setConnections: updates the number of concurrent connections to the server
 * getConnections: gets the number of concurrent connections to the server
 * getDir: gets the current server directory path
 */
public class TFTPServer extends Application implements TFTPConstants
{
    //Class Constants

    //Class Variables
    private Stage stage;
    private Scene scene;

    //Root Layout Manager
    private VBox root;

    //Other Layout Managers
    private VBox mainContent = new VBox(8);
    private FlowPane fp1 = new FlowPane(8,8);
    private FlowPane fp2 = new FlowPane(8,8);
    private FlowPane fp3 = new FlowPane(8,8);
    private FlowPane fp4 = new FlowPane(8,8);

    //TextFields
    private TextField tfServer = new TextField();
    private TextField tfStatus = new TextField();
    private static TextField tfConnections = new TextField();
    
    //TextAreas
    private static TextArea taData = new TextArea();
    private TextArea taDir = new TextArea();

    //Lables
    private Label lblServer = new Label("Server IP:");
    private Label lblDir = new Label("Current Directory:");
    private Label lblStatus = new Label("Status:");
    private Label lblConnections = new Label("Connections:");
    private Label lblServerTime = new Label("Current Server Time:");
    private static Label lblTime = new Label("00:00:00");

    //Menu
    private MenuBar mbarBar = new MenuBar();
        // File Menu
        private Menu mnuFile = new Menu("File");
            // Items
            private MenuItem miDir = new MenuItem("Set Dir");
        // System Menu
        private Menu mnuManage  = new Menu("Manage");
            // Items
            private MenuItem miStart = new MenuItem("Start");
            
    //Other
    private ServerThread serverThread;
    private static String directory;
    private java.util.Timer clock;

    /**
     * Description: instantiates an instance of this GUI class
     * @param args passes any arguments through
     */
    public static void main(String[] args)
    {
        launch(args);
    } //END main

    /**
     * Description: Called automatically after launch sets up javaFX
     * @param _stage windows all the GUI items are placed in
     * @throws Exception 
     */
    @Override
    public void start(Stage _stage) throws Exception
    {
        //GUI Layout
            //Menu
            mnuFile.getItems().addAll(miDir);
            mnuManage.getItems().addAll(miStart);
            mbarBar.getMenus().addAll(mnuFile, mnuManage);
            root = new VBox(mbarBar);
            
            //Server Address
            fp1.getChildren().addAll(lblServer, tfServer);
            fp1.setAlignment(Pos.CENTER);
            tfServer.setPrefColumnCount(10);
            tfServer.setAlignment(Pos.CENTER);
            tfServer.setEditable(false);
            
            tfServer.setText(InetAddress.getLocalHost().getHostAddress());
            mainContent.getChildren().add(fp1);
            
            //Current Directory
            fp2.getChildren().addAll(lblDir, taDir);
            fp2.setAlignment(Pos.CENTER);
            taDir.setPrefColumnCount(15);
            taDir.setPrefRowCount(1);
            taDir.setEditable(false);
            mainContent.getChildren().add(fp2);
            
            //Status & Connections
            fp3.getChildren().addAll(lblStatus, tfStatus, lblConnections, tfConnections);
            fp3.setAlignment(Pos.CENTER);
            tfStatus.setPrefColumnCount(7);
            tfConnections.setPrefColumnCount(4);
            tfStatus.setEditable(false);
            tfConnections.setEditable(false);
            tfStatus.setStyle("-fx-background-color: red;");
            tfStatus.setText("Stopped");
            tfStatus.setAlignment(Pos.CENTER);
            tfConnections.setAlignment(Pos.CENTER);
            tfConnections.setText("0");
            mainContent.getChildren().add(fp3);
            
            //Server Clock
            fp4.getChildren().addAll(lblServerTime, lblTime);
            fp4.setAlignment(Pos.CENTER);
            mainContent.getChildren().add(fp4);
            
            //Log
            taData.setPrefRowCount(14);
            taData.setWrapText(true);
            taData.setEditable(false);
            mainContent.getChildren().add(taData);
        
            //Add Main Content to Root
            mainContent.setPadding(new Insets(8, 0, 0, 0));
            root.getChildren().add(mainContent);

        //Setup Scene/Stage
        stage = _stage;
        stage.setTitle("ACE : TFTP Server");
        scene = new Scene(root, 300, 400);
        stage.setScene(scene);
        stage.show();
        
        //Window Close Capture
        stage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            public void handle(WindowEvent evt)
            {
                doStop();
                System.exit(0);
            }
        });
        
        //Anon Change Dir Button Event Handler
        miDir.setOnAction(new EventHandler<ActionEvent>() { public void handle(ActionEvent evt) {
            setDir();
        }});
        
        //Anon Event Handler for the Start/Stop Button
        miStart.setOnAction(new EventHandler<ActionEvent>() { public void handle(ActionEvent evt) {
            // Get the button that was clicked
            String label = ((MenuItem)evt.getSource()).getText();
            
            // Switch on its name
            switch(label)
            {
                case "Start":
                    doStart();
                    break;
                case "Stop":
                    doStop();
                break;
            } //END SWITCH
        }});
        
        //Setup Server Clock
        clock = new java.util.Timer();
        clock.scheduleAtFixedRate(new ClockTask(lblTime), 0, 1000);
        
        //Other
        directory = new File(".").getCanonicalPath();
        taDir.setText(directory);
        
        taData.requestFocus(); //fixes the highlighted ip address upon start bug
        
    } //END start
    
    /**
     * Description: Updates the directory the server is poiting towards for sending and recieving files
     */
    private void setDir()
    {
        File selectedDirectory = null;
        
        try
        {
            // directory chooser so that user can select directory
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Set the Server's Directory:"); // setting stage title
            dc.setInitialDirectory(new File("."));
            selectedDirectory = dc.showDialog(stage);
            
            //check to see if it exists
            if(selectedDirectory.exists() && selectedDirectory.isDirectory())
            {
                //update directory
                directory = selectedDirectory.getCanonicalPath();
                taDir.setText(directory);
                log("Server Directory Updated: " + directory);
            }
        }
        catch (NullPointerException npe) //thrown if user cancels Dir Chooser without choosing 
        {
            return;
        }
        catch (IOException ioe)
        {
            log("ExThrow SRV setDir: " + ioe);
            return;
        }
        
    } //END setDir
    
    /**
     * Description: starts the server thread
     */
    private void doStart()
    {
        serverThread = new ServerThread();
        serverThread.start();
        miStart.setText("Stop");
        tfStatus.setStyle("-fx-background-color: green;");
        tfStatus.setText("Running");
    } //END doStart
    
    /**
     * Description: stops the server thread
     */
    private void doStop()
    {
        try
        {
            serverThread.stopServer();
        }
        catch(NullPointerException npe)
        {
            return;
        }
        miStart.setText("Start");
        tfStatus.setStyle("-fx-background-color: red;");
        tfStatus.setText("Stopped");
    } //END doStop
    
    /**
     * Description: appends a line of text to taLog
     * @param message the message to appended to the screen
     */
    public static void log(String message)
    {
        Platform.runLater(new Runnable(){
            public void run()
            {
                taData.appendText("<" + lblTime.getText() + "> " + message + "\n");
            }
        });
    } //END log
    
    /**
     * Description: updates the number of concurrent connections to the server
     * @param num new number of connections
     */
    public static void setConnections(int num)
    {
        Platform.runLater(new Runnable(){
            public void run()
            {
                tfConnections.setText(num + "");
            }
        });
    } //END updateConnections

    /**
     * Description: gets the number of concurrent connections to the server
     * @return the current number of connections
     */
    public static String getConnections()
    {
        return tfConnections.getText();
    } //END updateConnections
    
    /**
     * Description: gets the current server directory path
     * @return current dir as a string
     */
    public static String getDir()
    {
        return directory;
    } //END updateConnections
    
} //END TFTPServer
