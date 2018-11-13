/******************************************************************
 *
 * This code is for the Pappking service project.
 *
 *
 * © 2018, Pappking Management All rights reserved.
 *
 *
 ******************************************************************/
package co.ppk.job;

import co.ppk.job.services.JobManager;
import co.ppk.job.services.impl.JobManagerImpl;
import co.ppk.job.utilities.PropertyManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

/***
 * Configuration class for Spring IOC
 *
 * @author jmunoz
 * 
 * @version 1.0
 */

@SpringBootApplication
public class Main {
    /**
     * @param args application arguments
     */
    public static void main(String[] args) throws InterruptedException {


        Properties prop = new PropertyManager().getInstance();

        JobManager jm = new JobManagerImpl(prop);

        final long timeInterval = Integer.valueOf(prop.getProperty("PAYMENTS.CHECK.INTERVAL.MINUTES")) * 60000;

        while(true){
            System.out.println("#################################### CHECKING PENDING PAYMENTS ####################################");
            jm.checkPendingPayments();
            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        //for demo only.
//        for (int i = 0; i <= 5; i++) {
//            System.out.println("Execution in Main Thread...." + i);
//            Thread.sleep(2000);
//            if (i == 5) {
//                System.out.println("Application Terminates");
//                System.exit(0);
//            }
//        }

    }

}
