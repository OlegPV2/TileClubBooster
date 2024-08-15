package com.oleg.tileclubbooster.readdata;

import static com.oleg.tileclubbooster.readdata.Exec.ShizukuExec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @Author AnswerDev
 * @Date 2024/06/17 20:49
 */
public class ShizukuFileUtil {

    public static List list(String path) {
        final ArrayList<String> files = new ArrayList<>();
        try {
            if(files.size() !=0) files.clear();
            ShizukuExec("ls " + path, new Function<String, String>(){
                    @Override
                    public String apply(String t) {
                        files.add(t);
                        return null;
                    }
                }, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public static String read(String path) {
        final StringBuilder builder = new StringBuilder();
        try {
            ShizukuExec("cat " + path, new Function<String, String>(){
                    @Override
                    public String apply(String t) {
                        builder.append(t);
                        return null;
                    }
                }, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public static Future<Boolean> move(String oldFile, String newFile) {
        return runVoidCommand("mv " + oldFile + " " + newFile);
    }

    public static Future<Boolean> remove(String path) {
        return runVoidCommand("rm -f " + path);
    }

    public static Future<Boolean> removeDir(String path) {
        return runVoidCommand("rmdir -f " + path);
    }

    public static Future<Boolean> copy(String directoryA, String directoryB) {
        return runVoidCommand("cp " + directoryA + " " + directoryB);
    }

    private static Future<Boolean> runVoidCommand(String command) {
        final Future<Boolean> future = new Future<>();
        try {
            ShizukuExec(command, new Function<String, String>(){
                    @Override
                    public String apply(String t) {
                        future.complete(true);
                        return null;
                    }
                }, new Function<String, String>(){
                    @Override
                    public String apply(String t) {
                        future.complete(false);
                        return null;
                    }}, new Function<String, String>(){
                    @Override
                    public String apply(String t) {
                        if (t.contains("0"))future.complete(true);
                        return null;
                    }});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return future;
    }

};
