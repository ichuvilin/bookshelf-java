package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.api.model.request.UpdateUserRequest;
import com.ichuvilin.bookshelf.db.entity.User;
import com.ichuvilin.bookshelf.db.repository.UserRepository;
import com.ichuvilin.bookshelf.domain.UserPublic;
import com.ichuvilin.bookshelf.exception.UserNotFoundException;
import com.ichuvilin.bookshelf.exception.UsernameAlreadyExistsException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public User create(String username, String email, String password) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();

        return repository.saveAndFlush(user);
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public UserPublic getUserById(Long userId) {
        User user = repository.findById(userId).orElseThrow(UserNotFoundException::new);
        return UserPublic.toPublic(user);
    }

    @Transactional
    public UserPublic update(Long userId, @Valid UpdateUserRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.setUsername(request.getUsername());

        repository.save(user);

        return UserPublic.toPublic(user);
    }
}
