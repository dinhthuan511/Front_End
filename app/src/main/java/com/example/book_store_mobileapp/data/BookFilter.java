package com.example.book_store_mobileapp.data;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BookFilter {
    private String removeAccents(String text) {
        if (text == null) return "";
        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public List<Book> searchBooks(List<Book> bookList, String query){
        // Return full book list if query is null or empty
        if(query == null || query.trim().isEmpty()){
            return new ArrayList<>(bookList);
        }
        //
        List<Book> filteredList = new ArrayList<>();
        String normalizedQuery = removeAccents(query.toLowerCase());
        for(Book book : bookList){
            String normalizedBookName = removeAccents(book.getName().toLowerCase());
            if(normalizedBookName.contains(normalizedQuery)){
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

    // Thêm phương thức này vào file BookFilter.java
    public List<Book> filterBooksByCategories(List<Book> books, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
        return books; // Nếu không có id nào được chọn, trả về danh sách gốc
    }
        List<Book> filteredBooks = new ArrayList<>();
        for (Book book : books) {
            // Kiểm tra xem categoryId của sách có nằm trong danh sách các id được chọn không
            if (book.getCategoryId() != null && categoryIds.contains(book.getCategoryId())) {
                filteredBooks.add(book);
            }
        }
        return filteredBooks;
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
