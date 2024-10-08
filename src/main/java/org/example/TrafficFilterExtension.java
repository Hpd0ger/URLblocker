package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrafficFilterExtension implements BurpExtension {
    private MontoyaApi api;
    private Logging log;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        this.log = api.logging();
        api.extension().setName("URL Blocker");

        // 注册上下文菜单项
        api.userInterface().registerContextMenuItemsProvider(new CustomContextMenuItemsProvider());
        log.logToOutput("==URL Blocker extension initialized==");
        log.logToOutput("======== Designed by Hpdoger ========");
    }

    private class CustomContextMenuItemsProvider implements ContextMenuItemsProvider {
        @Override
        public List<Component> provideMenuItems(ContextMenuEvent event) {
            ArrayList<Component> menus = new ArrayList<>();
            if (event.isFromTool(ToolType.PROXY, ToolType.TARGET, ToolType.REPEATER)) {
                JMenuItem blockMenuItem = new JMenuItem("Block This");
                blockMenuItem.addActionListener(e -> blockTraffic(event));
                menus.add(blockMenuItem);
                return menus;
            }
            return menus;
        }
    }

    private void blockTraffic(ContextMenuEvent event) {
        // 获取URL
        String url = "";
        List<HttpRequestResponse> requestResponses = event.selectedRequestResponses();
        if (!requestResponses.isEmpty()) {
            HttpRequestResponse requestResponse = requestResponses.get(0);
            url = requestResponse.request().url();
        } else {
            // 如果没有选中的请求/响应，尝试从消息编辑器获取
            Optional<MessageEditorHttpRequestResponse> messageEditors = event.messageEditorRequestResponse();
            if (messageEditors.isPresent()) {
                MessageEditorHttpRequestResponse editor = messageEditors.get();
                url = editor.requestResponse().request().url();
            }
        }
        // add to Exclude from scope
        if (!url.isEmpty()) {
            api.scope().excludeFromScope(url);
        } else {
            log.logToError("excludeFromScope failed, url is empty");
        }
    }
}