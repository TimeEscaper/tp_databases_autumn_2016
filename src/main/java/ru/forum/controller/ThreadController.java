package ru.forum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.forum.database.exception.DbException;
import ru.forum.model.Response;
import ru.forum.model.dataset.PostDataSet;
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

    @RequestMapping(path = "/db/api/thread/close/", method = RequestMethod.POST)
    public ResponseEntity closeThread(@RequestBody ThreadRequest request) {
        System.out.print("receive");
        try {
            if (threadService.closeThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to close thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/thread/create/", method = RequestMethod.POST)
    public ResponseEntity createThread(@RequestBody CreateThreadRequest request) {
        System.out.print("receive");
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

    @RequestMapping(path = "/db/api/thread/details/", method = RequestMethod.GET)
    public ResponseEntity threadDetails(@RequestParam(value = "thread") int threadId,
                                        @RequestParam(value = "related", required = false) String[] related) {
        System.out.print("receive");
        final ArrayList<String> relatedList = new ArrayList<>();
        if (related != null)
            Collections.addAll(relatedList, related);
        for (String value : relatedList) {
            if ((!value.equals("user")) && (!value.equals("forum")))
                return ResponseEntity.ok(new Response<>(3, "Wrong query!"));
        }
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

    @RequestMapping(path = "/db/api/thread/list/", method = RequestMethod.GET)
    public ResponseEntity listThread(@RequestParam(value = "user", required = false) String user,
                                           @RequestParam(value = "forum", required = false) String forum,
                                           @RequestParam(value = "limit", required = false) Integer limit,
                                           @RequestParam(value = "order", required = false) String order,
                                           @RequestParam(value = "since", required = false) String since) {
        System.out.print("receive");
        if (forum == null) {
            try {
                final ArrayList<ThreadDataSet> list = threadService.listThread(user, true, since, limit, order);
                return ResponseEntity.ok(new Response<>(0, list));
            } catch (DbException e) {
                LOGGER.error("Unable to list threads:", e);
                return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
            }
        }
        else {
            try {
                final ArrayList<ThreadDataSet> list = threadService.listThread(forum, false, since, limit, order);
                return ResponseEntity.ok(new Response<>(0, list));
            } catch (DbException e) {
                LOGGER.error("Unable to list threads:", e);
                return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
            }
        }
    }

    @RequestMapping(path = "/db/api/thread/listPosts/", method = RequestMethod.GET)
    public ResponseEntity listPosts(@RequestParam(value = "thread") int threadId,
                                    @RequestParam(value = "sort", required = false) String sort,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "order", required = false) String order,
                                    @RequestParam(value = "since", required = false) String since) {
        System.out.print("receive");
        try {
            final ArrayList<PostDataSet> list = threadService.listPosts(threadId, since, limit, order, sort);
            return ResponseEntity.ok(new Response<>(0, list));
        } catch (DbException e) {
            LOGGER.error("Unable to list posts by thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/thread/open/", method = RequestMethod.POST)
    public ResponseEntity openThread(@RequestBody ThreadRequest request) {
        System.out.print("receive");
        try {
            if (threadService.openThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to open thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/thread/remove/", method = RequestMethod.POST)
    public ResponseEntity removeThread(@RequestBody ThreadRequest request) {
        System.out.print("receive");
        try {
            if (threadService.removeThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to remove thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/thread/restore/", method = RequestMethod.POST)
    public ResponseEntity restoreThread(@RequestBody ThreadRequest request) {
        System.out.print("receive");
        try {
            if (threadService.restoreThread(request.getThread()))
                return ResponseEntity.ok(new Response<>(0, request));
            return ResponseEntity.ok(new Response<>(1, "No such thread!"));
        } catch (DbException e) {
            LOGGER.error("Unable to restore thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/thread/update/", method = RequestMethod.POST)
    public ResponseEntity updateThread(@RequestBody UpdateThreadRequest request) {
        System.out.print("receive");
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
    @RequestMapping(path = "/db/api/thread/subscribe/", method = RequestMethod.POST)
    public ResponseEntity subscribeThread(@RequestBody SubscribeRequest request) {
        System.out.print("receive");
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

    @RequestMapping(path = "/db/api/thread/unsubscribe/", method = RequestMethod.POST)
    public ResponseEntity unsubscribeThread(@RequestBody SubscribeRequest request) {
        System.out.print("receive");
        try {
            final SubscriptionDataSet subs = threadService.unsubscribeThread(request.getUser(), request.getThread());
            if (subs == null)
                return ResponseEntity.ok(new Response<>(1, "No such thread!"));
            return ResponseEntity.ok(new Response<>(0, subs));
        } catch (DbException e) {
            LOGGER.error("Unable to unsubscribe thread:", e);
            return ResponseEntity.ok(new Response<>(4, "Inner service error!"));
        }
    }

    @RequestMapping(path = "/db/api/thread/vote/", method = RequestMethod.POST)
    public ResponseEntity voteThread(@RequestBody VoteThreadRequest request) {
        System.out.print("receive");
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
