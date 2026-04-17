package com.demo.service.impl;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> page = new PageImpl<>(Collections.singletonList(buildNews(1, "馆内通知")));
        when(newsDao.findAll(pageable)).thenReturn(page);

        Page<News> result = newsService.findAll(pageable);

        assertSame(page, result);
        verify(newsDao).findAll(pageable);
    }

    @Test
    void testFindById() {
        News news = buildNews(2, "比赛公告");
        when(newsDao.getOne(2)).thenReturn(news);

        News result = newsService.findById(2);

        assertSame(news, result);
        verify(newsDao).getOne(2);
    }

    @Test
    void testCreate() {
        News news = buildNews(3, "新增公告");
        when(newsDao.save(news)).thenReturn(news);

        int result = newsService.create(news);

        assertEquals(3, result);
        verify(newsDao).save(news);
    }

    @Test
    void testDelById() {
        newsService.delById(4);

        verify(newsDao).deleteById(4);
    }

    @Test
    void testUpdate() {
        News news = buildNews(5, "公告更新");

        newsService.update(news);

        verify(newsDao).save(news);
    }

    private News buildNews(int newsId, String title) {
        News news = new News();
        news.setNewsID(newsId);
        news.setTitle(title);
        news.setContent(title + "内容");
        news.setTime(LocalDateTime.of(2026, 4, 17, 10, 0));
        return news;
    }
}
