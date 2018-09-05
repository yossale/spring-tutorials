# Spring Boot, PostgreSQL, JPA, Hibernate RESTful CRUD API Example 
https://www.callicoder.com/spring-boot-jpa-hibernate-postgresql-restful-crud-api-example/

## Creating the project 

Don't sweat it. Use the [Spring Initiazlier](https://start.spring.io/) to generate the pom you need.

### Connect the db
```
## Spring DATASOURCE (DataSourceAutoConfiguration & DataSourceProperties)
spring.datasource.url=jdbc:postgresql://${self.db_uri}:${self.db_port}/${self.db_name}

# The SQL dialect makes Hibernate generate better SQL for the chosen database
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect

# Hibernate ddl auto (create, create-drop, validate, update)
spring.jpa.hibernate.ddl-auto = update
```

and in your application-{env}.properties, update the relevant values of self.*

### Building the models

Basically, you can just add your models and assign them to the tables. However, if you want to have created/updated at fields, there's a better way to do it, and it's by using the AuditModel and then inherite from it, and add the `@EnableJpaAuditing` to your main class annotation.

> Notice: All the Annotations so far in the models have nothing to do with Spring - this is the javax.persistence annotations, the JPA - Java Persistency API. Hibernate is the default implementation in Spring for the JPA, but it can be replaced if you want to.

```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdAt", "updatedAt"},
        allowGetters = true
)
public abstract class AuditModel implements Serializable {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Date updatedAt;

}
```

And then, the models (Question and Answer) can be used like this:
```java
package com.yossale.restcrudspring.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "questions")
public class Question extends AuditModel {

    @Id
    @GeneratedValue(generator = "question_generator")
    @SequenceGenerator(
            name = "question_generator",
            sequenceName = "question_sequence",
            initialValue = 1000
    )
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

}

```

Now you can basically run your code. It will fail because you don't have the model tables. You can either create them manually, or ask hibernate to create them for you - just add the follwoing line to your application.properties file.
`spring.jpa.hibernate.ddl-auto=create-drop`

> (there are several values you can write there, like validate, none, create, create-drop , but we'll go over it somewhere else). The create-drop is the best case for testing and playing around.

## Repositories 

OK, so we have the models. But we'll probably want to query them and, you know, do stuff with them, like creating them, returning them, and so forth. What we need is something to do all the CRUD operations. 

Luckily, Spring is very nice in that aspect: It suggest pre-made repositories:  CrudRepository, PagingAndSortingRepository and JpaRepository. You can read about [the difference between them here](https://www.baeldung.com/spring-data-repositories). 

And all you need to do to use these repo is simply create an Interface that extends them! like this: (yes, it is empty. All the code is reflected by spring)

```java
package com.yossale.restcrudspring.repository;

import com.yossale.restcrudspring.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
```

## REST API's

So we have the models, we have the crud repo that can do things with the models, we have hibernate to connect us to the db - now we need something customer facing!

These are the controllers:

```java
import com.yossale.restcrudspring.model.Question;
import com.yossale.restcrudspring.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/questions")
    public Page<Question> getQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }


    @PostMapping("/questions")
    public Question createQuestion(@Valid @RequestBody Question question) {
        return questionRepository.save(question);
    }

    @PutMapping("/questions/{questionId}")
    public Question updateQuestion(@PathVariable Long questionId,
            @Valid @RequestBody Question questionRequest) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    question.setTitle(questionRequest.getTitle());
                    question.setDescription(questionRequest.getDescription());
                    return questionRepository.save(question);
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }


    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    questionRepository.delete(question);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }
}
```

The one interesting trick here is the good use of the Optional class and the ResourceNotFoundException - 

```java
return questionRepository.findById(questionId)
                .map(question -> {
                    questionRepository.delete(question);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
```

# Open questions:
0. Testing: the controller, the service, and so forth
1. What makes the app stay in the air, waiting? 
2. How to test 
3. How do we sync changes to the db with our versions?















