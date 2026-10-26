package com.jarvis.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** Optional user-enabled accessibility helper for in-call UI buttons. */
public class CallControlAccessibilityService extends AccessibilityService {
    public static final String ACTION_ANSWER = "com.jarvis.assistant.CALL_ANSWER";
    public static final String ACTION_END = "com.jarvis.assistant.CALL_END";
    public static final String ACTION_SPEAKER = "com.jarvis.assistant.CALL_SPEAKER";
    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            if (ACTION_ANSWER.equals(action)) clickLabels(new String[]{"answer", "accept", "answer call", "उठाएं", "जवाब दें"});
            else if (ACTION_END.equals(action)) clickLabels(new String[]{"end", "decline", "hang up", "end call", "काटें", "अस्वीकार"});
            else if (ACTION_SPEAKER.equals(action)) clickLabels(new String[]{"speaker", "speakerphone", "स्पीकर"});
        }
    };
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ANSWER); filter.addAction(ACTION_END); filter.addAction(ACTION_SPEAKER);
        if (android.os.Build.VERSION.SDK_INT >= 33) registerReceiver(controlReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(controlReceiver, filter);
    }
    @Override public void onDestroy() {
        try { unregisterReceiver(controlReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
    private void clickLabels(String[] labels) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        try {
            for (String label : labels) {
                java.util.List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(label);
                if (nodes == null) continue;
                for (AccessibilityNodeInfo node : nodes) {
                    if (node != null && node.isClickable() && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return;
                    AccessibilityNodeInfo parent = node == null ? null : node.getParent();
                    if (parent != null && parent.isClickable() && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return;
                }
            }
        } finally { root.recycle(); }
    }
}
