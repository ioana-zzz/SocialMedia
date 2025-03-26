package org.example.paging;

import java.util.List;

public class Page<E> {

    private List<E> elementsOnPage;
    private int totalNumberElements;

    public Page(List<E> elementsOnPage, int totalNumberElements) {
        this.elementsOnPage = elementsOnPage;
        this.totalNumberElements = totalNumberElements;
    }



    public int getTotalNumberElements() {
        return totalNumberElements;
    }

    public List<E> getElementsOnPage() {
        return elementsOnPage;
    }

}
