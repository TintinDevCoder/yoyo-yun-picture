package com.dd.yunpicturebackend.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.dd.yunpicturebackend.enums.SpaceRoleEnum;
import com.dd.yunpicturebackend.enums.SpaceTypeEnum;
import com.dd.yunpicturebackend.model.entity.Picture;
import com.dd.yunpicturebackend.model.entity.Space;
import com.dd.yunpicturebackend.model.entity.SpaceUser;
import com.dd.yunpicturebackend.model.entity.User;
import com.dd.yunpicturebackend.service.PictureService;
import com.dd.yunpicturebackend.service.SpaceService;
import com.dd.yunpicturebackend.service.SpaceUserService;
import com.dd.yunpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket 握手拦截器,建立连接前先校验
 */
@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 建立连接前先校验
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 WebSocketHandler 会话设置属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 获取当前用户
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (loginUser == null) {
                log.error("用户未登录，拒绝握手");
                return false;
            }
            // 校验用户有编辑当前图片的权限
            Picture picture = pictureService.getById(Long.valueOf(pictureId));
            if (picture == null) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            if (spaceId != null) {
                SpaceUser spaceUser = spaceUserService.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId)
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (ObjUtil.isNull(spaceUser)) {
                    log.error("用户无权限，拒绝握手");
                    return false;
                }
                String spaceRole = spaceUser.getSpaceRole();
                if (!SpaceRoleEnum.ADMIN.getValue().equals(spaceRole) && !SpaceRoleEnum.EDITOR.getValue().equals(spaceRole)) {
                    log.error("用户无权限，拒绝握手");
                    return false;
                }
            }
            // 设置用户登陆信息等属性到 Websocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
