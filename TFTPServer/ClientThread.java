

package TFTPServer;

//Import Statements
import TFTPAccessories.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @name ClientThread
 * @description: controls all front-end client/server communication
 * 
 * -Methods-
 * Constructor
 * run: takes in the first packet and decides what to do by opcode
 * doRead: sends a file from the server to the client
 * doWrite: recieves a file from the client
 * doError: sends an error to the client based on params
 * doShutdown: Closes all open streams and shuts down the connection
 * log: appends a line of text to taLog
 */
public class ClientThread extends Thread implements TFTPConstants
{
    //Class Constants
    
    //Class Variables
    private DatagramSocket cSocket = null;
    private DatagramPacket firstPkt = null;
    private DatagramManager manager;

    public ClientThread(DatagramPacket pkt1)
    {
        firstPkt = pkt1;
    } //END Constructor
   
    /**
     * Description: takes in the first packet and decides what to do by opcode
     */
    public void run()
    {
        int opcode;
        manager = new DatagramManager();
        
        //scoket declaration only works here and not constructor for some reason
        try
        {
            cSocket = new DatagramSocket();
            cSocket.setSoTimeout(7500);
        }
        catch (SocketException ex)
        {
            log("ExThrow CT Constr: " + ex);
        }
        
        //GET OPCODE
        manager.dissect(firstPkt);
        opcode = manager.getOpcode();
        
        log("Packet Received");
        TFTPServer.setConnections(((Integer.parseInt(TFTPServer.getConnections())) + 1));
        
        //run the protocol, using firstPkt as the first packet in the conversation
        if(opcode == RRQ)
        {
            doRead(manager);
        }
        else if(opcode == WRQ)
        {
            doWrite(manager);
        }
        else //Send Error
        {
            doError(manager, ILLOP, "Unexpected or Illegal Opcode Recieved");
        }
        
        doShutdown();

    } //END run
    
    /**
     * Description: sends a file from the server to the client
     * @param packet first packet recieved by the server
     */
    private void doRead(DatagramManager packet)
    {
        DataInputStream dis = null;
        byte[] blockSize = new byte[512];
        int dataLeft = 0;
        boolean done = false;
        DatagramPacket pktRecieve = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
        DatagramPacket pktSend;
        DatagramManager send;
        DatagramManager recieve;
        int blockNum = 0;
        
        log("Downlaod Started");
        
        try
        {
            log("Opening File: " + packet.getFileName());
            //open FIS to convert file to bytes
            dis = new DataInputStream(new FileInputStream(new File(TFTPServer.getDir() + File.separator + packet.getFileName())));
            
            while(!done)
            {
                try
                {
                    //read in 512 bytes of the file
                    dataLeft = dis.read(blockSize);
                }
                catch(EOFException eofe)
                {
                     dataLeft = 0;
                }
                
                //increase block number
                blockNum++;
                
                //put the data in a DatagramPacket
                send = new DatagramManager(DATA, packet.getAddress(), packet.getPort(), blockNum, blockSize, dataLeft);
                pktSend = send.build();
                
                //SEND IT!!!
                log("Sending File [" + blockNum + "] " + packet.getFileName());
                try
                {
                    cSocket.send(pktSend);
                }
                catch(SocketTimeoutException ste)
                {
                    log("DOWNLOAD: Timed Out");
                    return;
                }
                
                //wait for ACK
                cSocket.receive(pktRecieve);
                
                //Check to see if it is an ACK
                recieve = new DatagramManager();
                recieve.dissect(pktRecieve);
                if(recieve.getOpcode() != ACK)
                {
                    doError(recieve, ILLOP, "Unexpected or Illegal Opcode Recieved");
                    return;
                }
                
                if(dataLeft != 512)
                {
                    done = true;
                }
                
            } //END WHILE
            
            log("Downlaod Finished");
            dis.close();
        }
        catch(FileNotFoundException fnfe)
        {
            doError(manager, NOTFD, "File Not Found");
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
            doError(manager, UNDEF, "File Not Found: " + ex);
            return;
        }
        
    } //END doRead
    
    /**
     * Description: recieves a file from the client
     * @param packet first packet recieved by the server
     */
    private void doWrite(DatagramManager packet)
    {
        DataOutputStream dos;
        DatagramPacket pktRecieve = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
        DatagramPacket pktSend;
        DatagramManager send;
        DatagramManager recieve;
        boolean done = false;
        int blockNum = 0;
        
        log("Upload Started");
        
        try
        {
            log("Opening File: " + packet.getFileName());
            //open FIS to convert file to bytes
            dos = new DataOutputStream(new FileOutputStream(new File(TFTPServer.getDir() + File.separator + packet.getFileName())));
            
            while(!done)
            {
                //Send ACK to client
                send = new DatagramManager(ACK, packet.getAddress(), packet.getPort(), blockNum);
                pktSend = send.build();
                cSocket.send(pktSend);
                
                //wait for DATA
                try
                {
                    cSocket.receive(pktRecieve);
                }
                catch(SocketTimeoutException ste)
                {
                    log("Upload timed out waiting for DATA!");
                    return;
                }
                
                log("Packet Recieved");
                
                //Break down recieved packet
                recieve = new DatagramManager();
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
                
                //check if that was last packet
                if(recieve.getDataLen() < 512)
                {
                    done = true;
                }
                
            } //END WHILE
            
            //Send Final ACK to client
            send = new DatagramManager(ACK, packet.getAddress(), packet.getPort(), blockNum);
            pktSend = send.build();
            cSocket.send(pktSend);
            
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
            doError(manager, NOTFD, "File Not Found");
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
            doError(manager, UNDEF, "File Not Found: " + ex);
            return;
        }
        
    } //END doWrite

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
            cSocket.send(errorpkt);
        }
        catch (IOException ex)
        {
            log("ExThrow CT doError: " + ex);
        }
        doShutdown();

    } //END doError
    
    /**
     * Description: Closes all open streams and shuts down the connection
     */
    private void doShutdown()
    {
        log("Client Disconnected");
        cSocket.close();
        TFTPServer.setConnections(((Integer.parseInt(TFTPServer.getConnections())) - 1));
    } //END doShutdown
    
    /**
     * Description: appends a line of text to taLog
     * @param message the message to appended to the screen
     */
    private void log(String message)
    {
        TFTPServer.log(message);
    } //END log
    
} //END ClientThread