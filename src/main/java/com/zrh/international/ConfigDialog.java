package com.zrh.international;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.ui.DocumentAdapter;
import com.zrh.international.business.Translator;
import com.zrh.international.business.ExternalConfig;
import com.zrh.international.utils.FileUtils;
import com.zrh.international.utils.FormScalingUtil;
import com.zrh.international.utils.PropertiesUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class ConfigDialog extends DialogWrapper {

    public static final String EXCEL_PATH = "ET_EXCEL_PATH";
    public static final String SHEET_NAME = "ET_SHEET_NAME";
    public static final String WORK_DIR = "ET_WORK_DIR";
    public static final String CONFIG_FILE_PATH = "ET_CONFIG_FILE_PATH";

    private String configFilePath;
    private String excelFilePath;
    private String workDirPath;
    private String sheetName;

    private final Project project;

    public ConfigDialog(Project project) {
        super(project);
        this.project = project;

        excelFilePath = PropertiesUtils.get(EXCEL_PATH);
        sheetName = PropertiesUtils.get(SHEET_NAME);
        workDirPath = PropertiesUtils.get(WORK_DIR, getDefaultWorkDirPath());
        configFilePath = PropertiesUtils.get(CONFIG_FILE_PATH, getDefaultConfigPath());

        setTitle("Android International Plugin");
        init();
    }

    private String getDefaultWorkDirPath() {
        if (project.getBasePath() != null) {
            return project.getBasePath() + "/app";
        }
        return "";
    }

    private String getDefaultConfigPath() {
        if (project.getBasePath() != null) {
            return project.getBasePath() + "/international.json";
        }
        return "";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new VerticalFlowLayout());
        FormScalingUtil.scaleComponentTree(this.getClass(), contentPanel);

        contentPanel.add(createExcelFilePanel());
        contentPanel.add(createSheetNamePanel());
        contentPanel.add(createWorkDirPanel());
        contentPanel.add(createConfigFilePanel());
        return contentPanel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {

        if (!new File(excelFilePath).exists()) {
            return new ValidationInfo("Excel文件不存在");
        } else {
            PropertiesUtils.save(EXCEL_PATH, excelFilePath);
        }

        if (sheetName == null || sheetName.isEmpty()) {
            return new ValidationInfo("请输入要同步的Sheet名称");
        } else {
            PropertiesUtils.save(SHEET_NAME, sheetName);
        }

        if (!new File(workDirPath).exists()) {
            return new ValidationInfo("工作目录不存在");
        } else {
            PropertiesUtils.save(WORK_DIR, workDirPath);
        }

        File file = new File(configFilePath);
        if (!file.exists() || !file.getName().endsWith(".json")) {
            return new ValidationInfo("无效的配置文件");
        } else {
            PropertiesUtils.save(CONFIG_FILE_PATH, configFilePath);
        }

        return super.doValidate();
    }

    private void checkError() {
        ValidationInfo info = doValidate();
        if (info == null) return;

        if (!Registry.is("ide.inplace.validation.tooltip", true)) {
            DialogEarthquakeShaker.shake(getPeer().getWindow());
        }

        ArrayList<ValidationInfo> infoList = new ArrayList<>();
        infoList.add(info);
        updateErrorInfo(infoList);
        startTrackingValidation();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "同步文案") {

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {

                progressIndicator.setText("Loading...");

                File excelFile = new File(excelFilePath);
                String sheet = sheetName;
                File moduleDir = new File(workDirPath);
                ExternalConfig externalConfig = parseConfig(configFilePath);

                Translator.Configure configure = new Translator.Configure(
                        excelFile, sheet, moduleDir,
                        externalConfig.getTagNameIndex(),
                        externalConfig.getDefaultLanguage(),
                        externalConfig.getTextIndexMap()
                );
                configure.setMatchRegex(externalConfig.getMatchRegex());
                configure.setUseDefault(externalConfig.isUseDefault());
                try {
                    Translator.getInstance().translateSync(configure);
                    String resDirPath = workDirPath + "/src/main/res/";
                    File resDir = new File(resDirPath);
                    // 刷新资源文件目录
                    VfsUtil.markDirtyAndRefresh(true, true, true, resDir);
                    showMsg("Translate Success!");
                } catch (Exception e) {
                    e.printStackTrace();
                    showMsg("Translate error:" + e);
                }

                progressIndicator.setIndeterminate(true);
            }
        });
    }

    private void showMsg(String msg) {
        Notification notification = new Notification(
                Notifications.SYSTEM_MESSAGES_GROUP_ID,
                "EasyTranslator",
                msg,
                NotificationType.INFORMATION);

        Notifications.Bus.notify(notification, project);
    }

    private ExternalConfig parseConfig(String filePath) {
        try {
            String json = FileUtils.getString(filePath);
            Gson gson = new Gson();
            ExternalConfig config = gson.fromJson(json, ExternalConfig.class);
            if (config != null) return config;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        // 返回一个默认的配置
        return new ExternalConfig();
    }

    private JPanel createConfigFilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new VerticalFlowLayout());

        JLabel label = new JLabel("请选择配置文件");
        label.setFont(new Font("Dialog", 1, 12));
        panel.add(label);

        TextFieldWithBrowseButton button = new TextFieldWithBrowseButton();
        button.addBrowseFolderListener(
                "选择配置文件", null, null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );
        button.setText(configFilePath);
        button.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                configFilePath = button.getTextField().getText();
                checkError();
            }
        });
        panel.add(button);
        return panel;
    }

    private JPanel createExcelFilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new VerticalFlowLayout());

        JLabel label = new JLabel("选择excel翻译文件");
        label.setFont(new Font("Dialog", 1, 12));
        panel.add(label);

        TextFieldWithBrowseButton button = new TextFieldWithBrowseButton();
        button.addBrowseFolderListener(
                "选择excel翻译文件", null, null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );
        button.setText(excelFilePath);
        button.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                excelFilePath = button.getTextField().getText();
                checkError();
            }
        });
        panel.add(button);
        return panel;
    }

    private JPanel createWorkDirPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new VerticalFlowLayout());

        JLabel label = new JLabel("请选择文案所在的Module目录");
        label.setFont(new Font("Dialog", 1, 12));
        panel.add(label);

        TextFieldWithBrowseButton button = new TextFieldWithBrowseButton();
        button.addBrowseFolderListener(
                "选择工作目录", null, null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );
        button.setText(workDirPath);
        button.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                workDirPath = button.getTextField().getText();
                checkError();
            }
        });
        panel.add(button);
        return panel;
    }

    private JPanel createSheetNamePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new VerticalFlowLayout());

        JLabel label = new JLabel("请输入Sheet表格名称");
        label.setFont(new Font("Dialog", 1, 12));
        panel.add(label);

        JTextField textField = new JTextField();
        textField.setText(sheetName);
        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                sheetName = textField.getText();
                checkError();
            }
        });

        panel.add(textField);
        return panel;
    }
}
