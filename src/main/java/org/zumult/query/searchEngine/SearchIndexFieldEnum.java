/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import org.zumult.io.Constants;

/**
 *
 * @author Frick
 */
public enum SearchIndexFieldEnum {
    TRANSCRIPT_KENNUNG,
    TRANSCRIPT_CONTENT,
    TRANSCRIPT_TOKEN_TOTAL; // number of word tokens in transcript

    @Override
    public String toString() {
        return switch (this) {
            case TRANSCRIPT_KENNUNG -> Constants.METADATA_KEY_TRANSCRIPT_DGD_ID;
            case TRANSCRIPT_CONTENT -> "content";
            case TRANSCRIPT_TOKEN_TOTAL -> "tokenTotal";
            default -> null;
        };
    }
}
