package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.UserDataSet;
import ru.forum.model.full.UserFull;
import ru.forum.model.request.CreateUserRequest;
import ru.forum.model.request.FollowUserRequest;
import ru.forum.service.UserService;

@SuppressWarnings("unused")
@RestController
public class UserController {

    private UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/api/user/create/", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createUser(@RequestBody CreateUserRequest request) {
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

    @RequestMapping(path = "/api/user/details/", method = RequestMethod.GET)
    public ResponseEntity userDetails(@RequestParam(value = "user") String email) {
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

    @RequestMapping(path = "/api/user/follow/", method = RequestMethod.POST)
    private ResponseEntity followUser(@RequestBody FollowUserRequest request) {
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

}
