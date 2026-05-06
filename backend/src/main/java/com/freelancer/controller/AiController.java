package com.freelancer.controller;

import com.freelancer.dto.request.AiCoverLetterRequest;
import com.freelancer.dto.request.AiDescriptionRequest;
import com.freelancer.dto.response.AiSuggestResponse;
import com.freelancer.dto.response.ApiResponse;
import com.freelancer.service.impl.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiService geminiService;

    @PostMapping("/suggest-description")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<AiSuggestResponse>> suggestDescription(
            @RequestBody AiDescriptionRequest req) {
        String prompt = """
                Bạn là chuyên gia viết mô tả công việc freelance chuyên nghiệp bằng tiếng Việt.
                Hãy viết mô tả chi tiết cho công việc sau:
                - Tiêu đề: %s
                - Danh mục: %s

                Yêu cầu:
                - Viết bằng tiếng Việt, rõ ràng và chuyên nghiệp
                - Khoảng 200-300 từ
                - Chia thành 3 phần: Tổng quan dự án, Yêu cầu kỹ năng, Kỳ vọng đầu ra
                - Dùng dấu xuống dòng để phân cách các phần, không dùng markdown hay ký tự đặc biệt
                - Chỉ trả về nội dung mô tả, không có tiêu đề hay giải thích thêm
                """.formatted(req.getTitle(), req.getCategory() != null ? req.getCategory() : "Chưa phân loại");

        String suggestion = geminiService.generateText(prompt);
        return ResponseEntity.ok(ApiResponse.success(new AiSuggestResponse(suggestion)));
    }

    @PostMapping("/suggest-cover-letter")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<AiSuggestResponse>> suggestCoverLetter(
            @RequestBody AiCoverLetterRequest req) {
        String prompt = """
                Bạn là chuyên gia viết thư ứng tuyển freelance bằng tiếng Việt.
                Hãy viết thư giới thiệu (cover letter) để ứng tuyển cho công việc sau:
                - Tiêu đề job: %s
                - Mô tả job: %s
                - Ngân sách: %s - %s VNĐ

                Yêu cầu:
                - Viết bằng tiếng Việt, thân thiện và chuyên nghiệp
                - Khoảng 150-200 từ, tối đa 1500 ký tự
                - Bao gồm: tự giới thiệu ngắn, kinh nghiệm liên quan, cách bạn sẽ tiếp cận dự án, cam kết hoàn thành đúng hạn
                - Không dùng markdown, chỉ plain text với dấu xuống dòng
                - Viết ở ngôi thứ nhất (tôi), tự nhiên như người thật viết
                - Chỉ trả về nội dung cover letter, không giải thích thêm
                """.formatted(
                req.getJobTitle(),
                req.getJobDescription() != null ? req.getJobDescription().substring(0, Math.min(500, req.getJobDescription().length())) : "",
                req.getBudgetMin() != null ? req.getBudgetMin() : 0,
                req.getBudgetMax() != null ? req.getBudgetMax() : 0
        );

        String suggestion = geminiService.generateText(prompt);
        return ResponseEntity.ok(ApiResponse.success(new AiSuggestResponse(suggestion)));
    }
}
