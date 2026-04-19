package com.ruoyi.mental.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.mental.domain.entity.EmotionResult;
import com.ruoyi.mental.domain.entity.MentalChatMessage;
import com.ruoyi.mental.domain.entity.MentalChatSession;
import com.ruoyi.mental.domain.entity.MentalEmotionDiary;
import com.ruoyi.mental.domain.req.EmotionDiaryReq;
import com.ruoyi.mental.domain.req.SessionCreateReq;
import com.ruoyi.mental.domain.req.StreamChatReq;
import com.ruoyi.mental.domain.req.AdminArticleReq;
import com.ruoyi.mental.domain.req.AdminArticleStatusReq;
import com.ruoyi.mental.domain.req.UserLoginReq;
import com.ruoyi.mental.domain.req.UserRegisterReq;
import com.ruoyi.mental.mapper.MentalChatMapper;
import com.ruoyi.mental.mapper.MentalDiaryMapper;
import com.ruoyi.mental.mapper.MentalFileMapper;
import com.ruoyi.mental.mapper.MentalKnowledgeMapper;
import com.ruoyi.system.service.ISysUserService;

@Service
public class MentalAppService
{
    private static final Logger log = LoggerFactory.getLogger(MentalAppService.class);

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${zhipu.api-key:}")
    private String zhipuApiKey;

    @Value("${zhipu.chat-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String zhipuChatUrl;

    @Value("${zhipu.model:glm-4-flash}")
    private String zhipuModel;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private MentalChatMapper chatMapper;

    @Autowired
    private MentalDiaryMapper diaryMapper;

    @Autowired
    private MentalKnowledgeMapper knowledgeMapper;

    @Autowired
    private MentalFileMapper fileMapper;

    public Map<String, Object> login(UserLoginReq req)
    {
        if (req == null || StringUtils.isBlank(req.username()) || StringUtils.isBlank(req.password()))
        {
            throw new ServiceException("用户名和密码不能为空");
        }

        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(req.username().trim(), req.password());
            AuthenticationContextHolder.setContext(authenticationToken);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = tokenService.createToken(loginUser);
            userService.updateLoginInfo(loginUser.getUserId(), IpUtils.getIpAddr(), DateUtils.getNowDate());

            SysUser user = loginUser.getUser();
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getUserId());
            data.put("username", user.getUserName());
            data.put("nickname", user.getNickName());
            data.put("userType", "admin".equalsIgnoreCase(user.getUserName()) ? 2 : 1);
            data.put("role", "admin".equalsIgnoreCase(user.getUserName()) ? "admin" : "user");
            return data;
        }
        catch (BadCredentialsException ex)
        {
            throw new ServiceException("用户名或密码错误");
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
    }

    public void register(UserRegisterReq req)
    {
        if (req == null)
        {
            throw new ServiceException("注册参数不能为空");
        }
        if (StringUtils.isBlank(req.username()) || StringUtils.isBlank(req.password()) || StringUtils.isBlank(req.confirmPassword())
                || StringUtils.isBlank(req.email()))
        {
            throw new ServiceException("用户名、邮箱、密码、确认密码均不能为空");
        }
        if (!StringUtils.equals(req.password(), req.confirmPassword()))
        {
            throw new ServiceException("两次输入的密码不一致");
        }

        SysUser user = new SysUser();
        user.setUserName(req.username().trim());
        user.setNickName(StringUtils.isBlank(req.nickname()) ? req.username().trim() : req.nickname().trim());
        user.setEmail(req.email().trim());
        user.setPhonenumber(StringUtils.isBlank(req.phone()) ? null : req.phone().trim());
        user.setSex(convertGender(req.gender()));
        user.setStatus("0");
        user.setDelFlag("0");
        user.setDeptId(103L);
        user.setPassword(SecurityUtils.encryptPassword(req.password()));
        user.setCreateBy("app_register");

        if (!userService.checkUserNameUnique(user))
        {
            throw new ServiceException("用户名已存在");
        }
        if (StringUtils.isNotBlank(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("手机号已存在");
        }
        if (!userService.checkEmailUnique(user))
        {
            throw new ServiceException("邮箱已存在");
        }
        if (!userService.registerUser(user))
        {
            throw new ServiceException("注册失败");
        }
    }

    public Map<String, Object> pageSessions(Long userId, int pageNum, int pageSize)
    {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNum - 1) * safePageSize;

        List<Map<String, Object>> records = chatMapper.selectSessions(userId, offset, safePageSize);
        long total = chatMapper.countSessions(userId);
        return buildPage(records, total, safePageNum, safePageSize);
    }

