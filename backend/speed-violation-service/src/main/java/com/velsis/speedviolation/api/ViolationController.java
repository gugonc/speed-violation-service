package com.velsis.speedviolation.api;

import com.velsis.speedviolation.api.dto.EvaluationResponse;
import com.velsis.speedviolation.api.dto.SpeedReadingRequest;
import com.velsis.speedviolation.api.dto.StoredViolationResponse;
import com.velsis.speedviolation.api.exception.ApiError;
import com.velsis.speedviolation.api.exception.ValidationException;
import com.velsis.speedviolation.domain.model.EvaluationResult;
import com.velsis.speedviolation.domain.model.SpeedReadingCommand;
import com.velsis.speedviolation.domain.service.SpeedEvaluationService;
import com.velsis.speedviolation.domain.service.ViolationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/violations")
public class ViolationController {

    private static final Logger log = LoggerFactory.getLogger(ViolationController.class);

    private final SpeedReadingValidator validator;
    private final SpeedEvaluationService evaluationService;
    private final ViolationRepository repository;

    public ViolationController(SpeedReadingValidator validator,
                               SpeedEvaluationService evaluationService,
                               ViolationRepository repository) {
        this.validator = validator;
        this.evaluationService = evaluationService;
        this.repository = repository;
    }

    @PostMapping("/evaluate")
    public EvaluationResponse evaluate(
            @RequestHeader(value = "x-origin", required = false) String origin,
            @RequestBody(required = false) SpeedReadingRequest request) {

        if (request == null) {
            throw new ValidationException(ApiError.MALFORMED_REQUEST, "Request body is required");
        }

        SpeedReadingCommand command = validator.validateAndBuild(request, origin);
        EvaluationResult result = evaluationService.evaluate(command);

        if (result.hasViolation()) {
            repository.save(result.toViolation());
            log.info("Violation recorded: plate={}, equipment={}, origin={}, severity={}, excess={}%",
                    command.licensePlate(), command.equipmentId(), command.origin(),
                    result.severity(), result.excessPercentage());
        } else {
            log.info("No violation: plate={}, equipment={}, origin={}, consideredSpeed={}, limit={}",
                    command.licensePlate(), command.equipmentId(), command.origin(),
                    result.consideredSpeed(), command.speedLimit());
        }

        return EvaluationResponse.from(result);
    }

    @GetMapping
    public List<StoredViolationResponse> findByLicensePlate(@RequestParam String licensePlate) {
        return repository.findByLicensePlate(licensePlate).stream()
                .map(StoredViolationResponse::from)
                .toList();
    }
}
