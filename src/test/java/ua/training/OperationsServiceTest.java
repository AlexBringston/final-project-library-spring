package ua.training;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import ua.training.model.Abonnement;
import ua.training.model.Role;
import ua.training.model.User;
import ua.training.model.dto.input.BookAbonnementDTO;
import ua.training.services.BookService;
import ua.training.services.OperationsService;
import ua.training.services.OrderService;
import ua.training.services.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OperationsServiceTest {

    @MockBean
    private BookService bookService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    @Autowired
    private OperationsService operationsService;


    @Test
    public void testFindUserAbonnement() {
        User user = getLoggedUser();
        Page<Abonnement> abonnementPage = new PageImpl<>(new ArrayList<>(Arrays.asList(new Abonnement() ,
                new Abonnement())));
        doReturn(user).when(userService).getCurrentUser();
        doReturn(abonnementPage).when(orderService).findCurrentReaderAbonnement(any(User.class),
                any(BookAbonnementDTO.class));
        Page<Abonnement> returnedPage = operationsService.findCurrentUserAbonnement(new BookAbonnementDTO());
        verify(orderService, times(1)).findCurrentReaderAbonnement(any(),any());
        assertEquals(abonnementPage,returnedPage, "Pages are not the same");
    }


    private User getLoggedUser() {
        return User.builder()
                .id(1L)
                .name("John")
                .surname("Hopkins")
                .username("login")
                .role(new Role(3L, "ROLE_READER"))
                .password("$2a$10$.JmitmzYt./HkfzZaYgm3ec2VbZrIyyXQiQOKddYyc3JXvCExakam")
                .isAccountNonLocked(true)
                .birthDate(LocalDate.of(1990, 12, 12))
                .build();
    }
}
