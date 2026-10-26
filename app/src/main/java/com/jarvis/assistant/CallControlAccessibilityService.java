package com.jarvis.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** Optional user-enabled accessibility helper for in-call UI buttons. */
public class CallControlAccessibilityService extends AccessibilityService {
    public static final String ACTION_ANSWER = "com.jarvis.assistant.CALL_ANSWER";
    public static final String ACTION_END = "com.jarvis.assistant.CALL_END";
    public static final String ACTION_SPEAKER = "com.jarvis.assistant.CALL_SPEAKER";
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override protected void onServiceConnected() { super.onServiceConnected(); }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_ANSWER.equals(action)) clickLabels(new String[]{"answer", "accept", "answer call", "उठाएं", "जवाब दें"});
            else if (ACTION_END.equals(action)) clickLabels(new String[]{"end", "decline", "hang up", "end call", "काटें", "अस्वीकार"});
            else if (ACTION_SPEAKER.equals(action)) clickLabels(new String[]{"speaker", "speakerphone", "स्पीकर"});
        }
        return START_NOT_STICKY;
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
