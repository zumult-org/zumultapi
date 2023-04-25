/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.zumult.query.Pagination;

/**
 *
 * @author Elena
 */
public class DGD2Pagination implements Pagination {
    private int pageStartIndex;
    private int itemsPerPage;

    @Override
    public int getPageStartIndex() {
        return pageStartIndex;
    }

    public void setPageStartIndex(int pageStartIndex) {
        this.pageStartIndex = pageStartIndex;
    }

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
}
