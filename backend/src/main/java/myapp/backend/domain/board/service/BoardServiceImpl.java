package myapp.backend.domain.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import myapp.backend.domain.board.mapper.BoardMapper;
import myapp.backend.domain.board.vo.BoardVO;

@Service
public class BoardServiceImpl implements BoardService {
    @Autowired
    private BoardMapper boardMapper;
    
    @Override
    public List<BoardVO> getBoardList() {
        return boardMapper.getBoardList();
    }
    
    @Override
    public void createBoard(BoardVO board) {
        boardMapper.insertBoard(board); // 게시물 작성
    }
    
    @Override
    public BoardVO getBoard(int board_id) {
        // 조회수 증가
        increaseViewCount(board_id);
        // 게시글 조회
        return boardMapper.getBoardDetailById(board_id); // 개별 글 조회
    }
    
    @Override
    public void increaseViewCount(int board_id) {
        boardMapper.updateViewCount(board_id);
    }
    
    @Override
    public BoardVO getBoardDetail(int board_id, Integer currentUserId) {
        // 조회수 증가
        increaseViewCount(board_id);
        
        // 상세 정보 조회
        BoardVO boardDetail = boardMapper.getBoardDetailById(board_id);
        
        return boardDetail;
    }

    @Override
    public void deleteBoard(int boardId, int requestUserId) {
    Integer authorUserId = boardMapper.findAuthorUserId(boardId);
    if (authorUserId == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
    }
    if (!authorUserId.equals(requestUserId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다.");
    }
    int deleted = boardMapper.deleteBoard(boardId);
    if (deleted == 0) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제 대상이 존재하지 않습니다.");
    }
}

    @Override
    public void updateBoard(int boardId, BoardVO updatedBoard, int requestUserId) {
        Integer authorUserId = boardMapper.findAuthorUserId(boardId);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        if (!authorUserId.equals(requestUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }

        // 업데이트 대상 보드 ID를 확실히 설정
        updatedBoard.setBoard_id(boardId);
        int updated = boardMapper.updateBoard(updatedBoard);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정 대상이 존재하지 않습니다.");
        }
    }
}