    public Map<String, Object> pageSessionsForAdmin(String emotionTag, int currentPage, int size)
    {
        int safePage = Math.max(currentPage, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Map<String, Object>> records = chatMapper.selectSessionsForAdmin(StringUtils.trimToNull(emotionTag), offset, safeSize);
        long total = chatMapper.countSessionsForAdmin(StringUtils.trimToNull(emotionTag));
        return buildPage(records, total, safePage, safeSize);
    }

    public List<Map<String, Object>> listMessages(Long userId, Long sessionId)
    {
        assertSessionBelongsToUser(userId, sessionId);
        return chatMapper.selectMessages(sessionId, userId);
    }

    public List<Map<String, Object>> listMessagesForAdmin(Long sessionId)
    {
        if (sessionId == null)
        {
            throw new ServiceException("会话ID不能为空");
        }
        return chatMapper.selectMessagesForAdmin(sessionId);
    }

    public Map<String, Object> createSession(Long userId, SessionCreateReq req)
    {
        if (req == null || StringUtils.isBlank(req.sessionTitle()))
        {
            throw new ServiceException("会话标题不能为空");
        }

        String initialMessage = StringUtils.trimToEmpty(req.initialMessage());
        MentalChatSession session = new MentalChatSession();
        session.setUserId(userId);
        session.setSessionTitle(req.sessionTitle().trim());
        session.setLastMessage(initialMessage);
        chatMapper.insertSession(session);

        if (StringUtils.isNotBlank(initialMessage))
        {
            MentalChatMessage message = new MentalChatMessage();
            message.setSessionId(session.getId());
            message.setUserId(userId);
            message.setRole("user");
            message.setContent(initialMessage);
            chatMapper.insertMessage(message);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", session.getId());
        data.put("sessionTitle", session.getSessionTitle());
        return data;
    }

    public SseEmitter streamChat(Long userId, StreamChatReq req)
    {
        if (req == null || req.sessionId() == null || StringUtils.isBlank(req.userMessage()))
        {
            throw new ServiceException("会话ID和消息内容不能为空");
        }

        assertSessionBelongsToUser(userId, req.sessionId());

        MentalChatMessage userMessage = new MentalChatMessage();
        userMessage.setSessionId(req.sessionId());
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(req.userMessage().trim());
        chatMapper.insertMessage(userMessage);

        SseEmitter emitter = new SseEmitter(60000L);
        String userPrompt = req.userMessage().trim();
        EmotionResult emotion = estimateEmotion(userPrompt);

        CompletableFuture.runAsync(() -> {
            try
            {
                String reply = streamReplyFromZhipu(userPrompt, emitter);
                if (StringUtils.isBlank(reply))
                {
                    throw new ServiceException("智谱返回内容为空");
                }

                MentalChatMessage assistant = new MentalChatMessage();
                assistant.setSessionId(req.sessionId());
                assistant.setUserId(userId);
                assistant.setRole("assistant");
                assistant.setContent(reply);
                chatMapper.insertMessage(assistant);
                chatMapper.refreshSession(req.sessionId(), userId, userPrompt);
                upsertEmotion(userId, req.sessionId(), emotion);

                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            }
            catch (Exception ex)
            {
                log.error("流式对话调用失败, sessionId={}, userId={}", req.sessionId(), userId, ex);
                emitter.completeWithError(ex);
            }
        });

        return emitter;
    }

    private String streamReplyFromZhipu(String userMessage, SseEmitter emitter) throws Exception
    {
        if (StringUtils.isBlank(zhipuApiKey))
        {
            throw new ServiceException("智谱API Key未配置");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", zhipuModel);
        payload.put("stream", true);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "你是一名温和、共情、简洁的心理咨询助手。"));
        messages.add(Map.of("role", "user", "content", userMessage));
        payload.put("messages", messages);

        String body = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder(URI.create(zhipuChatUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + zhipuApiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() >= 400)
        {
            String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new ServiceException("智谱调用失败: " + errorBody);
        }

        StringBuilder reply = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String trimmed = line.trim();
                if (StringUtils.isBlank(trimmed) || !trimmed.startsWith("data:"))
                {
                    continue;
                }

                String jsonPart = trimmed.substring(5).trim();
                if ("[DONE]".equals(jsonPart))
                {
                    break;
                }

                JsonNode root = objectMapper.readTree(jsonPart);
                JsonNode contentNode = root.path("choices").path(0).path("delta").path("content");
                if (contentNode.isTextual())
                {
                    String chunk = contentNode.asText();
                    if (StringUtils.isNotBlank(chunk))
                    {
                        reply.append(chunk);
                        emitter.send(SseEmitter.event().data(chunk));
                    }
                }
            }
        }
        return reply.toString();
    }

