package com.quizapp.service.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ReportProblemEvent extends ApplicationEvent {

    private String fullName;

    private String email;

    private String problemType;

    private String questionIdentifier;

    private String description;

    public ReportProblemEvent(Object source, String fullName, String email, String problemType, String questionIdentifier,
                            String description) {
        super(source);
        this.fullName = fullName;
        this.email = email;
        this.problemType = problemType;
        this.questionIdentifier = questionIdentifier;
        this.description = description;
    }
}