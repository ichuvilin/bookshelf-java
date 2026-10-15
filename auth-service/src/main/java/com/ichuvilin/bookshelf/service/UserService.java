package com.ichuvilin.bookshelf.service;

import com.ichuvilin.bookshelf.db.entity.User;
import com.ichuvilin.bookshelf.db.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    @Transactional
    public User create(String username, String email, String password) {
        User user = User.builder()
                .username(username).email(email).password(password)
                .build();

        return repository.save(user);
    }

}
