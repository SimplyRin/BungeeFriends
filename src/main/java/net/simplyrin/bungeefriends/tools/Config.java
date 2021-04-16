package net.simplyrin.bungeefriends.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * Created by SimplyRin on 2018/10/08.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class Config {

	public static void saveConfig(Configuration config, String file) {
		saveConfig(config, new File(file));
	}

	public static void saveConfig(Configuration config, File file) {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Configuration getConfig(String file) {
		return getConfig(new File(file));
	}

	public static Configuration getConfig(File file) {
		try {
			return getProvider().load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Configuration loadConfig(String file) {
		return getConfig(new File(file));
	}

	public static Configuration loadConfig(File file) {
		return getConfig(file);
	}

	public static Configuration getConfig(InputStream is) {
		return getProvider().load(is);
	}

	public static Configuration getConfig(File file, Charset charset) {
		try {
			return getProvider().load(new InputStreamReader(new FileInputStream(file), charset));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Configuration getConfig(URL url) throws Exception {
		try {
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setReadTimeout(5000);
			connection.setConnectTimeout(5000);
			InputStream inputStream = connection.getInputStream();
			return getProvider().load(inputStream);
		} catch (Exception e) {
			throw e;
		}
	}

	private static ConfigurationProvider getProvider() {
		return ConfigurationProvider.getProvider(YamlConfiguration.class);
	}

}
