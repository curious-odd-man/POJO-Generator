package fr.jukien.intellij.plugins.action;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import fr.jukien.intellij.plugins.ui.JPAMappingSettings;
import fr.jukien.intellij.plugins.ui.POJOGeneratorSettings;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.jukien.intellij.plugins.util.Util.lastChoosedFile;

public abstract class GenericEntityAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (null == project) {
            return;
        }

        final POJOGeneratorSettings pojoGeneratorSettings = ServiceManager.getService(project, POJOGeneratorSettings.class);
        final JPAMappingSettings jpaMappingSettings = ServiceManager.getService(project, JPAMappingSettings.class);

        PsiElement[] psiElements = anActionEvent.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return;
        }

        if (null != project.getBasePath()) {
            Path projectPath = Paths.get(project.getBasePath());
            VirtualFile chooseFile = null;
            try {
                chooseFile = VfsUtil.findFileByURL(projectPath.toUri().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            if (null != pojoGeneratorSettings.getEntityFolderPath()) {
                try {
                    chooseFile = VfsUtil.findFileByURL(Paths.get(pojoGeneratorSettings.getEntityFolderPath()).toUri().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            lastChoosedFile = FileChooser.chooseFile(descriptor, project, chooseFile);
            if (null == lastChoosedFile) {
                return;
            } else {
                pojoGeneratorSettings.setEntityFolderPath(lastChoosedFile.getPath());
            }

            for (PsiElement psiElement : psiElements) {
                if (psiElement instanceof DbTable) {
                    processTable(project, pojoGeneratorSettings, jpaMappingSettings, (DbTable) psiElement);
                }
            }
        }
    }

    protected abstract void processTable(Project project, POJOGeneratorSettings pojoGeneratorSettings, JPAMappingSettings jpaMappingSettings, DbTable psiElement);
}
