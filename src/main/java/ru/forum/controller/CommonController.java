package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.forum.database.exception.DbException;
import ru.forum.model.DbStatus;
import ru.forum.model.Response;
import ru.forum.service.CommonDbService;

@RestController
public class CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonController.class);

    private CommonDbService commonDbService;

    @Autowired
    public CommonController(CommonDbService commonDbService) { this.commonDbService = commonDbService; }

    @RequestMapping(path = "/db/api/clear/", method = RequestMethod.POST)
    public ResponseEntity clearDatabase() {
        System.out.print("receive");
        try {
            commonDbService.truncateAll();
            return ResponseEntity.ok(new Response<>(0, "OK"));
        } catch (DbException e) {
            LOGGER.error("Unable to clear database:", e);
            return ResponseEntity.ok(new Response<>(4, "Innerservice error!"));
        }
    }

    @RequestMapping(path = "/db/api/status/", method = RequestMethod.GET)
    public ResponseEntity getStatus() {
        System.out.print("receive");
        try {
            final DbStatus dbStatus = commonDbService.getStatus();
            return ResponseEntity.ok(new Response<>(0, dbStatus));
        } catch (DbException e) {
            LOGGER.error("Unable to get database status:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }
}
