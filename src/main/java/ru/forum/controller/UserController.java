package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.UserDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.UserFull;
import ru.forum.model.request.CreateUserRequest;
import ru.forum.model.request.FollowUserRequest;
import ru.forum.model.request.UpdateUserRequest;
import ru.forum.service.UserService;

import java.util.List;

@SuppressWarnings({"unused", "Duplicates"})
@RestController
public class UserController {

    private UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/db/api/user/create/", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createUser(@RequestBody CreateUserRequest request) {
        System.out.println("/db/api/user/create/");
        try {
            final UserDataSet user = userService.createUser(request.getUsername(), request.getAbout(), request.getName(),
                    request.getEmail(), request.isAnonymous());
            if (user == null)
                return ResponseEntity.ok(new Response<>(5, "User already exists!"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            LOGGER.error("Unable to add user to database:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/user/details/", method = RequestMethod.GET)
    public ResponseEntity userDetails(@RequestParam(value = "user") String email) {
        System.out.println("/db/api/user/details/");
        try {
            final UserFull user = userService.getUserDetails(email);
            if (user == null)
                return ResponseEntity.ok(new Response<>(1, "No such user"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            LOGGER.error("Unable to get user from database:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/user/follow/", method = RequestMethod.POST)
    public ResponseEntity followUser(@RequestBody FollowUserRequest request) {
        System.out.println("/db/api/user/follow/");
        try {
            final UserFull user = userService.followUser(request.getFollower(), request.getFollowee());
            if (user == null)
                return ResponseEntity.ok(new Response<>(1, "No such user"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            LOGGER.error("Unable to follow user:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/user/unfollow/", method = RequestMethod.POST)
    public ResponseEntity unfollowUser(@RequestBody FollowUserRequest request) {
        System.out.println( "/db/api/user/unfollow/");
        try {
            final UserFull user = userService.unfollowUser(request.getFollower(), request.getFollowee());
            if (user == null)
                return ResponseEntity.ok(new Response<>(1, "No such user"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            LOGGER.error("Unable to unfollow user:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/user/listFollowers/", method = RequestMethod.GET)
    public ResponseEntity listFollowers(@RequestParam(value = "user") String email,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "order", required = false) String order,
                                        @RequestParam(value = "since_id", required = false) Integer sinceId) {
        System.out.println("/db/api/user/listFollowers/");
        try {
            final List<UserFull> followers = userService.listFollowers(email, limit, order, sinceId);
            return ResponseEntity.ok(new Response<>(0, followers));
        } catch (DbException e) {
            LOGGER.error("Unable to listFollowers:",e );
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/user/listFollowing/", method = RequestMethod.GET)
    public ResponseEntity listFollowing(@RequestParam(value = "user") String email,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "order", required = false) String order,
                                        @RequestParam(value = "since_id", required = false) Integer sinceId) {
        System.out.println("/db/api/user/listFollowing/");
        try {
            final List<UserFull> followings = userService.listFollowing(email, limit, order, sinceId);
            return ResponseEntity.ok(new Response<>(0, followings));
        } catch (DbException e) {
            LOGGER.error("Unable to listFollowing:",e );
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/user/listPosts/", method = RequestMethod.GET)
    public ResponseEntity listFollowing(@RequestParam(value = "user") String email,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "order", required = false) String order,
                                        @RequestParam(value = "since", required = false) String since) {
        System.out.println("/db/api/user/listPosts/");
        try {
            final List<PostFull> posts = userService.listPosts(email, since, limit, order);
            return ResponseEntity.ok(new Response<>(0, posts));
        } catch (DbException e) {
            LOGGER.error("Unable to list posts:",e );
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }


    @RequestMapping(path = "/db/api/user/updateProfile/", method = RequestMethod.POST)
    public ResponseEntity updateUser(@RequestBody UpdateUserRequest request) {
        System.out.println("/db/api/user/updateProfile/");
        try {
            final UserFull user = userService.updateUser(request.getUser(), request.getAbout(), request.getName());
            if (user == null)
                return ResponseEntity.ok(new Response<>(1, "No such user"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            LOGGER.error("Unable to update user:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

}
