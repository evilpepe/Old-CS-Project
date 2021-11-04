
package TFTPAccessories;

//Import Statements
import java.io.*;
import java.net.*;

/**
 * @name DatagramManager
 * @description: The primary use for this file is to build and dissect files through the usage of TFTP CONSTANTS such as RRQ/WRQ, DATA, ACK, and ERROR (with several
 * error codes).
 * -Methods-
 * DatagramManager - Default Constructor
 *
 */
public class DatagramManager implements TFTPConstants
{
    /**
    * Java IO
    * @ByteArrayOutputStream 'byteOutput'
    * @DataOutputStream 'inputStream'
    */

    private ByteArrayOutputStream byteOutput = null;
    private DataOutputStream inputStream = null;

    /**
    * Java NET
    * @String fileName
    * @InetAddress address
    * @opcode 0
    * @DatagramPacket 'pkt' null
    */

    private String fileName;
    private InetAddress address;
    private int opcode = 0;
    private DatagramPacket pkt = null;

    /**
    * Datagram Manager Build vars
    * @int port
    * @String mode
    * @int blockNo
    * @int dataLen
    * @String errorMsg
    * @int errorNo
    */
    private int port;
    private String mode;
    private int blockNo;
    private byte data[];
    private int dataLen;
    private String errorMsg;
    private int errorNo;

    /**
    * Create Default Constructor
    * @DatagramManager
    */

    public DatagramManager()
    {

    } //END Default Constructor

    /**
    * DatagramManager (RRQ & WRQ)
    * @param opcode
    * @param address
    * @param port
    * @param fileName
    * @param mode
    */
    public DatagramManager(int opcode, InetAddress address, int port, String fileName, String mode) {
        this.opcode = opcode;
        this.address = address;
        this.port = port;
        this.fileName = fileName;
        this.mode = mode;
    }

    /**
    * DatagramManager (DATA)
    * @param opcode
    * @param address
    * @param port
    * @param blockNo
    * @param data
    * @param dataLen
    */
    public DatagramManager(int opcode, InetAddress address, int port, int blockNo, byte[] data, int dataLen) {
        this.opcode = opcode;
        this.address = address;
        this.port = port;
        this.blockNo = blockNo;
        this.data = data;
        this.dataLen = dataLen;
    }

    /**
    * DatagramManager (ACK)
    * @param opcode
    * @param address
    * @param port
    * @param blockNo
    */
    public DatagramManager(int opcode, InetAddress address, int port, int blockNo) {
        this.opcode = opcode;
        this.address = address;
        this.port = port;
        this.blockNo = blockNo;
    }

    /**
     * DatagramManager (ERROR)
     * @param opcode
     * @param address
     * @param port
     * @param errorNo
     * @param errorMsg
     */
    public DatagramManager(int opcode, InetAddress address, int port, int errorNo, String errorMsg) {
        this.opcode = opcode;
        this.address = address;
        this.port = port;
        this.errorNo = errorNo;
        this.errorMsg = errorMsg;

    }

    /**
     * readToZ
     * Description: Utility Method (Reads to Zero)
     * @param dis
     * @return
     * @throws Exception
     */
    public static String readToZ(DataInputStream dis) throws Exception {
        String value = "";

        while (true) {
            byte b = dis.readByte();

            if(b == 0)

                return value;
            value += (char) b;

        }
    } //END readToZ

