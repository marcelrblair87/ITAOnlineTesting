package jm.gov.mtw.dto;

import lombok.Data;

@Data
public class AnswerDto {
    private Long questionId;
    private String selectedAnswer;

    public AnswerDto(){}
    public AnswerDto(Long questionId, String selectedAnswer){
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
    }
}
