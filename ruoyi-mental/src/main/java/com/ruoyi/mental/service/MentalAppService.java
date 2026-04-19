package com.ruoyi.mental.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ruoyi.mental.domain.req.UserLoginReq;
import com.ruoyi.mental.domain.req.UserRegisterReq;
import com.ruoyi.mental.mapper.MentalChatMapper;
import com.ruoyi.mental.mapper.MentalDiaryMapper;
import com.ruoyi.mental.mapper.MentalKnowledgeMapper;
import com.ruoyi.system.service.ISysUserService;

@Service
public class MentalAppService
{
    private static final Pattern CHUNK_SPLITTER = Pattern.compile("(?<=[，。！？；,.!?;])");

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

    public List<Map<String, Object>> listMessages(Long userId, Long sessionId)
    {
        assertSessionBelongsToUser(userId, sessionId);
        return chatMapper.selectMessages(sessionId, userId);
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

        SseEmitter emitter = new SseEmitter(30000L);
        String reply = buildReply(req.userMessage().trim());
        EmotionResult emotion = estimateEmotion(req.userMessage());

        CompletableFuture.runAsync(() -> {
            try
            {
                String[] chunks = CHUNK_SPLITTER.split(reply);
                for (String chunk : chunks)
                {
                    if (StringUtils.isNotBlank(chunk))
                    {
                        emitter.send(SseEmitter.event().data(chunk));
                    }
                }

                MentalChatMessage assistant = new MentalChatMessage();
                assistant.setSessionId(req.sessionId());
                assistant.setUserId(userId);
                assistant.setRole("assistant");
                assistant.setContent(reply);
                chatMapper.insertMessage(assistant);
                chatMapper.refreshSession(req.sessionId(), userId, req.userMessage().trim());
                upsertEmotion(userId, req.sessionId(), emotion);

                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            }
            catch (Exception ex)
            {
                emitter.completeWithError(ex);
            }
        });

        return emitter;
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

        String direction = "DESC";
        if ("asc".equalsIgnoreCase(sortDirection))
        {
            direction = "ASC";
        }
        return field + " " + direction;
    }
}