    public Map<String, Object> getSessionEmotion(Long userId, Long sessionId)
    {
        assertSessionBelongsToUser(userId, sessionId);
        Map<String, Object> data = chatMapper.selectEmotion(sessionId, userId);
        if (data == null || data.isEmpty())
        {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("dominantEmotion", "平静");
            fallback.put("moodScore", 80);
            return fallback;
        }
        return data;
    }

    public void deleteSession(Long userId, Long sessionId)
    {
        assertSessionBelongsToUser(userId, sessionId);
        chatMapper.deleteSessionMessages(sessionId, userId);
        chatMapper.deleteSession(sessionId, userId);
    }

    public void upsertDiary(Long userId, EmotionDiaryReq req)
    {
        if (req == null)
        {
            throw new ServiceException("请求参数不能为空");
        }

        LocalDate diaryDate;
        try
        {
            diaryDate = LocalDate.parse(req.diaryDate());
        }
        catch (DateTimeParseException e)
        {
            throw new ServiceException("记录日期格式错误，应为yyyy-MM-dd");
        }

        validateScore(req.moodScore(), "情绪评分");
        validateScore(req.sleepQuality(), "睡眠质量");
        validateScore(req.stressLevel(), "压力水平");

        if (StringUtils.isBlank(req.dominantEmotion()) || StringUtils.isBlank(req.emotionTriggers()) || StringUtils.isBlank(req.diaryContent()))
        {
            throw new ServiceException("主导情绪、触发因素、日记内容不能为空");
        }

        MentalEmotionDiary diary = new MentalEmotionDiary();
        diary.setUserId(userId);
        diary.setDiaryDate(diaryDate);
        diary.setMoodScore(req.moodScore());
        diary.setDominantEmotion(req.dominantEmotion().trim());
        diary.setEmotionTriggers(req.emotionTriggers().trim());
        diary.setDiaryContent(req.diaryContent().trim());
        diary.setSleepQuality(req.sleepQuality());
        diary.setStressLevel(req.stressLevel());

        Long existId = diaryMapper.selectDiaryId(userId, diaryDate);
        if (existId == null)
        {
            diaryMapper.insertDiary(diary);
        }
        else
        {
            diary.setId(existId);
            diaryMapper.updateDiary(diary);
        }
    }

