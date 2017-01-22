package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.PostDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.request.CreatePostRequest;
import ru.forum.model.request.PostRequest;
import ru.forum.model.request.UpdatePostRequest;
import ru.forum.model.request.VotePostRequest;
import ru.forum.service.PostService;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("Duplicates")
@RestController
public class PostController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class);
    private PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @RequestMapping(path = "/db/api/post/create/", method = RequestMethod.POST)
    public ResponseEntity createPost(@RequestBody CreatePostRequest request) {
        try {
            final PostDataSet post = postService.createPost(request.getDate(), request.getThread(), request.getMessage(),
                    request.getUser(), request.getForum(), request.getParent(), request.isApproved(),
                    request.isHighlighted(), request.isEdited(), request.isSpam(), request.isDeleted());
            if (post == null) {
                LOGGER.error("Unable to close thread!");
                return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
            }
            return ResponseEntity.ok(new Response<>(0, post));
        } catch (DbException e) {
            LOGGER.error("Unable to create post:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/post/details/", method = RequestMethod.GET)
    public ResponseEntity threadDetails(@RequestParam(value = "post") int postId,
                                        @RequestParam(value = "related", required = false) String[] related) {
        final ArrayList<String> relatedList = new ArrayList<>();
        if (related != null)
            Collections.addAll(relatedList, related);
        try {
            final PostFull post = postService.postDetails(postId, relatedList);
            if (post == null)
                return ResponseEntity.ok(new Response<>(1, "No such post!"));
            return ResponseEntity.ok(new Response<>(0, post));
        } catch (DbException e) {
            LOGGER.error("Unable to get post:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/post/list/", method = RequestMethod.GET)
    public ResponseEntity listPosts(@RequestParam(value = "thread", required = false) Integer threadId,
                                           @RequestParam(value = "forum", required = false) String forum,
                                           @RequestParam(value = "limit", required = false) Integer limit,
                                           @RequestParam(value = "order", required = false) String order,
                                           @RequestParam(value = "since", required = false) String since) {
        if (forum == null) {
            try {
                final ArrayList<PostFull> list = postService.listPostsByThread(threadId, since, limit, order);
                return ResponseEntity.ok(new Response<>(0, list));
            } catch (DbException e) {
                LOGGER.error("Unable to list posts by thread:", e);
                return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
            }
        }
        else {
            try {
                final ArrayList<PostFull> list = postService.listPostsByForum(forum, since, limit, order);
                return ResponseEntity.ok(new Response<>(0, list));
            } catch (DbException e) {
                LOGGER.error("Unable to list posts by forum:", e);
                return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
            }
        }
    }

    @RequestMapping(path = "/db/api/post/remove/", method = RequestMethod.POST)
    public ResponseEntity removeThread(@RequestBody PostRequest request) {
        try {
            if (postService.removePost(request.getPost()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such post!"));
        } catch (DbException e) {
            LOGGER.error("Unable to remove post:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/post/restore/", method = RequestMethod.POST)
    public ResponseEntity restoreThread(@RequestBody PostRequest request) {
        try {
            if (postService.restorePost(request.getPost()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such post!"));
        } catch (DbException e) {
            LOGGER.error("Unable to restore post:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/post/update/", method = RequestMethod.POST)
    public ResponseEntity updateThread(@RequestBody UpdatePostRequest request) {
        try {
            final PostDataSet post = postService.updatePost(request.getPost(), request.getMessage());
            if (post == null)
                return ResponseEntity.ok(new Response<>(1, "No such post!"));
            return ResponseEntity.ok(new Response<>(0, request));
        } catch (DbException e) {
            LOGGER.error("Unable to update post:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/post/vote/", method = RequestMethod.POST)
    public ResponseEntity voteThread(@RequestBody VotePostRequest request) {
        try {
            final PostDataSet post = postService.votePost(request.getPost(), request.getVote());
            if (post == null)
                return ResponseEntity.ok(new Response<>(1, "No such post!"));
            return ResponseEntity.ok(new Response<>(0, request));
        } catch (DbException e) {
            LOGGER.error("Unable to vote post:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

}
