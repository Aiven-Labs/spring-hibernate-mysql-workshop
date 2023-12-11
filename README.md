[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/aiven-labs/spring-hibernate-mysql-workshop)

# Data Persistence 101: Spring Boot and MySQL Edition

In this hands-on workshop, you'll learn how to connect your Spring Boot application to MySQL using the well-known ORM tool Hibernate.

## Prerequisites

* Github account
* Aiven account
* A Browser

## Creating your MySQL Service 

* Create your Aiven account by using this [link](https://go.aiven.io/signup-data-persist) 
* Confirm your email and connect to the console. 
* Create a new MySQL Service, choose `Free plan` , give it the name you want and hit "Create service". You can skip the 2 steps. 


### Create a new database

On your MySQL service page, on the left side panel you can see a `Databases` button click on it, from there, on the main panel you will see a big blue button 'Create database', click it a create a new database named `cats`.

## Connecting your Springboot application to MySQL

Open the file `src/main/resources/application.properties` and replace the :

* Host
* Port
* Password

## Create your first `Entity`

We are going to create our first entity, object that we will be persisting in the database. Create a new folder `model` under `src/main/java/io/aiven/dataworkshop` and then create a new file named `Cat.java` in `src/main/java/io/aiven/dataworkshop/model` and add this content : 

```java
package io.aiven.dataworkshop.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cat {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String name;
    
    private int age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}

```

Start the application to check that everything runs fine : `mvn spring-boot:run` , check the logs and find the sql statements. 

## Create the repository

Create a file name `CatRepository.java` in `src/main/java/io/aiven/dataworkshop` and add this content : 

```java
package io.aiven.dataworkshop;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.aiven.dataworkshop.model.Cat;

public interface CatRepository extends JpaRepository<Cat, Integer> {
    
}


```

## Create a REST Service for the Cat entity

Create a new file named `CatController.java` in `src/main/java/io/aiven/dataworkshop` and add this content : 

```java
package io.aiven.dataworkshop;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.aiven.dataworkshop.model.Cat;

@RestController 
@RequestMapping(path = "/cats")
public class CatController {
    @Autowired 
    private CatRepository catRepository;

    @PostMapping() 
    public Cat addNewCat(@RequestBody Cat cat) {
        catRepository.save(cat);
        return cat;
    }

    @GetMapping()
    public Iterable<Cat> getCats() {
        return catRepository.findAll();
    }

}

```

You can now start inserting and retrieving cats in the database ! Open a second terminal and try out the rest endpoints : 

```bash

curl localhost:8080/cats -H 'Content-Type: application/json' -d '{"name" : "Scratch", "age" : 5}'
curl localhost:8080/cats -H 'Content-Type: application/json' -d '{"name" : "Link", "age" : 4}'
curl localhost:8080/cats

```

## Adding a new `finder` method

Let's add a endpoint to retrieve the cats by `id` , add this method to `CatController.java` : 

```java

 @GetMapping("/{id}")
    public Optional<Cat> getCatById(@PathVariable Integer id) {
        return catRepository.findById(id);
    }

```

Stop and start the app, now you can search by `id` : 

```bash

curl localhost:8080/cats -H 'Content-Type: application/json' -d '{"name" : "Scratch", "age" : 5}'
curl localhost:8080/cats -H 'Content-Type: application/json' -d '{"name" : "Link", "age" : 4}'
curl localhost:8080/cats/1

```

## Add a new entity and create a relationship 

Let's assume to each cat has an owner and we want to reflect this in our application. Let's create a new entity named `Owner.java` in `src/main/java/io/aiven/dataworkshop/model` and this content : 

```java
package io.aiven.dataworkshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Owner {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}

```

Ok, now we need to create a relationship between the `Cat` and the `Owner` entity, let's modify the `Cat.java` by adding a new field and the getters and setters : 

```java
@ManyToOne(cascade = CascadeType.PERSIST)
Owner owner;

public Owner getOwner() {
    return owner;
}

public void setOwner(Owner owner) {
    this.owner = owner;
}

```

Stop the app and restart it, check the logs, you might see an exception but you can ignore it, see the SQL statements with the new column and the foreign key statement. 

## Create and persist a relation

For simplicity, we will add a new rest endpoint that will have a hardcoded `Owner` , modify your `CatController.java` and add this method : 

```java

@PostMapping(path = "/addToOwner") 
public Cat addNewCatToSebi(@RequestBody Cat cat) {
    Owner owner = new Owner();
    owner.setName("sebi");
    cat.setOwner(owner);
    catRepository.save(cat);
    return cat;
}

```

Stop and start the application and run those curl commands : 

```bash

curl localhost:8080/cats/addToOwner -H 'Content-Type: application/json' -d '{"name" : "Link", "age" : 4}'
curl localhost:8080/cats/1

```

In the response, you should now see that an owner is attached to the cat. 


## Add a `@Query` using `jpql`

Let's a `jpql` query to retrieve all the cats by owner id , modify the `CatRepository.java` interface and add this method signature : 

```java

@Query("SELECT c FROM Cat c WHERE c.owner.id =?1")
List<Cat> findCatsByOwner(Integer id);

```

Notice the syntax, it's really close to "vanilla" SQL but we manipulate objects here (i.e `c.owner.id`)

Add a new rest endpoint to your controller `CatController.java` : 

```java
@GetMapping(path = "/owner/{id}")
public List<Cat> findByOwner(@PathVariable Integer id) {
    return catRepository.findCatsByOwner(id);
}

```

Stop and start the application and run those curl commands : 

```bash

curl localhost:8080/cats/addToOwner -H 'Content-Type: application/json' -d '{"name" : "Link", "age" : 4}'
curl localhost:8080/cats/owner/1

```

You should see a list of `Cat` objects with just one element, you can now retrieve cats by owner id ! 

## Bonus track : cache your entities with Redis 


//TODO
