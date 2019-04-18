package com.vtradex.ehub.third.lbsthird.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.lbs.entity.request.LbsUploadLocationRequest;
import com.vtradex.ehub.lbs.services.LbsHandlerService;
import com.vtradex.ehub.plugins.com.google.common.collect.Lists;
import com.vtradex.ehub.sdk.caller.SdkNoticeCommand;
import com.vtradex.ehub.sdk.caller.SdkNoticeResult;
import com.vtradex.ehub.sdk.concurrent.SdkListener;
import com.vtradex.ehub.sdk.impl.DefaultSdkClient;

public class SdkClientUtil {

	public static DefaultSdkClient sdkClient;

	public static Logger LOGGER = LoggerFactory.getLogger(SdkClientUtil.class);

	/**
	 * 上传定位
	 */
	public static void upload84Location(String orgKey, LbsUploadLocationRequest location) {
		String deviceNo = location.getDeviceId();
		List<LbsUploadLocationRequest> list = Lists.newArrayList();
		list.add(location);
		String orderKey = orgKey + "-" + deviceNo + "-" + "saveLocation";
		// 调用ehub-sdk 发送任务
		sdkClient.getSdkExtBean(LbsHandlerService.class)
				.sdkAsyncNotice(new SdkNoticeCommand("save84Location", list), orderKey)
				.addListener(new SdkListener<SdkNoticeResult>() {
					@Override
					public void onFailure(Throwable var1) {
						// TODO Auto-generated method stub
						LOGGER.error("一条定位上传失败", var1);
					}

					@Override
					public void onSuccess(SdkNoticeResult var1) {
						// TODO Auto-generated method stub
						LOGGER.debug("一条定位上传成功，消息ID：{}", var1.getId());
					}
				});
	}
}
