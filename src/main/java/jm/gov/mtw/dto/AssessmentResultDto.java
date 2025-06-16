package jm.gov.mtw.dto;

import jm.gov.mtw.models.Assessment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssessmentResultDto {
    private boolean isCompleted;
    private int score;
    private LocalDate rescheduledDate;
    private String status;
    private String reference;
    private String trn;

    public static AssessmentResultDto from(Assessment result) {
        AssessmentResultDto dto = new AssessmentResultDto();
        dto.setCompleted(result.isCompleted());
        dto.setScore(result.getScore());
        dto.setTrn(result.getUser().getTrn());
        dto.setStatus(result.getStatus().name());
        dto.setReference(result.getReferenceNumber());
        return dto;
    }
}
