package com.oleg.tileclubbooster.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.oleg.tileclubbooster.R;
import com.oleg.tileclubbooster.constant.PathType;
import com.oleg.tileclubbooster.readdata.ShizukuFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class GameJSON {

	private static String loadExternalJSON(Context context, String path, String file) {
		if (FileTools.specialPathReadType != PathType.SHIZUKU) {
			if (!FileTools.shouldRequestUriPermission(FileTools.dataPath)) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
					return FileTools.readDocumentFile(context, path, file);
				} else {
					return FileTools.readFile(path, file);
				}
			}
		} else {
			return ShizukuFileUtil.read(path + file);
		}
		return "";
	}

	private static String loadJSONFromAsset(Context context, String file) {
		String jString;
		try {
			InputStream is = context.getAssets().open(file);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			jString = new String(buffer, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Log.e("loadJSON", String.valueOf(e));
			return "";
		}
		return jString;
	}

	public static String getCurrentLevel(Context context) {
		try {
			String a = loadExternalJSON(context, FileTools.tileClubFilesPath, "playerProfile.json");
			JSONObject names = new JSONObject(a);
			return names.getString("levelsCompleted");
		} catch (JSONException e) {
			Log.e("currentLevel", String.valueOf(e));
		}
		return context.getResources().getString(R.string.button_try_again_text);
	}

	public static String getCurrentLevelFromLevelsData(Context context) {
		List<String> list = FileTools.getFilesList();
		List<Integer> integerList = list.stream().map(Integer::valueOf).collect(Collectors.toList());
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < integerList.size(); i++){
			if (integerList.get(i) > max) {
				max = integerList.get(i);
			}
		}
		try {
			String a = loadExternalJSON(context, FileTools.tileClubFilesPath + "LevelsData/", max + ".json");
			JSONObject b = new JSONObject(a);
			JSONArray c = b.getJSONArray("subchapters");
			JSONObject d = c.getJSONObject(c.length() - 1);
			JSONArray e = d.getJSONArray("levels");
			JSONObject f = e.getJSONObject(e.length() - 1);
			if (f.getInt("numberOfStars") == -1) {
				try {
					f = e.getJSONObject(e.length() - 2);
				} catch (JSONException exception) {
					d = c.getJSONObject(c.length() - 2);
					e = d.getJSONArray("levels");
					f = e.getJSONObject(e.length() - 1);
				}
			}
			return String.valueOf((f.getInt("levelIndex")) + 1);
		} catch (JSONException e) {
			Log.e("currentLevel", String.valueOf(e));
		}
		return "0";
	}

	public static void currentLevelStatusPatch(Context context, String level) {
		currentLevelStatus(context, "dummy.json", level);
	}

	public static void currentLevelStatusPuzzlesPatch(Context context, String level) {
		currentLevelStatus(context, "puzzles.json", level);
	}

	private static void currentLevelStatus(Context context, String file, String level) {
		try {
			JSONObject dummy = new JSONObject(loadJSONFromAsset(context, file));
			dummy.put("levelIndex", level);
			FileTools.saveToFile(context, FileTools.tileClubFilesPath, "CurrentLevelStatus.json", dummy.toString().getBytes());
		} catch (JSONException e) {
			Log.e("currentLevelStatus", String.valueOf(e));
		}
	}
}