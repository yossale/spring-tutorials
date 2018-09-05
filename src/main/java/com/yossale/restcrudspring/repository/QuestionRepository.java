package com.yossale.restcrudspring.repository;

import com.yossale.restcrudspring.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
