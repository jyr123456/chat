package cn.ittiger.im.app;

import cn.ittiger.database.SQLiteDBConfig;

/**
 * 本地数据库接口
 */
public interface IDbApplication {
	/**
	 * 系统全局数据库配置
	 * @return
	 */
	SQLiteDBConfig getGlobalDbConfig();
}
