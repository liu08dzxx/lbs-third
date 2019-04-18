package com.vtradex.ehub.third.lbsthird;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.vtradex.ehub.sdk.impl.DefaultSdkClient;
import com.vtradex.ehub.third.lbsthird.jtt809.Jtt809NettyServer;
import com.vtradex.ehub.third.lbsthird.util.SdkClientUtil;

@EnableAutoConfiguration //自动加载配置信息
@SpringBootApplication
public class LbsThirdApplication {

	public static ConfigurableApplicationContext ctx;
	
	public static Logger LOGGER=LoggerFactory.getLogger(LbsThirdApplication.class);
			
			
	public static void main(String[] args) {
		ctx =SpringApplication.run(LbsThirdApplication.class, args);
//		DefaultSdkClient client=(DefaultSdkClient) ctx.getBean("sdkClientLbsDefault");
//		client.start();
//		SdkClientUtil.sdkClient=client;
//		LOGGER.info("注入sdkClient到SdkClientUtil工具类完成");
		Jtt809NettyServer server=new Jtt809NettyServer(1001);
		server.init();
	}
    
	/**
	 * sdkBean,用来上传定位等
	 * @return
	 */
	@Bean(name="sdkClientLbsDefault")
	public DefaultSdkClient defaultSdkClient() {
		DefaultSdkClient client=new DefaultSdkClient("sdkClientLbsDefault");
		//只需要异步功能
		client.setNoticeExecutable(true);
		client.setDelayNoticeExecutable(false);
		client.setInvokeExecutable(false);
		client.setOrderedNoticeExecutable(true);
		client.setInvokable(false);
		return client;
		
	}
}
