
package TFTPServer;

//Import Statements
import TFTPAccessories.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @name ServerThread
 * @description: controls all new connections coming into the server
 * 
 * -Methods-
 * Default Constructor
 * run: waits for new connections to the server
 * stopServer: stops the server from accepting new connections
 * log: appends a line of text to taLog
 */
public class ServerThread extends Thread implements TFTPConstants
{
    //Class Constants

    //Class Variables
    private DatagramSocket sSocket;

    //********************

    public ServerThread()
    {
        sSocket = null;
    } //END Constructor
    
    /**
     * Description: waits for new connections to the server
     */
    @Override
    public void run()
    {
        log("Server Started");

        try
        {
            sSocket = new DatagramSocket(TFTP_PORT);
        }
        catch(IOException ioe)
        {
            log("ExThrow ST run: " + ioe);
            return;
        }

        //loop for infinity while searching for connections
        while(true)
        {
            byte[] holder = new byte[MAX_PACKET_SIZE];
            DatagramPacket pkt = new DatagramPacket(holder, MAX_PACKET_SIZE);
            try
            {
                // Wait for a connection
                sSocket.receive(pkt);
            }
            catch(IOException ioe)
            {
                log("Server Stopped - Server Socket Closed");
                return;
            }

            ClientThread ct = new ClientThread(pkt);
            ct.start();

       } //END WHILE

    } //END run

    /**
     * Description: stops the server from accepting new connections
     */
    public void stopServer()
    {
        try
        {
            sSocket.close();
        }
        catch(Exception e)
        {
            log("ExThrow ST stopServer: " + e);
            return;
        }

    } //END stopServer
    
    /**
     * Description: appends a line of text to taLog
     * @param message the message to appended to the screen
     */
    private void log(String message)
    {
        TFTPServer.log(message);
    } //END log
    
} //END ServerThread