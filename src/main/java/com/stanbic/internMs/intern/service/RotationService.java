package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.PerformanceReview;
import com.stanbic.internMs.intern.models.Rotation;
import com.stanbic.internMs.intern.repository.RotationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RotationService {
    private final RotationRepository rotationRepository;

    public RotationService(RotationRepository rotationRepository){
        this.rotationRepository=rotationRepository;
    }

    public List<Rotation> listRotations(){return rotationRepository.findAll();}

    public Page<Rotation> listRotations(Pageable pageable){return rotationRepository.findAll(pageable);}

    public Page<Rotation> listRotations(Pageable pageable, String search){
        if(search==null || search.isBlank()){
            return rotationRepository.findAll(pageable);
        }
        return rotationRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    public Optional<Rotation>findById(Long id){return rotationRepository.findById(id);}



    //name, startDate,endDate,
    public Rotation update(Long id, Rotation rotation){
        return rotationRepository.findById(id)
                .map(existing->{
                    existing.setName(rotation.getName());
                    existing.setStartDate(rotation.getStartDate());
                    existing.setEndDate(rotation.getEndDate());
                    return rotationRepository.save(existing);
                })
                .orElse(null);
    }

    public void delete(Long id){rotationRepository.deleteById(id);}

    public Rotation createRotation(Rotation rotation){
//        rotation.setStartDate();
        return rotationRepository.save(rotation);
    }

    public Rotation save(Rotation rotation){return rotationRepository.save(rotation);}

}


