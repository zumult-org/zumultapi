/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.zumult.objects.Media;

/**
 *
 * @author Thomas.Schmidt
 */
public class MediaUtilities {
    
    String ffmpegPath;
    String ffprobePath;
    
    public MediaUtilities(String ffmpegPath){
        this.ffmpegPath = ffmpegPath;
    }
    
    public MediaUtilities(String ffmpegPath, String ffProbePath){
        this.ffmpegPath = ffmpegPath;
        this.ffprobePath = ffProbePath;
    }
    

    public void getVideoImage(double time, String pathToInputFile, 
                              String pathToOutputFile) throws IOException{

        //File sourceFile = new File(pathToInputFile);
        File targetFile = new File(pathToOutputFile);
        
        // ffmpeg.exe" -i N:\ARCHIV\FOLK\FOLK_E_00069\FOLK_E_00069_SE_01_V_01_DF_01.mp4 
        // -vf "select=eq(n\,1500)" -vframes 1 C:\Users\thomas.schmidt\Desktop\out.png

        //long frame = Math.round(time * 25);
        

        String[] commandAndParameters = {
            ffmpegPath,
            "-y",
            "-ss",
            Double.toString(time),
            "-i",
            pathToInputFile, //sourceFile.getAbsolutePath(),
            "-vframes",
            "1",
            targetFile.getAbsolutePath()
        };    // Hier hat Schlotti mit einem Semikolon ausgeholfen.
        
        ProcessBuilder ffmpegProcess = new ProcessBuilder(commandAndParameters);
        ffmpegProcess.inheritIO();
        //System.out.println("FFMPEG: " + ffmpegProcess.toString());
        Process process = ffmpegProcess.start();
        //ffmpegProcess.redirectOutput(new File(pathToOutputFile.replaceAll("\\.[Mm][Pp]4", "_Output.txt")));
        try {
            int exitCode = process.waitFor();            
            //return outputFile;
        } catch (InterruptedException ex) {
            Logger.getLogger(MediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
    public void cutVideo(double startTime, 
                                double endTime, 
                                String pathToInputFile, 
                                String pathToOutputFile) throws IOException {
        //String FFMPEGPATH = "ffmpeg";
        //String FFMPEGPATH = "C:\\Program Files\\ffmpeg-20191101-53c21c2-win64-static\\bin\\ffmpeg.exe";

        //File sourceFile = new File(pathToInputFile);
        File targetFile = new File(pathToOutputFile);
        
        String[] commandAndParameters = {
            ffmpegPath,
            "-y",
            "-i",
            pathToInputFile, //sourceFile.getAbsolutePath(),
            "-ss",
            Double.toString(startTime),
            "-c",
            "copy",
            "-t",
            Double.toString(endTime - startTime),
            targetFile.getAbsolutePath()
        };
        ProcessBuilder ffmpegProcess = new ProcessBuilder(commandAndParameters);
        ffmpegProcess.inheritIO();
        //System.out.println("FFMPEG: " + ffmpegProcess.toString());
        Process process = ffmpegProcess.start();
        //ffmpegProcess.redirectOutput(new File(pathToOutputFile.replaceAll("\\.[Mm][Pp]4", "_Output.txt")));
        try {
            process.waitFor();            
            //return outputFile;
        } catch (InterruptedException ex) {
            Logger.getLogger(MediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }


    public void cutAudio(double startTime, double endTime, String pathToInputFile, String pathToOutputFile) throws IOException {            
        AudioInputStream audioInputStream;
        System.out.println("Cutting " + pathToInputFile);
        
        // 2025-04-08 changed for #246
        if (!(COMAUtilities.isHttpLink(pathToInputFile))){
            try {
                File soundFile = new File(pathToInputFile);
                audioInputStream = AudioSystem.getAudioInputStream(soundFile);    
            } catch (UnsupportedAudioFileException ex){
                Logger.getLogger(MediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
                IOException wrappedException = new IOException("Unsupported audio file:" + ex.getLocalizedMessage());
                throw wrappedException;
            }
        } else {
            try {
                URL url = new URL(pathToInputFile);
                audioInputStream = AudioSystem.getAudioInputStream(url);
            } catch (UnsupportedAudioFileException ex) {
                Logger.getLogger(MediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
                IOException wrappedException = new IOException("Unsupported audio file:" + ex.getLocalizedMessage());
                throw wrappedException;
            }
        }
            
        AudioFormat audioFormat = audioInputStream.getFormat();        

        if (audioFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED){
            IOException wrappedException = new IOException("Audio format does not support this operation.");
            throw wrappedException;
        }

        long start = (int)Math.round(startTime * audioFormat.getFrameRate()); // in frames
        long length = (int)Math.round((endTime - startTime) * audioFormat.getFrameRate());; // in frames
        // changed 30-11-2011 for audio cutting servlet
        long streamLength = audioInputStream.getFrameLength();
        if ((start + length) > streamLength){
            length = streamLength - start;
        }
        if ((start<0) || ((start+length)> audioInputStream.getFrameLength())){
            String message = "At least one time value is illegal for this audio file: " + Double.toString(startTime) + "/" + Double.toString(endTime);
            IOException wrappedException = new IOException(message);
            throw wrappedException;                
        }

        int frameSize = audioFormat.getFrameSize();
        audioInputStream.skip(start * frameSize);
        AudioInputStream derivedAIS = new AudioInputStream(audioInputStream,audioFormat, length);        
        File outputFile = new File(pathToOutputFile);
        AudioSystem.write(derivedAIS, AudioFileFormat.Type.WAVE, outputFile);

        derivedAIS.close();
        audioInputStream.close();
    }

    public double getMediaDuration(String url) {
        if (url.toUpperCase().endsWith(".WAV")){
            AudioInputStream audioInputStream = null;
            try {
                // 2025-04-08 changed for #246
                if (!(COMAUtilities.isHttpLink(url))){
                    File file = new File(url);
                    audioInputStream = AudioSystem.getAudioInputStream(file);
                    AudioFormat format = audioInputStream.getFormat();
                    long audioFileLength = file.length();
                    int frameSize = format.getFrameSize();
                    float frameRate = format.getFrameRate();            
                    float durationInSeconds = (audioFileLength / (frameSize * frameRate));
                    return (durationInSeconds);
                } else {
                    URL theUrl = new URL(url);
                    audioInputStream = AudioSystem.getAudioInputStream(theUrl);
                    AudioFormat format = audioInputStream.getFormat();
                    long frames = audioInputStream.getFrameLength();
                    double durationInSeconds = (frames+0.0) / format.getFrameRate();                    
                    return (durationInSeconds);
                }
            } catch (UnsupportedAudioFileException | IOException ex) {
                Logger.getLogger(MediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    audioInputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(MediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        // here's a to do: get media duration for other file types via FFMPEG
        return -1;
    }
    
    public static Media.MEDIA_FORMAT getFormatForExtension(String extension){
        String ext = extension.toUpperCase();
        switch (ext){
            case "WAV" : return Media.MEDIA_FORMAT.WAV;
            case "MP3" : return Media.MEDIA_FORMAT.MP3;
            case "MP4" : return Media.MEDIA_FORMAT.MPEG4_WEB; 
        }
        return null;
    }
    
    
}
