package com.vtradex.ehub.third.lbsthird.jtt809;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.plugins.com.google.common.base.Strings;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.JT809Constants;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.LoginRequest;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.Message;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.Vehicle;
import com.vtradex.ehub.third.lbsthird.util.ByteBufPool;
import com.vtradex.ehub.third.lbsthird.util.DecimalConversion;
import com.vtradex.ehub.third.lbsthird.util.ProperitiesUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Jtt809Handler extends SimpleChannelInboundHandler<Message> {
    
	private Logger LOGGER=LoggerFactory.getLogger(Jtt809Handler.class);
	
	private LoginStatusEnum loginStatus = LoginStatusEnum.init;
	
	/**
	 * 下级平台接入码,身份标识
	 */
	@Getter
	private int msgGesscenterId;
	
	/**
	 * 定位上传使用的身份，与msgGesscenterId对应
	 */
	@Getter
	private String orgKey;
	
	public Jtt809Handler(int msgGesscenterId) {
		this.msgGesscenterId=msgGesscenterId;
		//根据msgGesscenterId找到对应的orgKey;
		orgKey=ProperitiesUtil.getString("jtt809."+msgGesscenterId+".orgKey");
		if(Strings.isNullOrEmpty(orgKey)) {
			throw new RuntimeException("接入码 :"+msgGesscenterId+" 还没有配置对应的定位上传所属orgKey,请配置:jtt809."+msgGesscenterId+".orgKey 的值"); 
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		//如果是初次连接，设置状态为wating，如果超过5秒钟没登陆成功，则直接断开
        if (loginStatus==LoginStatusEnum.init) {
            loginStatus = LoginStatusEnum.waiting;
            ctx.executor().schedule(new ConnectionTerminator(ctx), 5, TimeUnit.SECONDS);
        }
		switch (msg.getMsgId()) {
		case JT809Constants.UP_CONNECT_REQ:
			handlerLogin(ctx,msg);
			break;
		case JT809Constants.UP_EXG_MSG:
			handlerBiz(ctx,msg);
			break;
		case JT809Constants.UP_LINKETEST_REQ:
			handlerHold(ctx,msg);
			break;	
		default:
			LOGGER.info("未处理的消息类型：{}",DecimalConversion.intToHex(msg.getMsgId()));
			break;
		}
		
		
	}
	
	/**
	 * 主链路连接保持消息处理，这种消息数据体为空，直接发送响应即可
	 * @param ctx
	 * @param msg
	 */
	private void handlerHold(ChannelHandlerContext ctx, Message msg) {
		Message message=new Message(JT809Constants.UP_LINKTEST_RSP);
		message.setMsgGesscenterId(msgGesscenterId);
	    byte[] versioag = {0,0,1};
	    message.setVersionFlag(versioag);
	    message.setEncryptFlag(0L);
	    message.setEncryptKey(0L);
	    Jtt809Util.sendServerMessage(ctx, message);
	}

	/**
	 * 处理业务
	 * @param ctx
	 * @param msg
	 */
	private void handlerBiz(ChannelHandlerContext ctx, Message msg) {
		//如果未登陆，则直接拒绝
		if(loginStatus!=LoginStatusEnum.runnable) {
			LOGGER.warn("接入码为：{} 的下级平台没有登陆",msg.getMsgGesscenterId());
			return;
		}
		ByteBuf msgBody=msg.getMsgBody();
		//构建车辆信息交换对象
		Optional<Vehicle> op=Vehicle.builderVehicle(msgBody);
		if(op.isPresent()) {
			LOGGER.info("接收到定位交换数据请求对象：{},进行业务处理",op.get().toString());
			op.get().handlerBiz(orgKey);
		}
	}


	/**
	 * 处理登陆请求
	 * @param ctx
	 * @param msg
	 */
	private void handlerLogin(ChannelHandlerContext ctx, Message msg) {
		//转换登陆请求报文
		LoginRequest requeset=Jtt809Util.convertLoginRequest(msg);
		//校验参数，获取响应码
		int responseCode=validateLogin(msg,requeset);
	    LOGGER.info("接入码：{},用户：{} 登陆，登陆时间：{},输入的密码是：{},响应码：{}",msg.getMsgGesscenterId(),
	    		requeset.getUserId(),new Date(),requeset.getPassword(),responseCode);
	    //构建登陆响应报文
	    Message loginResponse=builderLoginResponse(responseCode);
	    //发送登陆响应
//	    Jtt809Util.sendServerMessage(ctx, loginResponse);
	    loginResponse.send(ctx);
	    //重复登陆的情况很多，如果重复登陆，则下面的判断不用继续处理
	    if(loginStatus==LoginStatusEnum.runnable) {
	    	return;
	    }
	    //根据响应码设置连接状态
        if(responseCode!=JT809Constants.UP_CONNECT_RSP_SUCCESS) {
        	//断开该连接
        	ctx.close();
        }else {
        	LOGGER.info("接入码：{},用户：{} 登陆成功，登陆时间：{}",msg.getMsgGesscenterId(),
    	    		requeset.getUserId(),new Date());
        	loginStatus=LoginStatusEnum.runnable;
        }
	}

	/**
	 * 构建登陆响应报文
	 * @param responseCode
	 * @return
	 */
	private Message builderLoginResponse(int responseCode) {
		Message response=new Message(JT809Constants.UP_CONNECT_RSP);
	    response.setMsgGesscenterId(msgGesscenterId);
        byte[] versioag = {0,0,1};
        response.setVersionFlag(versioag);
        response.setEncryptFlag(0L);
        response.setEncryptKey(123L);
        ByteBuf body=ByteBufPool.BYTE_BUF_POOL.buffer(5);
        body.writeByte(responseCode);
        body.writeInt(responseCode);
        response.setMsgBody(body);
        return response;
        
		
	}

	private int validateLogin(Message msg,LoginRequest request) {
		//判断接入码
		if(this.msgGesscenterId!=msg.getMsgGesscenterId()) {
			return JT809Constants.UP_CONNECT_RSP_ERROR_02;
		}
		//判断用户是否存在
		if(1234!=request.getUserId()) {
			return JT809Constants.UP_CONNECT_RSP_ERROR_03;
		}
		//判断密码是否正确
		if(!"VtJtt879".equals(request.getPassword())) {
			return JT809Constants.UP_CONNECT_RSP_ERROR_04;
		}
		//判断ip地址是否正确
        LOGGER.info("传入的ip地址和端口为：{}，{},尚未校验",request.getDownLinkIp(),request.getDownLinkPort());
		return JT809Constants.UP_CONNECT_RSP_SUCCESS;
	}


	private class ConnectionTerminator implements Runnable{
        ChannelHandlerContext ctx;
        public ConnectionTerminator(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (loginStatus!=LoginStatusEnum.runnable) {
            	LOGGER.info("接入码为：{} 的下级平台，在连接5秒内没有登陆成功，连接断开",msgGesscenterId);
                ctx.close();
            }  
        }
    } 

}
