package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.full.ForumFull;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.ThreadFull;
import ru.forum.model.full.UserFull;
import ru.forum.model.request.CreateForumRequest;
import ru.forum.service.ForumService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class ForumController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForumController.class);
    private ForumService forumService;

    @Autowired
    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @RequestMapping(path = "/db/api/forum/create/", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8")
    public ResponseEntity createForum(@RequestBody CreateForumRequest request) {
        try {
            final ForumDataSet forum = forumService.createForum(request.getName(), request.getShortName(), request.getUser());
            if (forum == null)
                return ResponseEntity.ok(new Response<>(5, "Forum already exists!"));
            return ResponseEntity.ok(new Response<>(0, forum));
        } catch (DbException e) {
            LOGGER.error("Unable to create forum:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/forum/details/", method = RequestMethod.GET)
    public ResponseEntity forumDetails(@RequestParam(value = "forum") String shortName,
                                       @RequestParam(value = "related", required = false) String related) {
        final String user = related == null ? null : related;
        try {
            final ForumFull forum = forumService.forumDetails(shortName, user);
            if (forum == null)
                return ResponseEntity.ok(new Response<>(1, "No such forum"));
            return ResponseEntity.ok(new Response<>(0, forum));
        } catch (DbException e) {
            LOGGER.error("Unable to get forum from database:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/forum/listPosts/", method = RequestMethod.GET)
    public ResponseEntity listPosts(@RequestParam(value = "forum") String shortName,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false) String order,
                                    @RequestParam(value = "since", required = false) String since,
                                    @RequestParam(value = "related[]", required = false) String[] related) {
        final ArrayList<String> relatedList = new ArrayList<>();
        if (related != null)
            Collections.addAll(relatedList, related);
        try {
            final List<PostFull> list = forumService.listPosts(shortName, since, limit, order, relatedList);
            return ResponseEntity.ok(new Response<>(0, list));
        } catch (DbException e) {
            LOGGER.error("Unable to list posts:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/forum/listThreads/", method = RequestMethod.GET)
    public ResponseEntity listThreads(@RequestParam(value = "forum") String shortName,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false) String order,
                                    @RequestParam(value = "since", required = false) String since,
                                    @RequestParam(value = "related[]", required = false) String[] related) {
        final ArrayList<String> relatedList = new ArrayList<>();
        if (related != null)
            Collections.addAll(relatedList, related);
        try {
            final List<ThreadFull> list = forumService.listThreads(shortName, since, limit, order, relatedList);
            return ResponseEntity.ok(new Response<>(0, list));
        } catch (DbException e) {
            LOGGER.error("Unable to list threads:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/forum/listUsers/", method = RequestMethod.GET)
    public ResponseEntity listUsers(@RequestParam(value = "forum") String shortName,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "order", required = false) String order,
                                      @RequestParam(value = "since", required = false) String since) {
        try {
            final List<UserFull> list = forumService.listUsers(shortName, since, limit, order);
            return ResponseEntity.ok(new Response<>(0, list));
        } catch (DbException e) {
            LOGGER.error("Unable to list threads:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }


}
