package myapp.backend.domain.board.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import myapp.backend.domain.board.mapper.BoardCommentMapper;
import myapp.backend.domain.board.vo.BoardCommentVO;

@Service
public class BoardCommentServiceImpl implements BoardCommentService {
    
    @Autowired
    private BoardCommentMapper boardCommentMapper;
    
    @Override
    public void createComment(int board_id, String comment_content, Integer parent_id, int user_id) {
        // 댓글 내용 검증
        if (comment_content == null || comment_content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        // parent_id가 있는 경우 (대댓글), 해당 댓글이 존재하는지 확인
        if (parent_id != null) {
            BoardCommentVO parentComment = boardCommentMapper.getCommentsByBoardId(board_id).stream()
                .filter(comment -> comment.getComment_id() == parent_id)
                .findFirst()
                .orElse(null);
            
            if (parentComment == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다.");
            }
        }
        
        // 댓글 생성
        BoardCommentVO comment = new BoardCommentVO(user_id, board_id, comment_content, parent_id);
        boardCommentMapper.insertComment(comment);
        
        System.out.println("[BoardCommentServiceImpl] 댓글 작성 완료 - boardId: " + board_id + ", userId: " + user_id);
    }
    
    @Override
    public List<BoardCommentVO> getCommentsByBoardId(int board_id) {
        return boardCommentMapper.getCommentsByBoardId(board_id);
    }
    
    @Override
    public List<BoardCommentVO> getRepliesByParentId(int parent_id) {
        return boardCommentMapper.getRepliesByParentId(parent_id);
    }
    
    @Override
    public void updateComment(int comment_id, String comment_content, int user_id) {
        // 댓글 내용 검증
        if (comment_content == null || comment_content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        // 댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findCommentAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 수정할 수 있습니다.");
        }
        
        // 댓글 수정
        BoardCommentVO comment = new BoardCommentVO();
        comment.setComment_id(comment_id);
        comment.setComment_content(comment_content);
        comment.setUser_id(user_id);
        
        int updated = boardCommentMapper.updateComment(comment);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 수정에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 댓글 수정 완료 - commentId: " + comment_id);
    }
    
    @Override
    public void deleteComment(int comment_id, int user_id) {
        // 댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findCommentAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다.");
        }
        
        // 댓글 삭제
        int deleted = boardCommentMapper.deleteComment(comment_id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 삭제에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 댓글 삭제 완료 - commentId: " + comment_id);
    }
    
    @Override
    public int getCommentCountByBoardId(int board_id) {
        return boardCommentMapper.getCommentCountByBoardId(board_id);
    }
}
