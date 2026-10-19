package com.ichuvilin.bookshelf.db.repository;

import com.ichuvilin.bookshelf.db.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {}
