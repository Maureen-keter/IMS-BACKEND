package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.Cohort;
import com.stanbic.internMs.intern.repository.CohortRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CohortService {
    private final CohortRepository cohortRepository;

    public CohortService(CohortRepository cohortRepository){
        this.cohortRepository=cohortRepository;
    }

    public List<Cohort> listCohorts(){return cohortRepository.findAll();}

    public Page<Cohort> listCohorts(Pageable pageable){return cohortRepository.findAll(pageable);}

    public Page<Cohort> listCohorts(Pageable pageable, String search){
        if(search==null || search.isBlank()){
            return cohortRepository.findAll(pageable);
        }
        return cohortRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    public Optional<Cohort> findById(Long id){return cohortRepository.findById(id);}

    //name, startdate, endDate, createdAt, isActive
    public Cohort update(Long id, Cohort cohort){
        return cohortRepository.findById(id)
                .map(existing->{
                    existing.setName(cohort.getName());
                    existing.setIsActive(cohort.getIsActive());
//                    existing.setEndDate(cohort.getEndDate());
//                    existing.setStartDate(new LocalDate());
                    return cohortRepository.save(existing);
                })
                .orElse(null);
    }
    public Cohort save(Cohort cohort){return cohortRepository.save(cohort);}

    public void delete(Long id){cohortRepository.deleteById(id);}

    public Cohort createCohort(Cohort cohort){
//        cohort.setCreatedAt();
        return cohortRepository.save(cohort);
    }

}