    /**
     * DatagramPacket - BUILD METHOD
     * Description: build a packet from attributes
     * @return
     * @throws IOException
     */
    public DatagramPacket build() throws IOException {
        switch (opcode) {
            // RRQ Packet
            case 1:
                byteOutput = new ByteArrayOutputStream(2 + fileName.length() + 1 + "octet".length() + 1);
                inputStream = new DataOutputStream(byteOutput);

                inputStream.writeShort(opcode); // Write Short
                inputStream.writeBytes(fileName); // Write Bytes
                inputStream.writeByte(0); // Write Byte (0)
                inputStream.writeBytes("octet"); // Write Bytes
                inputStream.writeByte(0); // Write Byte (0)

                // closing outputStream and flush pkt
                try{
                    inputStream.close(); // Close Stream
                } catch (Exception e){ }

                byte[] holder = byteOutput.toByteArray(); // Get underlying byte

                pkt = new DatagramPacket(holder, holder.length, address, TFTP_PORT); // Create Packet
                break;

            // WRQ Packet
            case 2:
                byteOutput = new ByteArrayOutputStream(2 + fileName.length() + 1 + "octet".length() + 1);
                inputStream = new DataOutputStream(byteOutput);

                inputStream.writeShort(opcode); // Write Short
                inputStream.writeBytes(fileName); // Write Bytes
                inputStream.writeByte(0); // Write Byte (0)
                inputStream.writeBytes("octet"); // Write Bytes
                inputStream.writeByte(0); // Write Byte (0)

                // closing dataoutputstream and flushing packet
                try{
                    inputStream.close(); // Close Stream
                } catch (Exception e){ }

                holder = byteOutput.toByteArray(); // getting underlying byte

                pkt = new DatagramPacket(holder, holder.length, address, TFTP_PORT); // Create Packet

                break;

            // DATA Packet
            case 3:
                byteOutput = new ByteArrayOutputStream((4) + data.length);
                inputStream = new DataOutputStream(byteOutput);

                inputStream.writeShort(opcode); // Write Short
                inputStream.writeShort(blockNo); // Write Short

                if(dataLen > 0) {
                    inputStream.write(data, 0, dataLen);
                }
                inputStream.close(); // Close Stream

                pkt = new DatagramPacket(byteOutput.toByteArray(), 0, byteOutput.toByteArray().length, address, port); // Create Packet

                break;

            //ACK Packet
            case 4:
                byteOutput = new ByteArrayOutputStream(4);
                inputStream = new DataOutputStream(byteOutput);

                inputStream.writeShort(opcode); // Write Short
                inputStream.writeShort(blockNo); // Write Short

                inputStream.close(); // Close Stream

                byte[] ack = byteOutput.toByteArray();
                int ackLength = ack.length;

                pkt = new DatagramPacket(ack, ackLength, address, port); // Create Packet

                break;

            // ERROR Packet
            case 5:
                byteOutput = new ByteArrayOutputStream(4 + errorMsg.length() + 1);
                inputStream = new DataOutputStream(byteOutput);

                inputStream.writeShort(opcode); // Write Short
                inputStream.writeShort(errorNo); // Write Short
                inputStream.writeBytes(errorMsg); // Write Bytes
                inputStream.writeByte(0); // Write Byte (0)

                inputStream.close(); // Close Stream

                pkt = new DatagramPacket(byteOutput.toByteArray(), 0, byteOutput.toByteArray().length, address, port); // Create Packet

                break;

        } // END switch
        return pkt;
    } // END build

    /**
     * DatagramPacket - DISSECT METHOD
     * Description: Blocking until packets arrive by filling holder with the raw data.
     * @param pkt
     */
    public void dissect(DatagramPacket pkt)
    {
        ByteArrayInputStream byteInput = new ByteArrayInputStream(pkt.getData(), pkt.getOffset(), pkt.getLength());
        DataInputStream inputStream = new DataInputStream(byteInput);

        try {

            this.opcode = inputStream.readShort();
            this.address = pkt.getAddress();
            this.port = pkt.getPort();

            switch(opcode) {

                // RRQ
                case 1: {

                    if(opcode != RRQ) {
                        fileName = "";
                        mode = "";
                        try {
                            inputStream.close(); } catch(Exception exRRQ) {}
                        return;

                    }
                    fileName = readToZ(inputStream); // readToZ within inputStream
                    mode = readToZ(inputStream); // readToZ within inputStream
                    try {
                        inputStream.close(); // close inputStream
                    } catch(Exception exRTZ) {}

                    break;

                }

                // WRQ
                case 2: {

                    if(opcode != WRQ) {
                        fileName = "";
                        mode = "";
                        try {
                            inputStream.close(); } catch(Exception exWRQ) {}
                        return;

                    }
                    fileName = readToZ(inputStream); // readToZ within inputStream
                    mode = readToZ(inputStream); // readToZ within inputStream
                    try {
                        inputStream.close(); // close inputStream
                    } catch(Exception exRTZ) {}

                    break;

                }

                // DATA
                case 3: {

                    blockNo = inputStream.readShort();
                    dataLen = pkt.getLength() - 4;
                    data = new byte[pkt.getLength()];

                    inputStream.read(data, 0, pkt.getLength());

                    break;

                }

                // ACK
                case 4: {
                    blockNo = inputStream.readShort(); // Read Short

                    break;

                }

                // ERROR
                case 5: {
                    errorNo = inputStream.readShort(); // Read Short
                    errorMsg = readToZ(inputStream); // readToZ within inputStream

                    break;

                }

            } // END switch

        } catch(Exception ex) { System.out.println(ex); }

    } //END dissect

