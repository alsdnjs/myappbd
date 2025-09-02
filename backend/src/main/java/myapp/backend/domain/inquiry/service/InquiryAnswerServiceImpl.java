package myapp.backend.domain.inquiry.service;

import myapp.backend.domain.inquiry.mapper.InquiryAnswerMapper;
import myapp.backend.domain.inquiry.mapper.InquiryMapper;
import myapp.backend.domain.inquiry.vo.InquiryAnswerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InquiryAnswerServiceImpl implements InquiryAnswerService {
    
    @Autowired
    private InquiryAnswerMapper inquiryAnswerMapper;
    
    @Autowired
    private InquiryMapper inquiryMapper;
    
    @Override
    public InquiryAnswerVO getAnswerByInquiryId(int inquiry_id) {
        return inquiryAnswerMapper.getAnswerByInquiryId(inquiry_id);
    }
    
    @Override
    @Transactional
    public void createAnswer(InquiryAnswerVO answer) {
        try {
            // 기존 답변 확인
            InquiryAnswerVO existingAnswer = inquiryAnswerMapper.getAnswerByInquiryId(answer.getInquiry_id());
            if (existingAnswer != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 답변이 존재합니다.");
            }
            
            inquiryAnswerMapper.insertAnswer(answer);
            
            // 문의사항 상태를 'answered'로 변경
            inquiryMapper.updateInquiryStatus(answer.getInquiry_id(), "answered");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "답변 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void updateAnswer(InquiryAnswerVO answer) {
        try {
            int result = inquiryAnswerMapper.updateAnswer(answer);
            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "답변 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deleteAnswer(int answer_id) {
        try {
            int result = inquiryAnswerMapper.deleteAnswer(answer_id);
            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "답변 삭제에 실패했습니다: " + e.getMessage());
        }
    }
}
