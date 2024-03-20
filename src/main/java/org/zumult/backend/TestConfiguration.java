/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend;

import org.zumult.backend.implementations.AGDFileSystem;
import org.zumult.backend.implementations.AbstractIDSBackend;

/**
 *
 * @author Thomas_Schmidt
 */
public class TestConfiguration {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AbstractIDSBackend bi = new AGDFileSystem();
        
        System.out.println(Configuration.getBackendInterfaceClassPath());
        System.out.println(Configuration.getMediaPath());
        System.out.println(Configuration.getRestAPIBaseURL());
        System.out.println(Configuration.getCorpusIDs());
    }
    
}
