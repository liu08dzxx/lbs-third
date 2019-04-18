package com.vtradex.ehub.third.lbsthird.jtt809;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.JT809Constants;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.LoginResponse;
import com.vtradex.ehub.third.lbsthird.jtt809.entity.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class NettyClient {
	
	
	
    
	public static void main(String[] args) throws InterruptedException {
		int a = 0x5d;
		ByteBuf delimiter = Unpooled.buffer(1);
		delimiter.writeByte(a);
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                    	ch.pipeline().addLast(new DelimiterBasedFrameDecoder(5000, delimiter));
                    	ch.pipeline().addLast(new Jtt809Decoder());//该处理器将信息转换成message对象
                    	ch.pipeline().addLast(new SimpleChannelInboundHandler<Message>() {
							@Override
							protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
								System.out.println("接收到一条" + msg.toString());
								if(msg.getMsgId()==JT809Constants.UP_CONNECT_RSP) {
									LoginResponse response=Jtt809Util.convertLoginResponse(msg);
									System.out.println("接收到登陆响应报文："+JSON.toJSONString(response));
								}
							}
                    		
                    	});
                    }
                });
//
//        Channel channel = bootstrap.connect("110.53.222.19", 32965).channel();
        Channel channel = bootstrap.connect("127.0.0.1", 8091).channel();
        //循环到连接成功
        while(!channel.isActive()) {
        	TimeUnit.SECONDS.sleep(1);
        	continue;
        }
        //发送一条消息
        
        TimeUnit.SECONDS.sleep(2);
        Message message=new Message(JT809Constants.UP_CONNECT_REQ);
        message.setMsgGesscenterId(1001);
        byte[] versioag = {0,0,1};
        message.setVersionFlag(versioag);
        message.setEncryptFlag(1L);
        message.setEncryptKey(123L);
        ByteBuf body=Unpooled.buffer(46);
        body.writeInt(1234);
        body.writeBytes("VtJtt879".getBytes());
        byte[] ipBytesource="169.254.246.63".getBytes();
        byte[] ipByte = Jtt809Util.rightComplementByte(ipBytesource, 32);
        body.writeBytes(ipByte);
        body.writeShort(8000);
        message.setMsgBody(body);
        Jtt809Util.sendClientMessage(channel, message);
        
//        message.send(channel);
      //循环到连接被断开
        while(channel.isActive()) {
        	TimeUnit.SECONDS.sleep(1);
        	continue;
        }
        System.out.println("连接被断开：时间："+new Date());
    }
	
}
