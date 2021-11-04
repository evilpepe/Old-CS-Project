
package TFTPAccessories;

/**
 * @name TFTPConstants
 * @description: stores constants needed for the TFTP project
 */
public interface TFTPConstants
{
    /**
    * RRQ - Read Request
    * WRQ - Write Request
    * DATA - Data 
    * ACK - Acknowledgement
    * ERROR - Error 
    */
    
    public static final int RRQ = 1;
    public static final int WRQ = 2;
    public static final int DATA = 3;
    public static final int ACK = 4;
    public static final int ERROR = 5;
    
    /**
    * ERROR CODES 
    */
    
    public static final int UNDEF = 0;
    public static final int NOTFD = 1;
    public static final int ACCESS = 2;
    public static final int DSKFUL = 3;
    public static final int ILLOP = 4;
    public static final int UNKID = 5;
    public static final int FILEX = 6;
    public static final int NOUSER = 7;
    
    /** 
    * TFTP_PORT SET TO 69 (DEFAULT)
    * MAX_PACKET_SIZE SET TO 1500 (MAXIMUM)
    */
    
    public static final int TFTP_PORT = 69;
    public static final int MAX_PACKET_SIZE = 1500;
    
    /**
    * errorCode contains all defined ecodes within an ArrayList 
    * opCode contains all defined opcodes within ArrayList (null for [0] due to start at 1)
    */
    
    public static final String[] errorCode = new String[]{"UNDEF", "NOTFND", "ACCESS", "DISKFULL", "ILLOP", "UNKID", "FILEEX", "NOUSER"};
    public static final String[] opCode = new String[]{null, "RRQ", "WRQ", "DATA", "ACK", "ERROR"};
} //END TFTPConstants
