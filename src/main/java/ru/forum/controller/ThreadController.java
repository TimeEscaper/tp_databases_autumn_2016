package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.SubscriptionDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.ThreadFull;
import ru.forum.model.request.*;
import ru.forum.service.ThreadService;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("Duplicates")
@RestController
public class ThreadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadController.class);
    private ThreadService threadService;

    @Autowired
    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @RequestMapping(path = "/api/thread/close/", method = RequestMethod.POST)
    public ResponseEntity closeThread(@RequestBody ThreadRequest request) {
        try {
            if (threadService.closeThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to close thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/create/", method = RequestMethod.POST)
    public ResponseEntity createThread(@RequestBody CreateThreadRequest request) {
        try {
            final ThreadDataSet thread = threadService.createThread(request.getForum(), request.getTitle(),
                    request.getUser(), request.getDate(), request.getMessage(), request.getSlug(),
                    request.isClosed(), request.isDeleted());
            if (thread == null)
                return ResponseEntity.ok(new Response<>(5, "Thread already exists!"));
            return ResponseEntity.ok(new Response<>(0, thread));
        } catch (DbException e) {
            LOGGER.error("Unable to create thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/details/", method = RequestMethod.GET)
    public ResponseEntity threadDetails(@RequestParam(value = "thread") int threadId,
                                        @RequestParam(value = "related[]", required = false) String[] related) {
        final ArrayList<String> relatedList = new ArrayList<>();
        if (related != null)
            Collections.addAll(relatedList, related);
        try {
            final ThreadFull thread = threadService.threadDetails(threadId, relatedList);
            if (thread == null)
                return ResponseEntity.ok(new Response<>(1, "No such thread!"));
            return ResponseEntity.ok(new Response<>(0, thread));
        } catch (DbException e) {
            LOGGER.error("Unable to get thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/list/", method = RequestMethod.GET)
    public ResponseEntity listThreadByUser(@RequestParam(value = "user") String user,
                                           @RequestParam(value = "limit", required = false) Integer limit,
                                           @RequestParam(value = "order", required = false) String order,
                                           @RequestParam(value = "since", required = false) String since) {
        try {
            final ArrayList<ThreadDataSet> list = threadService.listThread(user, true, since, limit, order);
            return ResponseEntity.ok(new Response<>(0, list));
        } catch (DbException e) {
            LOGGER.error("Unable to list threads:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/list/", method = RequestMethod.GET)
    public ResponseEntity listThreadByForum(@RequestParam(value = "forum") String user,
                                           @RequestParam(value = "limit", required = false) Integer limit,
                                           @RequestParam(value = "order", required = false) String order,
                                           @RequestParam(value = "since", required = false) String since) {
        try {
            final ArrayList<ThreadDataSet> list = threadService.listThread(user, false, since, limit, order);
            return ResponseEntity.ok(new Response<>(0, list));
        } catch (DbException e) {
            LOGGER.error("Unable to list threads:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/listPosts/", method = RequestMethod.GET)
    public ResponseEntity listPosts(@RequestParam(value = "thread") int threadId,
                                    @RequestParam(value = "sort") String sort,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false) String order,
                                    @RequestParam(value = "since", required = false) String since) {
        return ResponseEntity.ok(new Response<>(0, "Ok!"));
    }

    @RequestMapping(path = "/api/thread/open/", method = RequestMethod.POST)
    public ResponseEntity openThread(@RequestBody ThreadRequest request) {
        try {
            if (threadService.openThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to open thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/remove/", method = RequestMethod.POST)
    public ResponseEntity removeThread(@RequestBody ThreadRequest request) {
        try {
            if (threadService.removeThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to remove thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/restore/", method = RequestMethod.POST)
    public ResponseEntity restoreThread(@RequestBody ThreadRequest request) {
        try {
            if (threadService.restoreThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to remove thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/update/", method = RequestMethod.POST)
    public ResponseEntity updateThread(@RequestBody UpdateThreadRequest request) {
        try {
            final ThreadDataSet thread = threadService.updateThread(request.getThread(), request.getSlug(), request.getMessage());
            if (thread == null)
                return ResponseEntity.ok(new Response<>(1, "No such thread!"));
            return ResponseEntity.ok(new Response<>(0, request));
        } catch (DbException e) {
            LOGGER.error("Unable to update thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    //TODO: change to boolean
    @RequestMapping(path = "/api/thread/subscribe/", method = RequestMethod.POST)
    public ResponseEntity subscribeThread(@RequestBody SubscribeRequest request) {
        try {
            final SubscriptionDataSet subs = threadService.subscribeThread(request.getUser(), request.getThread());
            if (subs == null)
                return ResponseEntity.ok(new Response<>(1, "No such thread!"));
            return ResponseEntity.ok(new Response<>(0, subs));
        } catch (DbException e) {
            LOGGER.error("Unable to subscribe thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/unsubscribe/", method = RequestMethod.POST)
    public ResponseEntity unsubscribeThread(@RequestBody SubscribeRequest request) {
        try {
            final SubscriptionDataSet subs = threadService.subscribeThread(request.getUser(), request.getThread());
            if (subs == null)
                return ResponseEntity.ok(new Response<>(1, "No such thread!"));
            return ResponseEntity.ok(new Response<>(0, subs));
        } catch (DbException e) {
            LOGGER.error("Unable to unsubscribe thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/api/thread/vote/", method = RequestMethod.POST)
    public ResponseEntity voteThread(@RequestBody VoteRequest request) {
        try {
            final ThreadDataSet thread = threadService.voteThread(request.getThread(), request.getVote());
            if (thread == null)
                return ResponseEntity.ok(new Response<>(1, "No such thread!"));
            return ResponseEntity.ok(new Response<>(0, request));
        } catch (DbException e) {
            LOGGER.error("Unable to vote thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }


}
