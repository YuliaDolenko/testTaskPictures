package com.test.taskAgileEngine.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.test.taskAgileEngine.dto.PictureDto;
import com.test.taskAgileEngine.service.PictureService;
import com.test.taskAgileEngine.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.test.taskAgileEngine.constants.Constants.BEARER;
import static com.test.taskAgileEngine.constants.Constants.PAGE;
import static com.test.taskAgileEngine.constants.Constants.PICTURES_ENDPOINT;

@Service
public class PictureServiceImpl implements PictureService {

    private final RestTemplate restTemplate;

    private final TokenService tokenService;

    private final CacheManager cacheManager;

    public int currentPageCount = 0;

    @Autowired
    public PictureServiceImpl(RestTemplate restTemplate, TokenService tokenService, CacheManager cacheManager) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 6000)
    public void evictAllCachesAtIntervals() {
        evictAllCaches();
    }


    @Override
    @Cacheable("picture")
    public List<PictureDto> getPicturesByPage(Integer pageNum) {
        JsonArray jsonArrayPictures = getJsonObject(pageNum).getAsJsonArray("pictures");
        currentPageCount = jsonArrayPictures.size() - 1;

        Type collectionType = new TypeToken<List<PictureDto>>() {}.getType();

        List<PictureDto> pictureDtos = new Gson().fromJson(jsonArrayPictures, collectionType);

        pictureDtos.forEach(picture -> {
            PictureDto fullInformationForPictures = getFullInformationForPictures(picture.getId());
            picture.setAuthor(fullInformationForPictures.getAuthor());
            picture.setCamera(fullInformationForPictures.getCamera());
            picture.setCroppedPicture(fullInformationForPictures.getCroppedPicture());
            picture.setFullPicture(fullInformationForPictures.getFullPicture());
        });

        return pictureDtos;
    }

    @Override
    @Cacheable("picture")
    public List<PictureDto> getPictureByTerm(String term) {
        List<PictureDto> allPictures = getAllPictures();
        List<PictureDto> allPicturesByTerm = new ArrayList<>();

        for (int i = 0; i <= allPictures.size() - 1; i++) {
            if (allPictures.get(i).getAuthor() != null && allPictures.get(i).getAuthor().equals(term)
                    || allPictures.get(i).getCamera() != null && allPictures.get(i).getCamera().equals(term)
                    || allPictures.get(i).getFullPicture() != null && allPictures.get(i).getFullPicture().equals(term)
                    || allPictures.get(i).getCroppedPicture() != null && allPictures.get(i).getCroppedPicture().equals(term)) {

                allPicturesByTerm.add(allPictures.get(i));
            }
        }
        return allPicturesByTerm;
    }

    @Cacheable("picture")
    public List<PictureDto> getAllPictures() {
        List<PictureDto> picturesssDto = new ArrayList<>();

        JsonPrimitive jsonArrayPageCount = getJsonObject(0).getAsJsonPrimitive("pageCount");
        int pageCount = jsonArrayPageCount.getAsInt();

        while (pageCount != 0) {
            JsonArray jsonArrayPictures = getJsonObject(pageCount).getAsJsonArray("pictures");
            Type collectionType = new TypeToken<List<PictureDto>>() {
            }.getType();

            List<PictureDto> pictureDtos = new Gson().fromJson(jsonArrayPictures, collectionType);

            pictureDtos.forEach(picture -> {
                PictureDto fullInformationForPictures = getFullInformationForPictures(picture.getId());
                picture.setAuthor(fullInformationForPictures.getAuthor());
                picture.setCamera(fullInformationForPictures.getCamera());
                picture.setCroppedPicture(fullInformationForPictures.getCroppedPicture());
                picture.setFullPicture(fullInformationForPictures.getFullPicture());
            });

            picturesssDto.addAll(pictureDtos);

            pageCount--;
        }

        return picturesssDto;
    }

    public HttpHeaders getHeader() {
        return new HttpHeaders() {{
            setContentType(MediaType.APPLICATION_JSON);
            set(AUTHORIZATION, BEARER + tokenService.getToken());
            set("Cache-Control", "max-age=60, must-revalidate, no-transform");
        }};
    }

    public JsonObject getJsonObject(Integer pageNum) {
        Map<String, Integer> params = new HashMap<String, Integer>() {{
            put(PAGE, pageNum);
        }};

        HttpEntity<String> entity = new HttpEntity<>(getHeader());
        String responseEntity = restTemplate.exchange(PICTURES_ENDPOINT + "?page=" + pageNum, HttpMethod.GET, entity, String.class, params).getBody();

        return new Gson().fromJson(responseEntity, JsonObject.class);
    }

    public PictureDto getFullInformationForPictures(String id) {
        HttpEntity request = new HttpEntity<>(getHeader());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(PICTURES_ENDPOINT + "/" + id);

        return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, new ParameterizedTypeReference<PictureDto>() {
        }).getBody();
    }

    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .parallelStream()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }
}
