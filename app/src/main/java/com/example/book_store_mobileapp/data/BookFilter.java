package com.example.book_store_mobileapp.data;

import java.util.ArrayList;
import java.util.List;

public class BookFilter {
    public List<Book> searchBooks(List<Book> bookList, String query){
        // Return full book list if query is null or empty
        if(query == null || query.trim().isEmpty()){
            return new ArrayList<>(bookList);
        }
        //
        List<Book> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();
        for(Book book : bookList){
            if(book.getName().toLowerCase().contains(lowerCaseQuery)){
                filteredList.add(book);
            }
        }
        return filteredList;
    }

    public List<Book> filterBooksByPriceRange(List<Book> bookList, double min, double max){
        List<Book> filteredList = new ArrayList<>();
        for(Book book: bookList){
            if(book.getPrice() >= min && book.getPrice() <= max){
                filteredList.add(book);
            }
        }
        return filteredList;
    }

    public void sortBooksByPrice(List<Book> bookList, int sortType){
        switch (sortType){
            case 1: //Ascending price
                bookList.sort((book1, book2) -> Double.compare(book1.getPrice(), book2.getPrice()));
                break;
            case 2: //Descending price
                bookList.sort((book1, book2) -> Double.compare(book2.getPrice(), book1.getPrice()));
                break;
            default:
                bookList.sort((book1, book2) -> book1.getBookId().compareTo(book2.getBookId()));
                break;
        }
    }
}
