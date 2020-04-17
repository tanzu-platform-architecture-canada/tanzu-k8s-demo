package io.todos.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class TodosController {

    private static Logger LOG = LoggerFactory.getLogger(TodosController.class);

    @Value("${todos.api.limit:100}")
    long _limit;

    @Value("${cacheUrl:http://localhost:8888}")
    String _cacheUrl;
    private RestTemplate _cacheTemplate = new RestTemplate();

    @Value("${backendUrl:http://localhost:9090}")
    String _backendUrl;
    private RestTemplate _backendTemplate = new RestTemplate();

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static String DEFAULT_GROUP = "Default group";

    @GetMapping("/")
    public List<Todo> retrieve() {
        LOG.debug("Retrieving all Todos");
        Todo[] cached = _cacheTemplate.getForEntity(_cacheUrl, Todo[].class).getBody();
        //if cache is empty, hydrate
        if(cached.length < 1) {
            LOG.debug("Cache empty, retrieving from backend service");
            Todo[] backendResp = _backendTemplate.getForEntity(_backendUrl, Todo[].class).getBody();
            if(backendResp.length > 0)    Arrays.stream(backendResp)
                    .forEach(e->_cacheTemplate.postForObject(_cacheUrl, e, Todo.class));
            return Arrays.asList(backendResp);
        } else {
            //Return cached list
            return Arrays.asList(cached);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    public Todo create(@RequestBody Todo todo) {
        // check if cache size is not over the limit
        throwIfOverLimit();

        if(todo.getTitle() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.title cannot be null on put");
        }

        LOG.debug("Creating TODO: " + todo);
        Todo obj = new Todo();
        if(ObjectUtils.isEmpty(todo.getId())) {
            obj.setId(UUID.randomUUID().toString());
        } else {
            obj.setId(todo.getId());
        }
        if(!ObjectUtils.isEmpty(todo.getTitle())) {
            obj.setTitle(todo.getTitle());
        }
        if(!ObjectUtils.isEmpty(todo.isComplete())) {
            obj.setComplete(todo.isComplete());
        }
        if(ObjectUtils.isEmpty(todo.getCategory())) {
            obj.setCategory(DEFAULT_GROUP);
        } else {
            obj.setCategory(todo.getCategory());
        }
        if(ObjectUtils.isEmpty(todo.getDeadline())) {
            obj.setDeadline(dtf.format(LocalDateTime.now()));
        } else {
            obj.setDeadline(todo.getDeadline());
        }

        //Write to DB
        Todo saved = _backendTemplate.postForObject(_backendUrl, obj, Todo.class);
        LOG.debug("Created in Backend");

        //Invalidate/Add Cache
        Todo cached = _cacheTemplate.postForObject(_cacheUrl, saved, Todo.class);
        LOG.debug("Created in Cache");
        return saved;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{id}")
    public Todo put(@PathVariable String id, @RequestBody Todo todo) {
        if(todo.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.id cannot be null on put");
        }
        if(!todo.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.id ${todo.id} and id $id are inconsistent");
        }

        Todo obj = new Todo();
        obj.setId(todo.getId());

        if(!ObjectUtils.isEmpty(todo.isComplete())) {
            obj.setComplete(todo.isComplete());
        }
        if(ObjectUtils.isEmpty(todo.getCategory())) {
            obj.setCategory(DEFAULT_GROUP);
        } else {
            obj.setCategory(todo.getCategory());
        }
        if(ObjectUtils.isEmpty(todo.getDeadline())) {
            obj.setDeadline(dtf.format(LocalDateTime.now()));
        } else {
            obj.setDeadline(todo.getDeadline());
        }

        //Write to DB
        Todo saved = _backendTemplate.postForObject(_backendUrl, obj, Todo.class);
        LOG.debug("Created in Backend");

        //Invalidate/Add Cache
        Todo cached = _cacheTemplate.postForObject(_cacheUrl, saved, Todo.class);
        LOG.debug("Created in Cache");
        return saved;
    }

    @DeleteMapping("/")
    public void deleteAll() {
        LOG.debug("Removing all Todos");

        //Remove from DB
        _backendTemplate.delete(_backendUrl);
        //Remove from Cache
        _cacheTemplate.delete(_cacheUrl);
    }

    @GetMapping("/{id}")
    public Todo retrieve(@PathVariable("id") String id) {
        LOG.debug("Retrieving Todo: " + id);
        //Check cache + DB
        Todo cached = null;
        try {
            cached = _cacheTemplate.getForEntity(_cacheUrl + "/" + id, Todo.class).getBody();
        } catch (HttpStatusCodeException ex) {
            if(ex.getRawStatusCode() != 404) {
                LOG.error("Caching service error downstream", ex);
                throw ex;
            }
        }

        if(cached != null) {
            // found in cache
            LOG.debug("Found cached version");
            return cached;
        } else {
            LOG.debug("Not in cache, retrieving from backend");

            Todo source = null;
            try{
                source = _backendTemplate.getForEntity(_backendUrl + "/" + id, Todo.class).getBody();
            } catch (HttpStatusCodeException ex) {
                if(ex.getRawStatusCode() != 404) {
                    LOG.error("Database service error downstream", ex);
                    throw ex;
                }
            }

            if(source != null) {
                LOG.debug("Found in backend");
                cached = _cacheTemplate.postForObject(_cacheUrl, source, Todo.class);
                return source;
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "todo.id = " + id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id){
        //Remove from DB
        _backendTemplate.delete(_backendUrl + "/" + id);

        //Remove from Cache
        _cacheTemplate.delete(_cacheUrl + "/" + id);
    }

    @PatchMapping("/{id}")
    public Todo update(@PathVariable("id") String id, @RequestBody Todo todo) {
        if(todo.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.id cannot be null on put");
        }
        if(!todo.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.id ${todo.id} and id $id are inconsistent");
        }

        Todo obj = new Todo();
        obj.setId(todo.getId());

        if(!ObjectUtils.isEmpty(todo.getTitle())) {
            obj.setTitle(todo.getTitle());
        }
        if(!ObjectUtils.isEmpty(todo.isComplete())) {
            obj.setComplete(todo.isComplete());
        }
        if(ObjectUtils.isEmpty(todo.getCategory())) {
            obj.setCategory(DEFAULT_GROUP);
        } else {
            obj.setCategory(todo.getCategory());
        }
        if(ObjectUtils.isEmpty(todo.getDeadline())) {
            obj.setDeadline(dtf.format(LocalDateTime.now()));
        } else {
            obj.setDeadline(todo.getDeadline());
        }

        //Write to DB
        Todo saved = _backendTemplate.postForObject(_backendUrl, obj, Todo.class);
        LOG.debug("Created in Backend");

        //Invalidate/Add Cache
        Todo cached = _cacheTemplate.postForObject(_cacheUrl, saved, Todo.class);
        LOG.debug("Created in Cache");
        return saved;
    }

    private void throwIfOverLimit() {
        Todo[] cached = _cacheTemplate.getForEntity(_cacheUrl, Todo[].class).getBody();
        if(cached.length >= _limit) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.api.limit=$limit, todos.size=$count");
        } else {
            return;
        }
    }

}
