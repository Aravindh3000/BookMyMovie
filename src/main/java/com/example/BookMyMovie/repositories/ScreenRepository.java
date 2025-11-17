package com.example.BookMyMovie.repositories;

import com.example.BookMyMovie.models.Screen;
import com.example.BookMyMovie.models.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    Optional<Screen> findByNameAndTheatre(String name, Theatre theatre);
}
