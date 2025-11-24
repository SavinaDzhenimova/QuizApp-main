package com.quizapp.model.dto.category;

import com.quizapp.model.dto.PageDTO;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPageDTO<T> extends PageDTO {

    private List<T> categories;
}