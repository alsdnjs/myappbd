package myapp.backend.domain.inquiry.controller;

import myapp.backend.domain.inquiry.service.InquiryService;
import myapp.backend.domain.inquiry.vo.InquiryVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
public class InquiryController {
    
    @Autowired
    private InquiryService inquiryService;
    
    // 내 문의사항 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<InquiryVO>> getInquiryList(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }
            List<InquiryVO> inquiryList = inquiryService.getInquiryListByUserId(principal.getUserId());
            return ResponseEntity.ok(inquiryList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 내 문의사항 상세 조회
    @GetMapping("/{inquiry_id}")
    public ResponseEntity<InquiryVO> getInquiryDetail(
            @PathVariable("inquiry_id") int inquiry_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }
            InquiryVO inquiry = inquiryService.getInquiryDetailByUserId(inquiry_id, principal.getUserId());
            return ResponseEntity.ok(inquiry);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 문의사항 작성
    @PostMapping("/create")
    public ResponseEntity<?> createInquiry(
            @RequestBody InquiryVO inquiry,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            inquiry.setUser_id(principal.getUserId());
            inquiryService.createInquiry(inquiry);
            return ResponseEntity.ok().body("문의사항이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("문의사항 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 문의사항 수정
    @PutMapping("/{inquiry_id}")
    public ResponseEntity<?> updateInquiry(
            @PathVariable("inquiry_id") int inquiry_id,
            @RequestBody InquiryVO inquiry,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            inquiry.setInquiry_id(inquiry_id);
            inquiry.setUser_id(principal.getUserId());
            inquiryService.updateInquiry(inquiry);
            return ResponseEntity.ok().body("문의사항이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("문의사항 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 문의사항 삭제
    @DeleteMapping("/{inquiry_id}")
    public ResponseEntity<?> deleteInquiry(
            @PathVariable("inquiry_id") int inquiry_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            inquiryService.deleteInquiry(inquiry_id, principal.getUserId());
            return ResponseEntity.ok().body("문의사항이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("문의사항 삭제에 실패했습니다: " + e.getMessage());
        }
    }
}
