/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author josip.batinic
 */
public class MediaStreamer implements StreamingOutput {

    private int length;
    private RandomAccessFile raf;
    final byte[] buf = new byte[4096];

    public MediaStreamer(int length, RandomAccessFile raf) {
        this.length = length;
        this.raf = raf;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        try {
            while( length != 0) {
                int read = raf.read(buf, 0, buf.length > length ? length : buf.length);
                outputStream.write(buf, 0, read);
                length -= read;
            }
        } finally {
            raf.close();
        }
    }

    public int getLenth() {
        return length;
    }

//    @Override
//    public void write(OutputStream out) throws IOException, WebApplicationException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
}
