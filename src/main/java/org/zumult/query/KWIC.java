/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

/**
 *
 * @author Elena Frick
 */
public interface KWIC<T> extends SearchResultPlus {
    public KWICContext getLeftContext();
    public KWICContext getRightContext();
    public T getKWICSnippets();
    public String getType();
    public String toXML();
}
