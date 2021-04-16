package net.simplyrin.bungeefriends.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SimplyRin on 2018/06/06.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class ThreadPool {

	private static ExecutorService executorService = Executors.newFixedThreadPool(10240);

	public static void run(Runnable runnable) {
		runAsync(runnable);
	}

	public static void runAsync(Runnable runnable) {
		executorService.execute(runnable);
	}

}
