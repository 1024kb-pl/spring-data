package pl.blog.spring.springdata;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/init-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/clean-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookControllerTest implements WithAssertions {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldReturnAllBooks() {
        // when:
        RequestEntity<Void> request = RequestEntity.get("/book").accept(MediaType.APPLICATION_JSON).build();
        ResponseEntity<List<BookEntity>> response = testRestTemplate.exchange(request, new ParameterizedTypeReference<List<BookEntity>>() {});

        // then:
        List<BookEntity> books = response.getBody();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(books.size()).isEqualTo(3);
    }

    @Test
    void shouldReturnBookById() {
        // when:
        RequestEntity<Void> request = RequestEntity.get("/book/3").build();
        ResponseEntity<BookEntity> response = testRestTemplate.exchange(request, BookEntity.class);

        // then:
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getAuthor()).isEqualTo("Normal author");
    }

    @Test
    void shouldThrow404WhenBookDoesntExist() {
        // when:
        RequestEntity<Void> request = RequestEntity.get("/book/412").build();
        ResponseEntity<BookEntity> response = testRestTemplate.exchange(request, BookEntity.class);

        // then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldCreateBookSuccessfully() {
        // when;
        RequestEntity<CreateBookModel> request = RequestEntity
                .post("/book")
                .body(new CreateBookModel("New book", "New author"));

        ResponseEntity<BookEntity> response = testRestTemplate.exchange(request, BookEntity.class);

        // then:
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getId()).isNotEqualTo(null);
    }

    @Test
    void shouldDeleteExistingBook() {
        // when;
        RequestEntity<Void> request = RequestEntity
                .delete("/book/1")
                .build();

        ResponseEntity<Void> response = testRestTemplate.exchange(request, Void.class);

        // then:
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void shouldThrow404WhenBookToDeleteDoesntExist() {
        // when;
        RequestEntity<Void> request = RequestEntity
                .delete("/book/144")
                .build();

        ResponseEntity<Void> response = testRestTemplate.exchange(request, Void.class);

        // then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnProperlyNumberOfExistingBooks() {
        // when:
        RequestEntity<Void> request = RequestEntity.get("/book/count").build();
        ResponseEntity<Long> response = testRestTemplate.exchange(request, Long.class);

        // then:
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(3);
    }
}
