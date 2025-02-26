package com.cantomiletea.chavez.review;

import com.cantomiletea.chavez.review.dto.ReviewArticleDto;
import com.cantomiletea.chavez.review.dto.ReviewMinimalDto;
import com.cantomiletea.chavez.review.dto.ReviewDeleteDto;
import com.cantomiletea.chavez.review.dto.ReviewResponseDto;
import com.cantomiletea.chavez.util.ControllerUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> addReview(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                       @Valid @RequestBody ReviewMinimalDto reviewAddDto,
                                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.error("[ReviewController:addReview] Binding result error");
            return ResponseEntity.badRequest().body(ControllerUtil.getBindingResultErrors(bindingResult));
        }

        reviewService.addReview(authorizationHeader, reviewAddDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> deleteReview(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                          @Valid @RequestBody ReviewDeleteDto reviewDeleteDto,
                                          BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.error("[ReviewController:deleteReview] Binding result error");
            return ResponseEntity.badRequest().body(ControllerUtil.getBindingResultErrors(bindingResult));
        }

        reviewService.deleteReview(authorizationHeader, reviewDeleteDto);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> editReview(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                          @Valid @RequestBody ReviewMinimalDto reviewMinimalDto,
                                          BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.error("[ReviewController:editReview] Binding result error");
            return ResponseEntity.badRequest().body(ControllerUtil.getBindingResultErrors(bindingResult));
        }

        reviewService.editReview(authorizationHeader, reviewMinimalDto);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<ReviewResponseDto>> getUserReviews(@PathVariable String username) {
        return ResponseEntity.ok(reviewService.getUserReviews(username));
    }
}
