package ua.training.utils;

import ua.training.model.Author;
import ua.training.model.Book;

import java.util.Comparator;

public class CustomComparators {

    public static Comparator<Book> authorComparator() {

        return (o1, o2) -> {
            Author author1 = o1.getAuthors()
                    .stream().sorted()
                    .findFirst()
                    .orElse(new Author());

            Author author2 = o2.getAuthors()
                    .stream().sorted()
                    .findFirst()
                    .orElse(new Author());

            return author1.getSurname().compareTo(author2.getName());
        };
    }
}
