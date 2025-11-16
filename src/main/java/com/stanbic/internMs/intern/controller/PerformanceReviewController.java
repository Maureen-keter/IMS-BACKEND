package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.PerformanceReview;
import com.stanbic.internMs.intern.service.PerformanceReviewService;
import com.stanbic.internMs.intern.utils.PagedResponse;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;

    public PerformanceReviewController(PerformanceReviewService performanceReviewService){
        this.performanceReviewService=performanceReviewService;
    }

    @GetMapping("/list")
    public ResponseEntity<StandardAPIResponse> all(){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReviewService.listPerformanceReviews())
                            .message("Performance Review records retrieved successfully")
                            .successful(true)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }

//        Pagination + sorting endpoint

    }

    @GetMapping("/listPaginated")
    public ResponseEntity<StandardAPIResponse> getAll(
            @PageableDefault(size=5, sort = "id") Pageable pageable){
        try{
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReviewService.listPerformanceReviews(pageable))
                            .message("reviews fetched successfully")
                            .successful(true)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }

    }

    @GetMapping("/listSorted")
    public ResponseEntity<StandardAPIResponse> listApplications(
            @PageableDefault(size=5, sort="id") Pageable pageable){
        try {
            Page<PerformanceReview> page= performanceReviewService.listPerformanceReviews(pageable);
            PagedResponse<PerformanceReview> performanceReviews = new PagedResponse<>(
                    page.getContent(),
                    page.getNumber()+1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReviews)
                            .message("Reviews retrieved successfully")
                            .successful(true)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<StandardAPIResponse> getReview(
            @PathVariable("id")@Min(value = 1, message = "Review ID must be positive") Long id) {
        try {
            Optional<PerformanceReview> performanceReview = performanceReviewService.findById(id);
            if (!performanceReview.isPresent()) {
                throw new ValidationException(Map.of("id", "Performance Review not found with id " + id));
            }
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(performanceReview.get())
                            .message("Performance Review record retrieved successfully")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve) {
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getMessage())
                            .message("The performance Review record was not found. Try again with another id")
                            .successful(false)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .message("Something went wrong")
                            .successful(false)
                            .build()
            );
        }
    }



}
