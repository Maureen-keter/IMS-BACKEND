package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.repository.*;
import com.stanbic.internMs.intern.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final CohortRepository cohortRepository;
    private final RotationRepository rotationRepository;
    private final DepartmentRepository departmentRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository, CohortRepository cohortRepository, PerformanceReviewRepository performanceReviewRepository,
            RotationRepository rotationRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.cohortRepository = cohortRepository;
        this.departmentRepository = departmentRepository;
        this.performanceReviewRepository = performanceReviewRepository;
        this.rotationRepository = rotationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Map<String, String>> login(String userID, String password) {
        log.info("UserID: " + userID + "password: " + password);
        return userRepository.findByUserID(userID)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {

                            String jwt = jwtUtil.generateToken(user.getUserID());
                            return ResponseEntity.ok(Map.of(
                                    "access_token", jwt,
                                    "token_type", "Bearer"
                            ));
                        }
                )
                .orElseThrow(() -> new ValidationException(Map.of("id", "Invalid credentials.")));
    }
}










package com.stanbic.onboarding.admin.controller.staff;

        import com.stanbic.onboarding.admin.dto.DtoMapper;
        import com.stanbic.onboarding.admin.dto.GenericDTO;
        import com.stanbic.onboarding.admin.exception.ValidationException;
        import com.stanbic.onboarding.admin.model.ApplicationType;
        import com.stanbic.onboarding.admin.model.CompanyApplication;
        import com.stanbic.onboarding.admin.model.Customer;
        import com.stanbic.onboarding.admin.repository.ApplicationTypeRepository;
        import com.stanbic.onboarding.admin.repository.CustomerRepository;
        import com.stanbic.onboarding.admin.service.ApplicationService;
        import com.stanbic.onboarding.admin.utils.JwtUtil;
        import com.stanbic.onboarding.admin.utils.ValidationUtil;
        import org.springframework.data.domain.Pageable;
        import org.springframework.data.domain.Sort.Direction;
        import org.springframework.data.web.PageableDefault;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.access.prepost.PreAuthorize;
        import org.springframework.validation.annotation.Validated;
        import org.springframework.web.bind.annotation.*;

        import com.stanbic.onboarding.admin.dto.StandardAPIResponse;
        import com.stanbic.onboarding.admin.service.CustomerService;

        import lombok.extern.slf4j.Slf4j;

        import java.time.LocalDateTime;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Map;
        import java.util.Optional;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/customers")
public class CustomersController {

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final ApplicationService applicationService;

    public CustomersController(CustomerService customerService, JwtUtil jwtUtil, CustomerRepository customerRepository,
                               ApplicationTypeRepository applicationTypeRepository, ApplicationService applicationService) {

        this.customerService = customerService;
        this.jwtUtil=jwtUtil;
        this.customerRepository=customerRepository;
        this.applicationTypeRepository=applicationTypeRepository;
        this.applicationService=applicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_CUSTOMERS')")
    public ResponseEntity<StandardAPIResponse> getAllCompanies(@PageableDefault(size=20, sort="dateCreated", direction= Sort.Direction.DESC) Pageable pageable) {
        try {
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(customerService.listCustomers(pageable))
                            .message("Successfully fetched customers")
                            .successful(true)
                            .build()
            );
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(exception.getMessage())
                            .successful(false)
                            .message("Something went wrong while attempting to fetch customers")
                            .build()
            );
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('UPDATE_CUSTOMERS')")
    public ResponseEntity<StandardAPIResponse> updateCustomerStatus(
            @PathVariable Long id,
            @RequestParam String action) {

        try {
            String cleanAction = action.toUpperCase();

            if (!cleanAction.equals("BLOCK") && !cleanAction.equals("UNBLOCK")) {
                throw new ValidationException(Map.of("errors", "Cannot understand command"));
            }

            Customer customer = cleanAction.equals("BLOCK")
                    ? customerService.blockCustomer(id)
                    : customerService.unblockCustomer(id);

            String message = cleanAction.equals("BLOCK")
                    ? "Customer blocked successfully"
                    : "Customer unblocked successfully";

            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(customer)
                            .message(message)
                            .successful(true)
                            .build()

            );

        } catch (ValidationException ve) {
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("errors"))
                            .successful(false)
                            .message("The provided input is wrong. Confirm that the selected customer and action are correct")
                            .build()
            );

        } catch (Exception exception) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(exception.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('CREATE_APPLICATION')")
    public ResponseEntity<StandardAPIResponse> createApplication(
            @RequestBody GenericDTO dto,
            @RequestHeader(name="Authorization") String authToken) {

        try {
            String username = jwtUtil.getUsernameFromToken(authToken.substring(7));
            Optional<Customer> customer = customerRepository.findByEmailAddress(username.trim());

            if (!customer.isPresent()) {
                throw new ValidationException(Map.of("id", "The user could not be found"));
            }

            List<Integer> representatives = (ArrayList<Integer>)dto.get("representatives");
            List<Integer> signatories = (ArrayList<Integer>)dto.get("signatories");
            List<Integer> settlementAccounts = (ArrayList<Integer>)dto.get("settlementAccounts");

            String representativesAsString = representatives.toString();
            String signatoriesAsString = signatories.toString();
            String settlementAccountsAsString = settlementAccounts.toString();

            List<ApplicationType> applicationTypes = new ArrayList<>();

            for (Integer applicationTypeId: (ArrayList<Integer>)dto.get("applicationTypes")) {
                Optional<ApplicationType> applicationType = applicationTypeRepository.findById(Long.valueOf(applicationTypeId));

                if (!applicationType.isPresent()) {
                    throw new ValidationException(Map.of("error", "Some of the application types could not be found"));
                }

                applicationTypes.add(applicationType.get());
            }

            Map<String, Object> fields = dto.getFields();

            for (String field: fields.keySet()) {
                log.info(field + ": " + dto.get(field).toString());
            }

            dto.set("submittedBy", customer.get());
            dto.set("submittedAt", LocalDateTime.now());
            dto.set("status", CompanyApplication.ApprovalStatus.SUBMITTED);
            dto.set("representatives", representativesAsString);
            dto.set("signatories", signatoriesAsString);
            dto.set("settlementAccounts", settlementAccountsAsString);

            ValidationUtil.validateRequiredFields(dto, CompanyApplication.class);

            CompanyApplication companyApplication = DtoMapper.mapToEntity(dto, CompanyApplication.class);
            companyApplication.setApplicationTypes(applicationTypes);
            CompanyApplication createdCompanyApplication = applicationService.createApplication(companyApplication);

            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(createdCompanyApplication)
                            .message("Successfully created application")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve) {
            return ResponseEntity.status(400).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors())
                            .successful(false)
                            .message("The input is incorrect. Try again")
                            .build()
            );

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(exception.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

}






















