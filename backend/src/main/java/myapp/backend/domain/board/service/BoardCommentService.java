package myapp.backend.domain.board.service;

import java.util.List;
import myapp.backend.domain.board.vo.BoardCommentVO;

public interface BoardCommentService {
    // 댓글 작성
    void createComment(int board_id, String comment_content, Integer parent_id, int user_id);
    
    // 게시글의 댓글 목록 조회 (최상위 댓글만)
    List<BoardCommentVO> getCommentsByBoardId(int board_id);
    
    // 특정 댓글의 대댓글 목록 조회
    List<BoardCommentVO> getRepliesByParentId(int parent_id);
    
    // 댓글 수정
    void updateComment(int comment_id, String comment_content, int user_id);
    
    // 댓글 삭제
    void deleteComment(int comment_id, int user_id);
    
    // 게시글의 총 댓글 수 조회
    int getCommentCountByBoardId(int board_id);
}
