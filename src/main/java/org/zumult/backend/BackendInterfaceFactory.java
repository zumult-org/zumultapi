/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend;

/**
 *
 * @author Thomas_Schmidt
 */
public class BackendInterfaceFactory {
     
    static {
        // determine configuration, for instance from some configuration file
        // depending on the configuration, we will know what implementation
        // of BackendInterface we are meant to return
    }
       
    public static BackendInterface newBackendInterface() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        System.out.println("Backend: " + Configuration.getBackendInterfaceClassPath());
        return (BackendInterface) Class.forName(Configuration.getBackendInterfaceClassPath()).newInstance();
    }
    
    public static BackendInterface newBackendInterface(String backendInterfaceClassPath) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        System.out.println("Backend: " + backendInterfaceClassPath);
        return (BackendInterface) Class.forName(backendInterfaceClassPath).newInstance();
    }
    
}
