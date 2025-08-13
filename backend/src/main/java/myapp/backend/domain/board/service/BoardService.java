package myapp.backend.domain.board.service;

import java.util.List;

import myapp.backend.domain.board.vo.BoardVO;

public interface BoardService {
    List<BoardVO> getBoardList();
    void createBoard(BoardVO board); // 게시물 작성
    BoardVO getBoard(int board_id); // 개별 글 조회
    void increaseViewCount(int board_id); // 조회수 증가
    BoardVO getBoardDetail(int board_id, Integer currentUserId); // 상세페이지 조회
    void deleteBoard(int board_id, int userId); // 게시물 삭제
    void updateBoard(int board_id, BoardVO updatedBoard, int userId); // 게시물 수정
}