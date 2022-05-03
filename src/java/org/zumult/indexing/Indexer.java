/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.IOException;

/**
 *
 * @author Thomas.Schmidt
 */
public interface Indexer {
    
    public void index() throws IOException;
    
}
