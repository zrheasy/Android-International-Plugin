package com.zrh.international;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PopupDialogAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ConfigDialog dialog = new ConfigDialog(event.getProject());
        dialog.pack();
        dialog.setSize(800,400);
        dialog.show();
    }

}