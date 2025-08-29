package myapp.backend.domain.board.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import myapp.backend.domain.board.vo.BoardCommentVO;

@Mapper
public interface BoardCommentMapper {
    // 댓글 작성
    void insertComment(BoardCommentVO comment);
    
    // 게시글의 댓글 목록 조회 (최상위 댓글만)
    List<BoardCommentVO> getCommentsByBoardId(int board_id);
    
    // 특정 댓글의 대댓글 목록 조회
    List<BoardCommentVO> getRepliesByParentId(int parent_id);
    
    // 댓글 수정
    int updateComment(BoardCommentVO comment);
    
    // 댓글 삭제
    int deleteComment(int comment_id);
    
    // 댓글 작성자 확인
    Integer findCommentAuthorUserId(int comment_id);
    
    // 게시글의 총 댓글 수 조회
    int getCommentCountByBoardId(int board_id);
}
