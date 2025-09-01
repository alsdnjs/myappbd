package myapp.backend.domain.admin.admin.controller;

import myapp.backend.domain.admin.admin.service.AdminService;
import myapp.backend.domain.admin.admin.vo.AdminVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/info")
    public ResponseEntity<AdminVO> getAdminInfo(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        int userId = principal.getUserId();
        AdminVO info = adminService.getAdminInfo(userId);
        if (info == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAdminRole(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.ok(false);
        }
        boolean isAdmin = adminService.isAdmin(principal.getUserId());
        return ResponseEntity.ok(isAdmin);
    }
}


