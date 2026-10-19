
package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.UpdateUserRequest;
import com.ichuvilin.bookshelf.db.entity.User;
import com.ichuvilin.bookshelf.db.repository.UserRepository;
import com.ichuvilin.bookshelf.domain.UserPublic;
import com.ichuvilin.bookshelf.exception.UserNotFoundException;
import com.ichuvilin.bookshelf.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_whenUserExists_returnsPublicUser() {
        User user = createUser(1L, "ivan");

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserPublic result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("ivan", result.getUsername());

        verify(repository).findById(1L);
    }

    @Test
    void getUserById_whenUserDoesNotExist_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(999L)
        );
    }

    @Test
    void update_whenUsernameIsAvailable_updatesUser() {
        User user = createUser(1L, "ivan");
        UpdateUserRequest request = new UpdateUserRequest("new-ivan");

        when(repository.existsByUsername("new-ivan")).thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserPublic result = userService.update(1L, request);

        assertEquals("new-ivan", result.getUsername());
        assertEquals("new-ivan", user.getUsername());

        verify(repository).save(user);
    }

    @Test
    void update_whenUsernameAlreadyExists_throwsException() {
        UpdateUserRequest request = new UpdateUserRequest("existing");

        when(repository.findById(1L)).thenReturn(Optional.of(createUser(1L, "ivan")));
        when(repository.existsByUsername("existing")).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.update(1L, request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenUserDoesNotExist_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update(999L, new UpdateUserRequest("new-ivan"))
        );

        verify(repository, never()).save(any());
    }

    private User createUser(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email("ivan@example.com")
                .password("hashed-password")
                .build();
    }
}
