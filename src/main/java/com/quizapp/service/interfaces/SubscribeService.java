package com.quizapp.service.interfaces;

import com.quizapp.model.dto.SubscribeDTO;
import com.quizapp.model.entity.Result;

public interface SubscribeService {
    Result subscribe(SubscribeDTO subscribeDTO);
}
