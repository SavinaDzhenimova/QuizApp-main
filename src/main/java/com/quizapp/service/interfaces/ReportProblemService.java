package com.quizapp.service.interfaces;

import com.quizapp.model.dto.ReportProblemDTO;
import com.quizapp.model.entity.Result;

public interface ReportProblemService {


    Result sendEmailToReportProblem(ReportProblemDTO reportProblemDTO);
}