    public Map<String, Object> pageKnowledge(String sortField, String sortDirection, int currentPage, int size)
    {
        String orderBy = resolveKnowledgeOrderBy(sortField, sortDirection);
        int safePage = Math.max(currentPage, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Map<String, Object>> records = knowledgeMapper.selectPublishedArticles(offset, safeSize, orderBy);
        long total = knowledgeMapper.countPublishedArticles();
        return buildPage(records, total, safePage, safeSize);
    }

    public Map<String, Object> pageKnowledgeForAdmin(String title, Integer categoryId, String status, String authorName,
            String sortField, String sortDirection, int currentPage, int size)
    {
        int safePage = Math.max(currentPage, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        String sortClause = resolveKnowledgeOrderBy(sortField, sortDirection);
        String categoryName = resolveCategoryName(categoryId);

        List<Map<String, Object>> records = knowledgeMapper.selectArticlesForAdmin(StringUtils.trimToNull(title), categoryName,
                StringUtils.trimToNull(status), StringUtils.trimToNull(authorName), offset, safeSize, sortClause);
        long total = knowledgeMapper.countArticlesForAdmin(StringUtils.trimToNull(title), categoryName, StringUtils.trimToNull(status),
                StringUtils.trimToNull(authorName));
        return buildPage(records, total, safePage, safeSize);
    }

    public List<Map<String, Object>> categoryTreeForAdmin()
    {
        List<Map<String, Object>> categories = knowledgeMapper.selectCategorySummary();
        List<Map<String, Object>> children = new ArrayList<>();
        long seed = 1L;
        for (Map<String, Object> category : categories)
        {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", seed);
            node.put("label", category.get("categoryName"));
            node.put("name", category.get("categoryName"));
            node.put("articleCount", category.get("articleCount"));
            children.add(node);
            seed++;
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", 0);
        root.put("label", "知识库");
        root.put("name", "知识库");
        root.put("children", children);
        return List.of(root);
    }

    public void createArticleForAdmin(AdminArticleReq req)
    {
        validateAdminArticleReq(req);
        String author = SecurityUtils.getUsername();
        String categoryName = resolveCategoryName(req.categoryId());
        int rows = knowledgeMapper.insertArticle(req.title().trim(), req.summary().trim(), req.content().trim(), categoryName,
                StringUtils.trimToEmpty(req.coverImage()), author, "1");
        if (rows <= 0)
        {
            throw new ServiceException("文章新增失败");
        }
    }

    public Map<String, Object> getKnowledgeDetailForAdmin(Long id)
    {
        if (id == null)
        {
            throw new ServiceException("文章ID不能为空");
        }
        Map<String, Object> detail = knowledgeMapper.selectArticleDetailForAdmin(id);
        if (detail == null)
        {
            throw new ServiceException("文章不存在");
        }
        return detail;
    }

    public void updateArticleForAdmin(Long id, AdminArticleReq req)
    {
        if (id == null)
        {
            throw new ServiceException("文章ID不能为空");
        }
        validateAdminArticleReq(req);
        int rows = knowledgeMapper.updateArticle(id, req.title().trim(), req.summary().trim(), req.content().trim(),
                resolveCategoryName(req.categoryId()), StringUtils.trimToEmpty(req.coverImage()));
        if (rows <= 0)
        {
            throw new ServiceException("文章不存在或已删除");
        }
    }

    public void updateArticleStatusForAdmin(Long id, AdminArticleStatusReq req)
    {
        if (id == null)
        {
            throw new ServiceException("文章ID不能为空");
        }
        if (req == null || StringUtils.isBlank(req.status()))
        {
            throw new ServiceException("文章状态不能为空");
        }
        String status = req.status().trim();
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("文章状态只支持1(发布)或2(下线)");
        }
        int rows = knowledgeMapper.updateArticleStatus(id, status);
        if (rows <= 0)
        {
            throw new ServiceException("文章不存在或已删除");
        }
    }

    public void deleteArticleForAdmin(Long id)
    {
        if (id == null)
        {
            throw new ServiceException("文章ID不能为空");
        }
        if (knowledgeMapper.deleteArticle(id) <= 0)
        {
            throw new ServiceException("文章不存在或已删除");
        }
    }

    public Map<String, Object> pageDiaryForAdmin(Long userId, Integer minMoodScore, Integer maxMoodScore, String dominantEmotion,
            int currentPage, int size)
    {
        int safePage = Math.max(currentPage, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Map<String, Object>> records = diaryMapper.selectDiaryForAdmin(userId, minMoodScore, maxMoodScore,
                StringUtils.trimToNull(dominantEmotion), offset, safeSize);
        long total = diaryMapper.countDiaryForAdmin(userId, minMoodScore, maxMoodScore, StringUtils.trimToNull(dominantEmotion));
        return buildPage(records, total, safePage, safeSize);
    }

    public void deleteDiaryForAdmin(Long id)
    {
        if (id == null)
        {
            throw new ServiceException("日记ID不能为空");
        }
        if (diaryMapper.deleteDiaryById(id) <= 0)
        {
            throw new ServiceException("日记不存在或已删除");
        }
    }

    public Map<String, Object> getOverviewAnalytics()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", knowledgeMapper.countTotalUsers());
        data.put("totalSessions", knowledgeMapper.countTotalSessions());
        data.put("totalDiaries", knowledgeMapper.countTotalDiaries());
        data.put("avgMoodScore", knowledgeMapper.avgMoodScore());
        data.put("activeUsers", knowledgeMapper.countActiveUsers());
        data.put("todayNewDiaries", knowledgeMapper.countTodayDiaries());
        data.put("todayNewSessions", knowledgeMapper.countTodaySessions());
        data.put("emotionDistribution", knowledgeMapper.selectEmotionDistribution());
        data.put("emotionTrend", knowledgeMapper.selectEmotionTrend(30));
        data.put("consultationTrend", knowledgeMapper.selectConsultationTrend(30));
        return data;
    }

    public void saveUploadRecord(String businessType, String businessId, String businessField, String originName, String fileName,
            String fileUrl, long fileSize)
    {
        if (StringUtils.isBlank(businessType) || StringUtils.isBlank(businessId) || StringUtils.isBlank(businessField))
        {
            throw new ServiceException("businessType、businessId、businessField不能为空");
        }
        Long uploaderId = SecurityUtils.getUserId();
        String uploaderName = SecurityUtils.getUsername();
        fileMapper.insertFileRecord(businessType.trim(), businessId.trim(), businessField.trim(), originName, fileName, fileUrl,
                fileSize, uploaderId, uploaderName);
    }

    public Map<String, Object> getKnowledgeDetail(Long id)
    {
        knowledgeMapper.increaseReadCount(id);
        Map<String, Object> detail = knowledgeMapper.selectArticleDetail(id);
        if (detail == null)
        {
            throw new ServiceException("文章不存在");
        }
        return detail;
    }

    private void assertSessionBelongsToUser(Long userId, Long sessionId)
    {
        if (sessionId == null)
        {
            throw new ServiceException("会话ID不能为空");
        }
        if (chatMapper.countSession(sessionId, userId) <= 0)
        {
            throw new ServiceException("会话不存在或无权限访问");
        }
    }

    private void upsertEmotion(Long userId, Long sessionId, EmotionResult emotion)
    {
        if (chatMapper.countEmotion(sessionId, userId) <= 0)
        {
            chatMapper.insertEmotion(sessionId, userId, emotion.dominantEmotion(), emotion.moodScore());
        }
        else
        {
            chatMapper.updateEmotion(sessionId, userId, emotion.dominantEmotion(), emotion.moodScore());
        }
    }

    private EmotionResult estimateEmotion(String text)
    {
        String msg = StringUtils.trimToEmpty(text);
        if (msg.contains("焦虑") || msg.contains("紧张") || msg.contains("害怕"))
        {
            return new EmotionResult("焦虑", 4);
        }
        if (msg.contains("难过") || msg.contains("伤心") || msg.contains("低落"))
        {
            return new EmotionResult("低落", 3);
        }
        if (msg.contains("开心") || msg.contains("高兴") || msg.contains("愉快"))
        {
            return new EmotionResult("愉悦", 8);
        }
        return new EmotionResult("平静", 7);
    }

    private String buildReply(String userMessage)
    {
        return "我已经收到你的感受：" + userMessage
                + "。你可以先做三次深呼吸，感受身体的变化；如果愿意，也可以继续说说最让你困扰的具体场景。";
    }

    private Map<String, Object> buildPage(List<Map<String, Object>> records, long total, int currentPage, int size)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("currentPage", currentPage);
        data.put("size", size);
        return data;
    }

    private void validateScore(Integer score, String fieldName)
    {
        if (score == null || score < 1 || score > 10)
        {
            throw new ServiceException(fieldName + "必须在1到10之间");
        }
    }

    private String convertGender(Integer gender)
    {
        if (gender == null)
        {
            return "2";
        }
        return switch (gender)
        {
            case 1 -> "0";
            case 2 -> "1";
            default -> "2";
        };
    }

    private String resolveKnowledgeOrderBy(String sortField, String sortDirection)
    {
        String field = "read_count";
        if ("createTime".equalsIgnoreCase(sortField) || "create_time".equalsIgnoreCase(sortField))
        {
            field = "create_time";
        }
        else if ("updateTime".equalsIgnoreCase(sortField) || "update_time".equalsIgnoreCase(sortField))
        {
            field = "update_time";
        }
        else if ("publishTime".equalsIgnoreCase(sortField) || "publish_time".equalsIgnoreCase(sortField))
        {
            field = "publish_time";
        }
        else if ("title".equalsIgnoreCase(sortField))
        {
            field = "title";
        }

        String direction = "DESC";
        if ("asc".equalsIgnoreCase(sortDirection))
        {
            direction = "ASC";
        }
        return field + " " + direction;
    }

    private void validateAdminArticleReq(AdminArticleReq req)
    {
        if (req == null)
        {
            throw new ServiceException("文章参数不能为空");
        }
        if (StringUtils.isBlank(req.title()) || StringUtils.isBlank(req.content()) || StringUtils.isBlank(req.summary()))
        {
            throw new ServiceException("标题、内容、摘要不能为空");
        }
        if (req.categoryId() == null)
        {
            throw new ServiceException("分类ID不能为空");
        }
    }

    private String resolveCategoryName(Integer categoryId)
    {
        if (categoryId == null)
        {
            return null;
        }
        return switch (categoryId)
        {
            case 1 -> "情绪管理";
            case 2 -> "睡眠健康";
            case 3 -> "压力应对";
            case 4 -> "人际关系";
            default -> "心理健康";
        };
    }
}
