package io.aiven.dataworkshop;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.aiven.dataworkshop.model.Cat;
import io.aiven.dataworkshop.model.Owner;

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

    @GetMapping("/{id}")
    public Optional<Cat> getCatById(@PathVariable Integer id) {
        return catRepository.findById(id);
    }

    @PostMapping(path = "/addToOwner") 
    public Cat addNewCatToSebi(@RequestBody Cat cat) {
       Owner owner = new Owner();
       owner.setName("sebi");
       cat.setOwner(owner);
       catRepository.save(cat);
       return cat;
    }

    @GetMapping(path = "/owner/{id}")
    public List<Cat> findByOwner(@PathVariable Integer id) {
        return catRepository.findCatsByOwner(id);
    }

}

