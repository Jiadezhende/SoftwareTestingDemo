package com.demo.service.impl;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    void testFindAllBoundaryWithEmptyPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(newsDao.findAll(pageable)).thenReturn(emptyPage);

        Page<News> result = newsService.findAll(pageable);

        assertSame(emptyPage, result);
        assertEquals(0, result.getTotalElements());
        verify(newsDao).findAll(pageable);
    }

    @Test
    void testFindAllException() {
        Pageable pageable = PageRequest.of(1, 10);
        RuntimeException exception = new RuntimeException("分页查询失败");
        when(newsDao.findAll(pageable)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.findAll(pageable));

        assertSame(exception, result);
        verify(newsDao).findAll(pageable);
    }

    @Test
    void testFindAllExceptionWithNullPageable() {
        IllegalArgumentException exception = new IllegalArgumentException("pageable 不能为空");
        when(newsDao.findAll((Pageable) null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> newsService.findAll(null));

        assertSame(exception, result);
        verify(newsDao).findAll((Pageable) null);
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
    void testFindByIdBoundaryWithZeroId() {
        News news = buildNews(0, "默认公告");
        when(newsDao.getOne(0)).thenReturn(news);

        News result = newsService.findById(0);

        assertSame(news, result);
        verify(newsDao).getOne(0);
    }

    @Test
    void testFindByIdException() {
        RuntimeException exception = new RuntimeException("查询公告失败");
        when(newsDao.getOne(99)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.findById(99));

        assertSame(exception, result);
        verify(newsDao).getOne(99);
    }

    @Test
    void testCreate() {
        News news = buildNews(0, "新增公告");
        News savedNews = buildNews(3, "新增公告");
        when(newsDao.save(news)).thenReturn(savedNews);

        int result = newsService.create(news);

        assertEquals(3, result);
        verify(newsDao).save(news);
    }

    @Test
    void testCreateBoundaryWithZeroId() {
        News news = buildNews(0, "边界公告");
        News savedNews = buildNews(0, "边界公告");
        when(newsDao.save(news)).thenReturn(savedNews);

        int result = newsService.create(news);

        assertEquals(0, result);
        verify(newsDao).save(news);
    }

    @Test
    void testCreateException() {
        News news = buildNews(0, "异常公告");
        RuntimeException exception = new RuntimeException("保存公告失败");
        when(newsDao.save(news)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.create(news));

        assertSame(exception, result);
        verify(newsDao).save(news);
    }

    @Test
    void testCreateBoundaryWithNullSavedEntity() {
        News news = buildNews(0, "持久化返回空对象");
        when(newsDao.save(news)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> newsService.create(news));

        verify(newsDao).save(news);
    }

    @Test
    @Disabled("未实现：title 为空的新闻应被拒绝")
    @DisplayName("17 - create: title 为空时应抛出异常")
    void testCreate_EmptyTitle_ShouldBeRejected() {
        News news = buildNews(0, "");

        assertThrows(Exception.class, () -> newsService.create(news));
        verify(newsDao, never()).save(any(News.class));
    }

    @Test
    @Disabled("未实现：content 为空的新闻应被拒绝")
    @DisplayName("18 - create: content 为空时应抛出异常")
    void testCreate_EmptyContent_ShouldBeRejected() {
        News news = buildNews(0, "内容为空公告");
        news.setContent("");

        assertThrows(Exception.class, () -> newsService.create(news));
        verify(newsDao, never()).save(any(News.class));
    }

    @Test
    void testDelById() {
        newsService.delById(4);

        verify(newsDao).deleteById(4);
    }

    @Test
    void testDelByIdBoundaryWithZeroId() {
        newsService.delById(0);

        verify(newsDao).deleteById(0);
    }

    @Test
    void testDelByIdException() {
        RuntimeException exception = new RuntimeException("删除公告失败");
        doThrow(exception).when(newsDao).deleteById(9);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.delById(9));

        assertSame(exception, result);
        verify(newsDao).deleteById(9);
    }

    @Test
    void testUpdate() {
        News news = buildNews(5, "公告更新");

        newsService.update(news);

        verify(newsDao).save(news);
    }

    @Test
    void testUpdateBoundaryWithZeroId() {
        News news = buildNews(0, "边界更新");

        newsService.update(news);

        verify(newsDao).save(news);
    }

    @Test
    void testUpdateException() {
        News news = buildNews(7, "异常更新");
        RuntimeException exception = new RuntimeException("更新公告失败");
        when(newsDao.save(news)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.update(news));

        assertSame(exception, result);
        verify(newsDao).save(news);
    }

    @Test
    void testUpdateBoundaryWithNullNews() {
        IllegalArgumentException exception = new IllegalArgumentException("news 不能为空");
        when(newsDao.save(null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> newsService.update(null));

        assertSame(exception, result);
        verify(newsDao).save(null);
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
