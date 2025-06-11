package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.LogAdmin;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

    private final CommentRepository commentRepository;

    @LogAdmin(comment = "Admin delete comment")
    @Transactional
    public void deleteComment(long commentId) {
        commentRepository.deleteById(commentId);
    }
}
