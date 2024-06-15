package com.aluraone.literalura.repository;

import com.aluraone.literalura.model.Autor;
import com.aluraone.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByIdiomasContains(String idiomas);

    Optional<Libro> findByTituloContains(String titulo); //esto puse

}
