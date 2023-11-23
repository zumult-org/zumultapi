/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.zuHand;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.MediaUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.Transcript;

/**
 *
 * @author Elena
 * 
 * This script creates images from videos. 
 * It was used just once for virtual collections in ZuHand
 * 
 */
public class VideoImageCreater {
    
    BackendInterface backendInterface;
    public static File DONWLOAD_DIRECTORY = new File(new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getPath()).
            getParentFile().getParentFile(), "downloads");  // \ids-sample\build\web\downloads

    public static final String[][] TEST_INPUT = 
    {
        {"FOLK_E_00126_SE_01_T_01", "c122"},
        {"FOLK_E_00126_SE_01_T_01", "c587"},
        {"FOLK_E_00126_SE_01_T_01", "c757"}
    }; 

    public static void main(String[] args) {
        try {
            new VideoImageCreater().createVideoImages(TEST_INPUT);
        } catch (IOException ex) {
            Logger.getLogger(VideoImageCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createVideoImages(String[][] input) throws IOException{
        System.out.println("DONWLOAD_DIRECTORY: " + DONWLOAD_DIRECTORY);
        for (int i = 0; i<input.length; i++){
                createVideoImage(input[i][0], input[i][1]);        
        }
    }
    
    private void createVideoImage(String transcriptID, String annotationBlockID) throws IOException{
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            IDList videos = backendInterface.getVideos4Transcript(transcriptID);
            if(videos.size()>0){
                
                String mediaID = videos.get(0);  
                Transcript transcript = backendInterface.getTranscript(transcriptID);
                Media video = backendInterface.getMedia(mediaID);
                
                double time = transcript.getTimeForID(annotationBlockID);
                String fileName = video.getID() + "_" + time + ".png";
                File outputFile = new File(DONWLOAD_DIRECTORY + "\\"+ fileName);
                new MediaUtilities(Configuration.getFfmpegPath()).getVideoImage(time, video.getURL(), outputFile.getAbsolutePath());
            }
            
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(VideoImageCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
