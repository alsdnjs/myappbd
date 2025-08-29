package myapp.backend.domain.board.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import myapp.backend.domain.board.service.BoardCommentService;
import myapp.backend.domain.board.vo.BoardCommentVO;
import myapp.backend.domain.auth.vo.UserPrincipal;

@RestController
@RequestMapping("/api/board/comment")
public class BoardCommentController {
    
    @Autowired
    private BoardCommentService boardCommentService;
    
    // 댓글 작성
    @PostMapping("/{board_id}")
    public ResponseEntity<?> createComment(
            @PathVariable int board_id,
            @RequestParam String comment_content,
            @RequestParam(required = false) Integer parent_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        System.out.println("[BoardCommentController] 🗨️ 댓글 작성 요청 - boardId: " + board_id);
        System.out.println("[BoardCommentController] 🗨️ 댓글 내용: " + comment_content);
        System.out.println("[BoardCommentController] 🗨️ 부모 댓글 ID: " + parent_id);
        System.out.println("[BoardCommentController] 🗨️ 인증된 사용자: " + (principal != null ? "userId=" + principal.getUserId() + ", username=" + principal.getUsername() : "null"));
        
        if (principal == null) {
            System.err.println("[BoardCommentController] 🗨️ 댓글 작성 실패 - 인증되지 않은 사용자 ❌");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.createComment(board_id, comment_content, parent_id, principal.getUserId());
            System.out.println("[BoardCommentController] 🗨️ 댓글 작성 성공 ✅");
            return ResponseEntity.ok().body("댓글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            System.err.println("[BoardCommentController] 🗨️ 댓글 작성 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("댓글 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글의 댓글 목록 조회 (최상위 댓글만)
    @GetMapping("/{board_id}")
    public ResponseEntity<List<BoardCommentVO>> getCommentsByBoardId(@PathVariable int board_id) {
        try {
            List<BoardCommentVO> comments = boardCommentService.getCommentsByBoardId(board_id);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 특정 댓글의 대댓글 목록 조회
    @GetMapping("/replies/{parent_id}")
    public ResponseEntity<List<BoardCommentVO>> getRepliesByParentId(@PathVariable int parent_id) {
        try {
            List<BoardCommentVO> replies = boardCommentService.getRepliesByParentId(parent_id);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 댓글 수정
    @PutMapping("/{comment_id}")
    public ResponseEntity<?> updateComment(
            @PathVariable int comment_id,
            @RequestParam String comment_content,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.updateComment(comment_id, comment_content, principal.getUserId());
            return ResponseEntity.ok().body("댓글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 댓글 삭제
    @DeleteMapping("/{comment_id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable int comment_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.deleteComment(comment_id, principal.getUserId());
            return ResponseEntity.ok().body("댓글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글의 총 댓글 수 조회
    @GetMapping("/{board_id}/count")
    public ResponseEntity<Map<String, Integer>> getCommentCountByBoardId(@PathVariable int board_id) {
        try {
            int count = boardCommentService.getCommentCountByBoardId(board_id);
            return ResponseEntity.ok(Map.of("commentCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
