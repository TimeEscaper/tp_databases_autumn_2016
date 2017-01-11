package ru.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.UserDataSet;
import ru.forum.model.full.UserFull;
import ru.forum.model.request.CreateUserRequest;
import ru.forum.service.UserService;

@SuppressWarnings("unused")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/api/user/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createUser(@RequestBody CreateUserRequest request) {
        try {
            final UserDataSet user = userService.createUser(request.getUsername(), request.getAbout(), request.getName(),
                    request.getEmail(), request.isAnonymous());
            if (user == null)
                return ResponseEntity.ok(new Response<>(5, "User already exists!"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/user/details", method = RequestMethod.GET)
    public ResponseEntity userDetails(@RequestParam(value = "user") String email) {
        try {
            final UserFull user = userService.getUserDetails(email);
            if (user == null)
                return ResponseEntity.ok(new Response<>(1, "No such user"));
            return ResponseEntity.ok(new Response<>(0, user));
        } catch (DbException e) {
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

}
