package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository movieRepository;

	private Long existingId, nonExistingId, dependentMovieId;
	private PageImpl<MovieEntity> page;
	private MovieEntity movieEntity;
	private Pageable pageable;
	private MovieDTO movieDTO;

	@BeforeEach
	void setUp() throws Exception {

		existingId = 1L;
		nonExistingId = 2L;
		dependentMovieId = 3L;

		pageable = PageRequest.of(0, 10);

		//findAll
		movieEntity = MovieFactory.createMovieEntity();
		page = new PageImpl<>(List.of(movieEntity));
		Mockito.when(movieRepository.searchByTitle(movieEntity.getTitle(), pageable)).thenReturn(page);

		//save
		Mockito.when(movieRepository.save(ArgumentMatchers.any())).thenReturn(movieEntity);
		movieDTO = MovieFactory.createMovieDTO();

		//findById
		Mockito.when(movieRepository.findById(existingId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingId)).thenReturn(Optional.empty());

		//update
		Mockito.when(movieRepository.getReferenceById(existingId)).thenReturn(movieEntity);
		Mockito.when(movieRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		//delete
		Mockito.when(movieRepository.existsById(existingId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistingId)).thenReturn(false);
		Mockito.when(movieRepository.existsById(dependentMovieId)).thenReturn(true);

		Mockito.doNothing().when(movieRepository).deleteById(existingId);
		Mockito.doThrow(DatabaseException.class).when(movieRepository).deleteById(dependentMovieId);

	}
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Page<MovieDTO> result = service.findAll(movieEntity.getTitle(), pageable);
		Assertions.assertNotNull(result);
		Mockito.verify(movieRepository, Mockito.times(1)).searchByTitle(movieEntity.getTitle(), pageable);
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(existingId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals("Test Movie", result.getTitle());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () ->
				service.findById(nonExistingId));
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = service.insert(movieDTO);
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.update(existingId, movieDTO);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(existingId, result.getId());
		Assertions.assertEquals(movieDTO.getTitle(), result.getTitle());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () ->
				service.update(nonExistingId, movieDTO));
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentMovieId);
		});
	}
}
