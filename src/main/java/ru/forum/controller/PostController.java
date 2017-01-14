package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.forum.service.PostService;
import ru.forum.service.ThreadService;

@RestController
public class PostController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class);
    private PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }
    
}
