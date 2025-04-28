package org.banana.service;

import lombok.extern.slf4j.Slf4j;
import org.banana.entity.Comment;
import org.banana.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    @Override
    public void printComments() {
        List<Comment> allRootComments = commentRepository.findAllRootComments();
        List<Comment> allCommentsInRootIds = commentRepository.findAllCommentsInRootIds(allRootComments.stream().map(Comment::getCommentId).toList());
        List<Comment> allComments = new ArrayList<>();
        allComments.addAll(allRootComments);
        allComments.addAll(allCommentsInRootIds);
        System.out.println(allComments);
    }
}
