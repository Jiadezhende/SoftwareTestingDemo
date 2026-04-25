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

import org.junit.jupiter.api.Tag;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("NewsService 单元测试")
class NewsServiceImplTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    // ==================== findAll ====================

    @Test
    @DisplayName("UT-NW-001 - findAll: 传入分页参数查询新闻列表时返回分页结果")
    void testFindAll_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> page = new PageImpl<>(Collections.singletonList(buildNews(1, "馆内通知")));
        when(newsDao.findAll(pageable)).thenReturn(page);

        Page<News> result = newsService.findAll(pageable);

        assertSame(page, result);
        verify(newsDao).findAll(pageable);
    }

    @Test
    @DisplayName("UT-NW-006 - findAll: 传入第一页且 DAO 返回空分页时原样返回空分页对象")
    void testFindAll_emptyPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(newsDao.findAll(pageable)).thenReturn(emptyPage);

        Page<News> result = newsService.findAll(pageable);

        assertSame(emptyPage, result);
        assertEquals(0, result.getTotalElements());
        verify(newsDao).findAll(pageable);
    }

    @Test
    @DisplayName("UT-NW-007 - findAll: DAO 分页查询抛出异常时异常向上透传")
    void testFindAll_daoException() {
        Pageable pageable = PageRequest.of(1, 10);
        RuntimeException exception = new RuntimeException("分页查询失败");
        when(newsDao.findAll(pageable)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.findAll(pageable));

        assertSame(exception, result);
        verify(newsDao).findAll(pageable);
    }

    @Test
    @DisplayName("UT-NW-016 - findAll: pageable=null 时调用 DAO 并异常向上透传")
    void testFindAll_nullPageable() {
        IllegalArgumentException exception = new IllegalArgumentException("pageable 不能为空");
        when(newsDao.findAll((Pageable) null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> newsService.findAll(null));

        assertSame(exception, result);
        verify(newsDao).findAll((Pageable) null);
    }

    // ==================== findById ====================

    @Test
    @DisplayName("UT-NW-002 - findById: 查询存在的新闻 ID 时返回对应 News 对象")
    void testFindById_success() {
        News news = buildNews(2, "比赛公告");
        when(newsDao.getOne(2)).thenReturn(news);

        News result = newsService.findById(2);

        assertSame(news, result);
        verify(newsDao).getOne(2);
    }

    @Test
    @DisplayName("UT-NW-008 - findById: 查询 newsID=0 的新闻时委托 DAO 查询并返回对象")
    void testFindById_zeroId() {
        News news = buildNews(0, "默认公告");
        when(newsDao.getOne(0)).thenReturn(news);

        News result = newsService.findById(0);

        assertSame(news, result);
        verify(newsDao).getOne(0);
    }

    @Test
    @DisplayName("UT-NW-009 - findById: DAO 查询单条新闻抛出异常时异常向上透传")
    void testFindById_daoException() {
        RuntimeException exception = new RuntimeException("查询公告失败");
        when(newsDao.getOne(99)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.findById(99));

        assertSame(exception, result);
        verify(newsDao).getOne(99);
    }

    // ==================== create ====================

    @Test
    @DisplayName("UT-NW-003 - create: 新增合法新闻对象时返回持久化后对象的 newsID")
    void testCreate_success() {
        News news = buildNews(0, "新增公告");
        News savedNews = buildNews(3, "新增公告");
        when(newsDao.save(news)).thenReturn(savedNews);

        int result = newsService.create(news);

        assertEquals(3, result);
        verify(newsDao).save(news);
    }

    @Test
    @DisplayName("UT-NW-010 - create: 新增新闻后持久化对象 newsID=0 时返回 0")
    void testCreate_zeroId() {
        News news = buildNews(0, "边界公告");
        News savedNews = buildNews(0, "边界公告");
        when(newsDao.save(news)).thenReturn(savedNews);

        int result = newsService.create(news);

        assertEquals(0, result);
        verify(newsDao).save(news);
    }

    @Test
    @DisplayName("UT-NW-011 - create: DAO 保存新闻抛出异常时异常向上透传")
    void testCreate_daoException() {
        News news = buildNews(0, "异常公告");
        RuntimeException exception = new RuntimeException("保存公告失败");
        when(newsDao.save(news)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.create(news));

        assertSame(exception, result);
        verify(newsDao).save(news);
    }

    @Test
    @DisplayName("UT-NW-017 - create: [BUG-037] DAO save 返回 null 时服务层无 null 检查，直接透传 NullPointerException")
    void testCreate_nullSavedEntity() {
        News news = buildNews(0, "持久化返回空对象");
        when(newsDao.save(news)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> newsService.create(news));

        verify(newsDao).save(news);
    }

    @Test
    @Disabled("BUG-021: 空 title 可被持久化，待服务层输入校验实现后启用")
    @DisplayName("UT-NW-019 - create: title 为空时应抛出异常")
    void testCreate_emptyTitle() {
        News news = buildNews(0, "");

        assertThrows(Exception.class, () -> newsService.create(news));
        verify(newsDao, never()).save(any(News.class));
    }

    @Test
    @Disabled("BUG-022: 空 content 可被持久化，待服务层输入校验实现后启用")
    @DisplayName("UT-NW-020 - create: content 为空时应抛出异常")
    void testCreate_emptyContent() {
        News news = buildNews(0, "内容为空公告");
        news.setContent("");

        assertThrows(Exception.class, () -> newsService.create(news));
        verify(newsDao, never()).save(any(News.class));
    }

    // ==================== delById ====================

    @Test
    @DisplayName("UT-NW-004 - delById: 删除指定新闻 ID 时委托 DAO 删除对应记录")
    void testDelById_success() {
        newsService.delById(4);

        verify(newsDao).deleteById(4);
    }

    @Test
    @DisplayName("UT-NW-012 - delById: 删除 newsID=0 的新闻时委托 DAO 删除")
    void testDelById_zeroId() {
        newsService.delById(0);

        verify(newsDao).deleteById(0);
    }

    @Test
    @DisplayName("UT-NW-013 - delById: DAO 删除新闻抛出异常时异常向上透传")
    void testDelById_daoException() {
        RuntimeException exception = new RuntimeException("删除公告失败");
        doThrow(exception).when(newsDao).deleteById(9);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.delById(9));

        assertSame(exception, result);
        verify(newsDao).deleteById(9);
    }

    // ==================== update ====================

    @Test
    @DisplayName("UT-NW-005 - update: 更新已有新闻对象时委托 DAO 保存")
    void testUpdate_success() {
        News news = buildNews(5, "公告更新");

        newsService.update(news);

        verify(newsDao).save(news);
    }

    @Test
    @DisplayName("UT-NW-014 - update: 更新 newsID=0 的新闻对象时委托 DAO 保存")
    void testUpdate_zeroId() {
        News news = buildNews(0, "边界更新");

        newsService.update(news);

        verify(newsDao).save(news);
    }

    @Test
    @DisplayName("UT-NW-015 - update: DAO 更新新闻抛出异常时异常向上透传")
    void testUpdate_daoException() {
        News news = buildNews(7, "异常更新");
        RuntimeException exception = new RuntimeException("更新公告失败");
        when(newsDao.save(news)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> newsService.update(news));

        assertSame(exception, result);
        verify(newsDao).save(news);
    }

    @Test
    @DisplayName("UT-NW-018 - update: news=null 时调用 DAO 并异常向上透传")
    void testUpdate_nullNews() {
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
