

package TFTPClient;

//Import Statements
import TFTPAccessories.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import java.io.*;
import java.net.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.text.Font;

/**
 * @name TFTPClient
 * @description: 
 * 
 * -Methods-
 * main: instantiates an instance of this GUI class
 * start: Called automatically after launch sets up javaFX
 * sendAlert: opens a new window with an alert message that can be customized
 */
public class TFTPClient extends Application implements TFTPConstants
{
    //Class Constants

    //Class Variables
    private Stage stage;
    private Scene scene;

    //Root Layout Manager
    private VBox root = new VBox(8);

    //Other Layout Managers
    ScrollPane scroll = new ScrollPane();

    //TextFields
    TextField tfServer = new TextField();
    TextField tfFolder = new TextField();

    //TextAreas
    TextArea taLog = new TextArea();

    //Buttons
    Button btnFolder = new Button("Choose Folder");
    Button btnUpload = new Button("Upload");
    Button btnDownload = new Button("Download");

    //Lables
    Label lblServer = new Label("Server:");
    Label lblLog = new Label("Log");
    
    // UDP
    private final static int pktLength = 512;
    private InetAddress serverAddress;
    private DatagramSocket socket;
    private byte[] requestByteArray;
    
    // IO
    private DataInputStream dis = null;
    private String filePath;

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
        taLog.setPrefHeight(300); // setting taLog width
        
        // flowpane with label server and textfield server
        FlowPane row1 = new FlowPane(8, 8);
        row1.getChildren().addAll(lblServer, tfServer);
        row1.setAlignment(Pos.CENTER); // positioning to center
        
        // flowpane with upload and download buttons
        FlowPane row4 = new FlowPane(8, 8);
        row4.getChildren().addAll(btnUpload, btnDownload);
        row4.setAlignment(Pos.CENTER); // positioning to center
         
        // adding scrollpane to file textfield
        scroll.setContent(tfFolder);
        
        // adding both flowpanes and all other buttons, textareas, textfields and labels to root
        root.getChildren().addAll(row1, btnFolder, tfFolder, row4, lblLog, taLog);
        root.setAlignment(Pos.CENTER); // positiong to center
        
        // changing font of textfields and text area
        tfFolder.setFont(Font.font("MONOSPACED"));
        tfServer.setFont(Font.font("MONOSPACED"));
        taLog.setFont(Font.font("MONOSPACED"));

        //Setup Scene/Stage
        stage = _stage;
        stage.setTitle("ACE : TFTP Client");
        scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();
        
        // making sure window can't be resized
        stage.setResizable(false);
        
        // setting server textfield
        tfServer.setText("localhost");
        
