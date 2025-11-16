package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.PerformanceReview;
import com.stanbic.internMs.intern.repository.PerformanceReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;

    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository){
        this.performanceReviewRepository=performanceReviewRepository;
    }

    public List<PerformanceReview> listPerformanceReviews(){return performanceReviewRepository.findAll();}

    public Page<PerformanceReview> listPerformanceReviews(Pageable pageable){return performanceReviewRepository.findAll(pageable);}

//    public Page<PerformanceReview> listPerformanceReviews(Pageable pageable, String search){
//        if(search==null || search.isBlank()){
//            return performanceReviewRepository.findAll(pageable);
//        }
//        return performanceReviewRepository.findByNameContainingIgnoreCase(search, pageable);
//    }

    public Optional<PerformanceReview> findById(Long id){return performanceReviewRepository.findById(id);}

    public PerformanceReview update(Long id, PerformanceReview performanceReview){
        //                    existing.setStartDate();
        return performanceReviewRepository.findById(id)
                .map(performanceReviewRepository::save)
                .orElseThrow(()-> new RuntimeException("Performance Review not found with id: " +id));
    }

    public PerformanceReview createPerformanceReview(PerformanceReview performanceReview){
//        performanceReview.setCreatedAt();
        return performanceReviewRepository.save(performanceReview);

    }

    public PerformanceReview save(PerformanceReview performanceReview){
        return performanceReviewRepository.save(performanceReview);
    }

    public void delete(Long id){performanceReviewRepository.deleteById(id);}
}
