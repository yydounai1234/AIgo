package com.aigo.service;

import com.aigo.dto.ErrorCode;
import com.aigo.dto.comment.CommentResponse;
import com.aigo.dto.comment.CreateCommentRequest;
import com.aigo.entity.Comment;
import com.aigo.entity.User;
import com.aigo.exception.BusinessException;
import com.aigo.repository.CommentRepository;
import com.aigo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Comment testComment;
    private CreateCommentRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user1")
                .username("testUser")
                .email("test@example.com")
                .avatarUrl("avatar.jpg")
                .build();

        testComment = Comment.builder()
                .id("comment1")
                .targetType("work")
                .targetId("work1")
                .userId("user1")
                .content("测试评论")
                .createdAt(LocalDateTime.now())
                .build();
        testComment.setUser(testUser);

        createRequest = CreateCommentRequest.builder()
                .targetType("work")
                .targetId("work1")
                .content("测试评论")
                .build();
    }

    @Test
    void testGetComments_Success() {
        when(commentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("work", "work1"))
                .thenReturn(Arrays.asList(testComment));

        List<CommentResponse> comments = commentService.getComments("work", "work1");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("comment1", comments.get(0).getId());
        assertEquals("测试评论", comments.get(0).getContent());
        assertEquals("testUser", comments.get(0).getUsername());
        verify(commentRepository).findByTargetTypeAndTargetIdOrderByCreatedAtDesc("work", "work1");
    }

    @Test
    void testGetComments_EmptyList() {
        when(commentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("work", "work1"))
                .thenReturn(Arrays.asList());

        List<CommentResponse> comments = commentService.getComments("work", "work1");

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    void testCreateComment_Success() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentResponse response = commentService.createComment(createRequest, "user1");

        assertNotNull(response);
        assertEquals("comment1", response.getId());
        assertEquals("测试评论", response.getContent());
        assertEquals("testUser", response.getUsername());
        verify(userRepository).findById("user1");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testCreateComment_UserNotFound() {
        when(userRepository.findById("user1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> commentService.createComment(createRequest, "user1"));

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findById("user1");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testDeleteComment_Success() {
        when(commentRepository.findById("comment1")).thenReturn(Optional.of(testComment));

        commentService.deleteComment("comment1", "user1");

        verify(commentRepository).findById("comment1");
        verify(commentRepository).delete(testComment);
    }

    @Test
    void testDeleteComment_NotFound() {
        when(commentRepository.findById("comment1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> commentService.deleteComment("comment1", "user1"));

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("评论不存在", exception.getMessage());
        verify(commentRepository).findById("comment1");
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void testDeleteComment_Forbidden() {
        when(commentRepository.findById("comment1")).thenReturn(Optional.of(testComment));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> commentService.deleteComment("comment1", "user2"));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        assertEquals("无权限删除此评论", exception.getMessage());
        verify(commentRepository).findById("comment1");
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
