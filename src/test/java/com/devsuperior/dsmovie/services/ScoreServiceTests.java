package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private ScoreRepository scoreRepository;

	@Mock
	private UserService userService;

	private ScoreDTO scoreDTO;
	private MovieEntity movieEntity;
	private Long nonExistingMovieId;
	private ScoreEntity scoreEntity;
	private UserEntity userEntity;

	@BeforeEach
	void setUp() throws Exception {
		nonExistingMovieId = 2L;

		userEntity = UserFactory.createUserEntity();
		scoreDTO = ScoreFactory.createScoreDTO();

		//findById
		movieEntity = MovieFactory.createMovieEntity();
		Mockito.when(movieRepository.findById(scoreDTO.getMovieId())).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenThrow(ResourceNotFoundException.class);

		//save and flush
		scoreEntity = ScoreFactory.createScoreEntity();
		Mockito.when(scoreRepository.saveAndFlush(scoreEntity)).thenReturn(scoreEntity);

		//save
		movieEntity.getScores().add(scoreEntity);
		Mockito.when(movieRepository.save(movieEntity)).thenReturn(movieEntity);
	}
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {
		Mockito.when(userService.authenticated()).thenReturn(userEntity);
		MovieDTO result = service.saveScore(scoreDTO);
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
		Mockito.when(userService.authenticated()).thenReturn(userEntity);
		scoreEntity.setMovie(new MovieEntity());
		scoreDTO = new ScoreDTO(scoreEntity);

		Assertions.assertThrows(ResourceNotFoundException.class, () ->
				service.saveScore(scoreDTO));
	}
}
