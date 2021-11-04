
package TFTPAccessories;

//Import Statements
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * @name ClockTask
 * @description: Intitially meant to be stored within the Server, moved to TFTPAccessories as it only keeps track of 
 * user's local system time and displays it within the FX GUI. 
 * -Methods-
 * run: 
 * update: 
 */
public class ClockTask extends java.util.TimerTask
{

    //Class Variables
    private Label lblTime;
    private GregorianCalendar cal;
    private SimpleDateFormat dateFormat;

    //********************

    public ClockTask(Label lblTime)
    {
        this.lblTime = lblTime;
        cal = new GregorianCalendar();
        dateFormat = new SimpleDateFormat("hh:mm:ss");
    } //END Constructor
    
    /**
     * Description: 
     */
    @Override
    public void run()
    {
        cal.setTimeInMillis(System.currentTimeMillis());
        update(dateFormat.format(cal.getTime()));
        
    } //END run
    
    /**
     * Description: updates the time to the label
     * @param message the new time as a string
     */
    private void update(String message)
    {
        Platform.runLater(new Runnable(){
            public void run()
            {
                lblTime.setText(message);
            }
        });
    } //END log
    
} //END ClockTask
