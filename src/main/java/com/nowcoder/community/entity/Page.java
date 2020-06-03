package com.nowcoder.community.entity;

public class Page {

    //Current age number
    private int currentPage = 1;

    //Limit number of rows in one page
    private int limitInOnePage = 10;

    //Rows in total
    private int rowsTotal;

    //Query path
    private String path;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {

        if (currentPage >= 1) {
            this.currentPage = currentPage;
        }

    }

    public int getLimitInOnePage() {
        return limitInOnePage;
    }

    public void setLimitInOnePage(int limitInOnePage) {

        if (limitInOnePage >= 1 && limitInOnePage <= 100) {
            this.limitInOnePage = limitInOnePage;
        }

    }

    public int getRowsTotal() {
        return rowsTotal;
    }

    public void setRowsTotal(int rowsTotal) {

        if (rowsTotal >= 0) {
            this.rowsTotal = rowsTotal;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset() {
        return (currentPage - 1) * limitInOnePage;
    }

    public int getTotalPageNumber() {

        if (rowsTotal % limitInOnePage == 0) {
            return rowsTotal / limitInOnePage;
        } else {
            return rowsTotal / limitInOnePage + 1;
        }

    }

    public int getFromPageNumber() {
        return Integer.max(currentPage - 2, 1);
    }

    public int getToPageNumber() {
        return Integer.min(currentPage + 2, getTotalPageNumber());
    }

}
