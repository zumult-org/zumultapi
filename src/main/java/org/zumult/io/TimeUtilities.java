/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Elena
 */
public class TimeUtilities {
    
    public static String format(long millis){
        return String.format("%02d min, %02d sec, %02d msec", TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
            TimeUnit.MILLISECONDS.toMillis(millis)- TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)));
    }
    
    public static String formatDigital(long sec){
        return String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(sec),
            TimeUnit.SECONDS.toMinutes(sec) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(sec)),
            TimeUnit.SECONDS.toSeconds(sec)- TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(sec)));
    }
        
}
