package com.test.taskAgileEngine.service;

import com.test.taskAgileEngine.dto.PictureDto;

import java.util.List;

public interface PictureService {

    Object getPicturesByPage(Integer page);

    List<PictureDto> getPictureByTerm(String term);
}
