package com.lenon.users.service;

import com.lenon.users.dto.UserDTO;
import com.lenon.users.dto.UserRequest;
import com.lenon.users.exception.ConflictException;
import com.lenon.users.exception.DomainValidationException;
import com.lenon.users.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserServiceTest {

    private InMemoryUserService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryUserService();
    }

    @Test
    void create_shouldReturnUserWithIdAndTrimmedFields() {
        UserRequest req = new UserRequest("  Lenon  ", "  lenon@example.com  ", "  admin ");
        UserDTO saved = service.create(req);

        assertNotNull(saved.id());
        assertEquals(1L, saved.id());
        assertEquals("Lenon", saved.name());
        assertEquals("lenon@example.com", saved.email());
        assertEquals("ADMIN", saved.role());
    }

    @Test
    void create_shouldFailWhenEmailAlreadyExists_ignoreCase() {
        service.create(new UserRequest("A", "mail@ex.com", "ADMIN"));
        assertThrows(ConflictException.class,
                () -> service.create(new UserRequest("B", "MAIL@EX.COM", "USER")));
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void update_shouldReplaceAllFields_andKeepId() {
        UserDTO saved = service.create(new UserRequest("X", "x@ex.com", "USER"));

        UserRequest update = new UserRequest("  New Name ", " new@ex.com ", " manager ");
        UserDTO updated = service.update(saved.id(), update);

        assertEquals(saved.id(), updated.id());
        assertEquals("New Name", updated.name());
        assertEquals("new@ex.com", updated.email());
        assertEquals("MANAGER", updated.role());
        // garante que o findById reflete os dados atualizados
        UserDTO byId = service.findById(saved.id());
        assertEquals(updated, byId);
    }

    @Test
    void update_shouldFailWhenEmailBelongsToAnotherUser() {
        UserDTO u1 = service.create(new UserRequest("A", "a@ex.com", "ADMIN"));
        UserDTO u2 = service.create(new UserRequest("B", "b@ex.com", "USER"));

        // tentar mudar o email do u2 para o email do u1 -> 409
        assertThrows(ConflictException.class,
                () -> service.update(u2.id(), new UserRequest("B", "A@EX.COM", "USER")));
    }

    @Test
    void create_shouldFailWhenRoleInvalid() {
        assertThrows(DomainValidationException.class,
                () -> service.create(new UserRequest("X", "x@ex.com", "TECH")));
    }

    @Test
    void delete_shouldRemove_andThen404() {
        UserDTO saved = service.create(new UserRequest("Del", "del@ex.com", "USER"));
        service.delete(saved.id());
        assertThrows(NotFoundException.class, () -> service.findById(saved.id()));
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> service.delete(123L));
    }
}
