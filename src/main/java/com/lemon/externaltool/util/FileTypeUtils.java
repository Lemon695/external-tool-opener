package com.lemon.externaltool.util;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * File Type Utilities
 * 文件类型相关的工具类
 */
public class FileTypeUtils {
    
    /**
     * 获取文件扩展名
     */
    @Nullable
    public static String getFileExtension(@NotNull VirtualFile file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            return name.substring(lastDot);
        }
        return null;
    }
    
    /**
     * 检查文件扩展名是否匹配
     */
    public static boolean matchesExtension(@NotNull VirtualFile file, @NotNull String extension) {
        String fileExt = getFileExtension(file);
        if (fileExt == null) {
            return false;
        }
        
        String ext = extension.toLowerCase();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        
        return fileExt.toLowerCase().equals(ext);
    }
    
    /**
     * 获取文件的MIME类型描述
     */
    @NotNull
    public static String getFileTypeDescription(@NotNull VirtualFile file) {
        String extension = getFileExtension(file);
        if (extension == null) {
            return "Unknown";
        }
        
        // 常见文件类型映射
        switch (extension.toLowerCase()) {
            case ".md":
            case ".markdown":
                return "Markdown Document";
            case ".txt":
                return "Text Document";
            case ".java":
                return "Java Source";
            case ".py":
                return "Python Script";
            case ".js":
                return "JavaScript";
            case ".ts":
                return "TypeScript";
            case ".json":
                return "JSON Data";
            case ".xml":
                return "XML Document";
            case ".html":
            case ".htm":
                return "HTML Document";
            case ".css":
                return "CSS Stylesheet";
            case ".jpg":
            case ".jpeg":
            case ".png":
            case ".gif":
            case ".bmp":
                return "Image File";
            case ".pdf":
                return "PDF Document";
            case ".doc":
            case ".docx":
                return "Word Document";
            case ".xls":
            case ".xlsx":
                return "Excel Spreadsheet";
            case ".ppt":
            case ".pptx":
                return "PowerPoint Presentation";
            default:
                return extension.toUpperCase().substring(1) + " File";
        }
    }
    
    /**
     * 判断是否为文本文件
     */
    public static boolean isTextFile(@NotNull VirtualFile file) {
        String extension = getFileExtension(file);
        if (extension == null) {
            return false;
        }
        
        String[] textExtensions = {
                ".txt", ".md", ".markdown", ".java", ".py", ".js", ".ts",
                ".json", ".xml", ".html", ".htm", ".css", ".log", ".yaml",
                ".yml", ".properties", ".gradle", ".sql", ".sh", ".bat"
        };
        
        for (String textExt : textExtensions) {
            if (extension.equalsIgnoreCase(textExt)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断是否为图片文件
     */
    public static boolean isImageFile(@NotNull VirtualFile file) {
        String extension = getFileExtension(file);
        if (extension == null) {
            return false;
        }
        
        String[] imageExtensions = {
                ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".ico", ".webp"
        };
        
        for (String imgExt : imageExtensions) {
            if (extension.equalsIgnoreCase(imgExt)) {
                return true;
            }
        }
        
        return false;
    }
}
