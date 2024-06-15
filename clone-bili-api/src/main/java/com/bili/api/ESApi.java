package com.bili.api;

import Service.ElasticSearchService;
import domain.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ESApi {

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @GetMapping("/contents")
    public JsonResponse<List<Map<String, Object>>> getContents(@RequestParam String keyword,
                                                               @RequestParam Integer pageNo,
                                                               @RequestParam Integer size) throws IOException {
        List<Map<String, Object>> list = elasticSearchService.getContents(keyword, pageNo, size);
        return new JsonResponse<>(list);
    }
}