        //Window Close Capture
        stage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            public void handle(WindowEvent evt) { System.exit(0); }
        });
        
        //Anon inner classes for button activation
         btnFolder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                 doChooseDirectory();
             }
         });
         
         btnDownload.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                 doDownload();
             }
         });
         
         btnUpload.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                 doUpload();
             }
         });
                 
    } //END start
    
    /**
     * Description: opens a new window with an alert message that can be customized
     * @param type type of alert message
     * @param message string to put in the dialog box
     * @param header message at the top of the alert
     */
    private void sendAlert(Alert.AlertType type, String message, String header)
    {
        Platform.runLater(new Runnable(){
            public void run()
            {
                Alert alert = new Alert(type, message);
                alert.setHeaderText(header);
                alert.showAndWait();
            }
        });

    } //END sendAlert
    
   /* method for user to choose a directory */
   public void doChooseDirectory(){
      // directory chooser so that user can select directory
      DirectoryChooser dc = new DirectoryChooser();
      dc.setTitle("Choose a folder:"); // setting stage title
      File selectedDirectory = dc.showDialog(stage);
      
      // displaying directory path name in textfield folder
      tfFolder.setText(selectedDirectory.getAbsolutePath());
      filePath = selectedDirectory.getAbsolutePath();
   }
   
   // if user selects download
   public void doDownload(){
      //File chosen = null;
      
      // text input dialoug so that user can select a file can be downloaded
      TextInputDialog input = new TextInputDialog();
      input.setHeaderText("Enter the name of the remote file to download: ");
      input.showAndWait();
      
      // file chooser so that user can choose the name of the file for saving the download
      FileChooser fc = new FileChooser();
      
      File initialPath = new File(tfFolder.getText());
      fc.setInitialDirectory(initialPath);
      
      File chosen = fc.showOpenDialog(stage);
      
      // if user enters name in text dialog, then show filechooser
      if (input.getEditor().getText() != null){ 
         // making sure chosen file is valid and if it is, creating a new thread
         DownloadThread dt = new DownloadThread(input.getEditor().getText(), chosen);
         dt.start();
      } else {
         log("Please enter a name in the textfield");
      }
   }
   
   // if user selects upload
   public void doUpload(){
      // file chooser so that user can select a file can be uploaded
      FileChooser fc = new FileChooser();
      
      File initialPath = new File(tfFolder.getText());
      fc.setInitialDirectory(initialPath);
      
      File chosen = fc.showOpenDialog(stage);
      
      TextInputDialog tid = new TextInputDialog();
      tid.setHeaderText("Enter a name for the file for saving the upload");
      tid.showAndWait();
      
      // if the file selected exists, ask the user is name for saving the uploda
     if(tid.getEditor().getText() != null){ 
         UploadThread ut = new UploadThread(tid.getEditor().getText(), chosen);
         ut.start();
     } else {
       log("No file name was entered for saving the file");
     }
   }
   
   // logging in a thread safe manner
   private void log(String message){
      Platform.runLater(new Runnable() {
         public void run() {
            taLog.appendText(message + "\n");
         }
      });
   }
   
   class DownloadThread extends Thread{
      // file
      String name = ""; // name of file
      File chosen = null; // selected file
      // String chosenPath = new String(chosen.getPath());
      
      // TFTP
      DatagramPacket packet = null;
      DatagramManager defaultManager = new DatagramManager();
      DatagramManager dgmWrite = null;
      byte[] maxByte = new byte[1500]; // creating maximum byte size
      
      // IO
      DataOutputStream dos = null;
      FileOutputStream fos = null;
      
      public DownloadThread(String name, File chosen){
         this.name = name;
         this.chosen = chosen;
      
      try{
         DatagramSocket dgs = new DatagramSocket();
         InetAddress serverAddress = InetAddress.getByName(tfServer.getText()); 
         dgmWrite = new DatagramManager(RRQ, serverAddress, TFTP_PORT, name, "octet"); // building WRQ packet
         
         // recording when server is connected
         log("Connected to server"); 
         
         // recording when download has begun in text area
         log("Downloading " + name);
      } catch (Exception e){
         log("Can not connect to server");
      }
    }
    
    public void run(){ 
   
      try{
         // building datagram packet
         //packet = new DatagramPacket(maxByte, MAX_PACKET_SIZE);
         socket = new DatagramSocket(0);
         socket.setSoTimeout(1000);
         packet = dgmWrite.build();
         socket.send(packet);
         log("Requesting " + dgmWrite.getFileName());
         
      } catch (IOException ioe) {
         log("Error creating packet");
      }
  
      
        int dataLeft = 0;
        boolean done = false;
        DatagramPacket pktRecieve = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
        DatagramPacket pktSend;
        DatagramManager send;
        DatagramManager recieve = new DatagramManager();
        int blockNum = 0;
      
      try
        {
            log("Opening File: " + dgmWrite.getFileName());
            //open FIS to convert file to bytes
            dos = new DataOutputStream(new FileOutputStream(new File(tfFolder.getText() + File.separator + dgmWrite.getFileName())));
            
            while(!done)
            {
                log("Packet Recieved");
                
                socket.receive(pktRecieve);
                //Break down recieved packet
                recieve.dissect(pktRecieve);
                
                //Chack to see if its DATA
                if(recieve.getOpcode() != DATA)
                {
                    doError(recieve, ILLOP, "Unexpected or Illegal Opcode Recieved");
                    return;
                }
                
                //if the program gets this far the packet must be correct
                //increase block number
                blockNum++;
                
                //write data
                dos.write(recieve.getData(), 0, recieve.getDataLen());
                dos.flush();
                
                //Send ACK to client
                send = new DatagramManager(ACK, recieve.getAddress(), recieve.getPort(), blockNum);
                pktSend = send.build();
                socket.send(pktSend);
                
                
                
                //check if that was last packet
                if(recieve.getDataLen() != 512)
                {
                    done = true;
                }

            } //END WHILE
            
            send = new DatagramManager(ACK, recieve.getAddress(), recieve.getPort(), blockNum);
            pktSend = send.build();
            socket.send(pktSend);
            
            log("Upload Finished");
            dos.close();
        }
        catch(SocketTimeoutException ste)
        {
            log("Upload: Timed Out");
            return;
        }
        catch(FileNotFoundException fnfe)
        {
            doError(recieve, NOTFD, "File Not Found");
            log(fnfe + "");
            return;
        }
        catch(IOException ioe)
        {
            log("ExThrow CT Read: " + ioe);
            return;
        }
        catch(Exception ex)
        {
            log("ExThrow CT Read: " + ex);
            doError(recieve, UNDEF, "File Not Found: " + ex);
            return;
        }   
    }
    
    /**
     * Description: sends an error to the client based on params
     * @param packet first packet recieved by the server
     * @param num error number
     * @param message error message
     */
    private void doError(DatagramManager packet, int num, String message)
    {
        log("ERROR: " + errorCode[num]);
        try
        {
            DatagramManager error = new DatagramManager(ERROR, packet.getAddress(), packet.getPort(), num, message);
            DatagramPacket errorpkt = error.build();
            socket.send(errorpkt);
        }
        catch (IOException ex)
        {
            log("ExThrow CT doError: " + ex);
        }

    } //END doError
  }
  
  class UploadThread extends Thread{
   // file
   File chosen;
   String name;
   
   // TFTP
   DatagramPacket sendingPckt = null;
   DatagramPacket recievedPckt = null;
   DatagramPacket packet = null;
   DatagramManager defaultManager = new DatagramManager();
   DatagramManager dgmWrite = null;
   byte[] maxByte = new byte[1500]; // creating maximum byte size
   
   // IO
   DataInputStream dis = null;
   FileInputStream fis = null;   

   // parameterized instructor that takes in file and remote name
   public UploadThread(String name, File chosen){
      this.chosen = chosen;
      this.name = name;

      try{
         // connecting to server
         InetAddress serverAddress = InetAddress.getByName(tfServer.getText()); 
         dgmWrite = new DatagramManager(WRQ, serverAddress, TFTP_PORT, name, "octet"); // building WRQ packet
         
         
         // recording when server is connected
         
         
         // recording when upload has begun in text area
         log("Uploading " + name);
      } catch (Exception e){
         log("Can not connect to server");
      }
   }
   
   public void run(){     
       try {
           socket = new DatagramSocket(0);
           socket.setSoTimeout(1000);
       } catch (SocketException ex) {
           log("Can not connect to server");
       }
         log("Connected to server"); 
      // building and sending packet   
      try{
        packet = dgmWrite.build(); // building packet
        socket.send(dgmWrite.build()); // sending to socket  
        log("Sending packet");
      } catch (IOException ioe) {
        log("Error sending packet");
      } catch (Exception e) {
         log("Error");
      }
      
      // inputting file in input streams
      try{
        fis = new FileInputStream(chosen);
        dis = new DataInputStream(fis);
       } catch (FileNotFoundException fnfe){
         log(name + " not found");
       }       
         
         // creating datagram packet 
         sendingPckt = new DatagramPacket(maxByte, MAX_PACKET_SIZE); 
         
         // sending datagrampacket to socket and catching timeout exceptions
         try{
            socket.receive(sendingPckt);
         } 
         catch(SocketTimeoutException ste){
            log("Socket timed out");
         }
         catch(IOException ioe){
            log("Error sending packet");
         }
         
         // notifying user when packet has been recieved
         log("Client has receieved packet");
         
         // decoding packet that was sent
         defaultManager.dissect(sendingPckt);
         
         // returning packet if it has been acknolwedged
         if(defaultManager.getOpcode() != ACK){
            DatagramManager error = new DatagramManager(ERROR, serverAddress, defaultManager.getPort(), 0, "Opcode does not match required number");
            DatagramPacket packetError = null;
            try{
               packetError = error.build();
               socket.send(packetError);
               return;
            } catch (SocketTimeoutException ste){
               log("Socket timed out");
            } catch (IOException ioe){
               log("Error creating packet");
            }
         }
         
         boolean done = false;
         int dataLeft = 0;
         int blockNum = defaultManager.getBlockNo();
         DatagramPacket pktRecieve = new DatagramPacket(maxByte, MAX_PACKET_SIZE);
         DatagramPacket pktSend;
         DatagramManager send;
         DatagramManager recieve;
         try
         {
            while(!done)
            {
                try
               {
                   //read in 512 bytes of the file
                   dataLeft = dis.read(maxByte);
               }
               catch(EOFException eofe)
               {
                    dataLeft = 0;
               }
                //increase block number
                blockNum++;
                
                //put the data in a DatagramPacket
                send = new DatagramManager(DATA, defaultManager.getAddress(), defaultManager.getPort(), blockNum, maxByte, dataLeft);
                pktSend = send.build();
                
                //SEND IT!!!
                log("Sending File [" + blockNum + "] " + name);
                try
                {
                    socket.send(pktSend);
                }
                catch(SocketTimeoutException ste)
                {
                    log("UPLOAD: Timed Out");
                    return;
                }
                
                //wait for ACK
                socket.receive(pktRecieve);
                
                //Check to see if it is an ACK
                recieve = new DatagramManager();
                recieve.dissect(pktRecieve);
                if(recieve.getOpcode() != ACK)
                {
                    doError(recieve, ILLOP, "Unexpected or Illegal Opcode Recieved");
                    return;
                }
                
                if(dataLeft < 512)
                {
                    done = true;
                }
                
            }
            
            log("Upload Finished");
         }
         catch(SocketTimeoutException ste)
        {
            log("Upload: Timed Out");
            return;
        }
        catch(FileNotFoundException fnfe)
        {
            doError(defaultManager, NOTFD, "File Not Found");
            log(fnfe + "");
            return;
        }
         catch(IOException ioe)
        {
            log("ExThrow CUT run: " + ioe);
            return;
        }
         
         
          // reading data from byte
         try{
            dis.read(maxByte);
         } catch (IOException ioe) {
            log("Can not read data from packet");
         } 
         
         // closing streams
         try{
            dis.close();
            fis.close();
         } catch (IOException ioe){
            log("Streams can not be closed");
         }
         
    }
   
   /**
     * Description: sends an error to the client based on params
     * @param packet first packet recieved by the server
     * @param num error number
     * @param message error message
     */
    private void doError(DatagramManager packet, int num, String message)
    {
        log("ERROR: " + errorCode[num]);
        try
        {
            DatagramManager error = new DatagramManager(ERROR, packet.getAddress(), packet.getPort(), num, message);
            DatagramPacket errorpkt = error.build();
            socket.send(errorpkt);
        }
        catch (IOException ex)
        {
            log("ExThrow CT doError: " + ex);
        }

    } //END doError
  }
  
  

} //END TFTPClient
