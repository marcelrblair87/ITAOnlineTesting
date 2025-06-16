package jm.gov.mtw.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssessmentSubmissionDto {
    private List<AnswerDto> answers;
}