    /**
     * getFileName
     * Description: Gets FileName
     * @return
     */
    public String getFileName()
    {
        return fileName;
    } //END getFileName

    /**
     * setFileName
     * Description: Sets fileName
     * @param fileName
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    } //END setFileName

    /**
     * getAddress
     * Description: Gets Address
     * @return
     */
    public InetAddress getAddress()
    {
        return address;
    } //END getAddress

    /**
     * setAddress
     * Description:
     * @param address
     */
    public void setAddress(InetAddress address)
    {
        this.address = address;
    } //END setAddress

    /** getOpcode
     * Description: Gets Opcode
     * @return
     */
    public int getOpcode()
    {
        return opcode;
    } //END getOpcode

    /**
     * setOpcode
     * Description: Sets Opcode
     * @param opcode
     */
    public void setOpcode(int opcode)
    {
        this.opcode = opcode;
    } //END setOpcode

    /**
     * getPort
     * Description: Gets Port
     * @return
     */
    public int getPort()
    {
        return port;
    } //END getPort

    /**
     * setPort
     * Description: Sets Port
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    } //END setPort

    /**
     * getMode
     * Description: Gets Mode
     * @return
     */
    public String getMode()
    {
        return mode;
    } //END getMode

    /**
     * setMode
     * Description: Sets Mode
     * @param mode
     */
    public void setMode(String mode)
    {
        this.mode = mode;
    } //END setMode

    /**
     * getBlockNo
     * Description: Gets BlockNo
     * @return
     */
    public int getBlockNo()
    {
        return blockNo;
    } //END getBlockNo

    /**
     * setBlockNo
     * Description: Sets BlockNo
     * @param blockNo
     */
    public void setBlockNo(int blockNo)
    {
        this.blockNo = blockNo;
    } //END setBlockNo

    /**
     * getData
     * Description: Gets Data
     * @return
     */
    public byte[] getData()
    {
        return data;
    } //END getData

    /**
     * Description:
     * @param data
     */
    public void setData(byte[] data)
    {
        this.data = data;
    } //END setData

    /**
     * getDataLen
     * Description: Gets dataLen
     * @return
     */
    public int getDataLen()
    {
        return dataLen;
    } //END getDataLen

    /**
     * setDataLen
     * Description: Sets DataLen
     * @param dataLen
     */
    public void setDataLen(int dataLen)
    {
        this.dataLen = dataLen;
    } //END setDataLen

    /**
     * getErrorMsg
     * Description: Gets ErrorMsg
     * @return
     */
    public String getErrorMsg()
    {
        return errorMsg;
    } //END getErrorMsg

    /**
     * setErrorMsg
     * Description: Sets ErrorMsg
     * @param errorMsg
     */
    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    } //END setErrorMsg

    /**
     * getErrorNo
     * Description: Gets ErrorNo
     * @return
     */
    public int getErrorNo()
    {
        return errorNo;
    } //END getErrorNo

    /**
     * setErrorNo
     * Description: Sets ErrorNo
     * @param errorNo
     */
    public void setErrorNo(int errorNo)
    {
        this.errorNo = errorNo;
    } //END setErrorNo

} //END DatagramManager
