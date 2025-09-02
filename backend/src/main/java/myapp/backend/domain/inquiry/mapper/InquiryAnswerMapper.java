package myapp.backend.domain.inquiry.mapper;

import myapp.backend.domain.inquiry.vo.InquiryAnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InquiryAnswerMapper {
    // 답변 조회
    InquiryAnswerVO getAnswerByInquiryId(@Param("inquiry_id") int inquiry_id);
    
    // 답변 작성
    void insertAnswer(InquiryAnswerVO answer);
    
    // 답변 수정
    int updateAnswer(InquiryAnswerVO answer);
    
    // 답변 삭제
    int deleteAnswer(@Param("answer_id") int answer_id);
}
