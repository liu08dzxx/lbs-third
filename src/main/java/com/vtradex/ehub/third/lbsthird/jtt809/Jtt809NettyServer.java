package com.vtradex.ehub.third.lbsthird.jtt809;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.third.lbsthird.jtt809.entity.Message;
import com.vtradex.ehub.third.lbsthird.util.ProperitiesUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class Jtt809NettyServer {
	
	public Jtt809NettyServer(int msgGesscenterId) {
		this.msgGesscenterId=msgGesscenterId;
		this.port=ProperitiesUtil.getInt("jtt809."+msgGesscenterId+".port");
	}
	
	/**
	 * 发放的下级平台接入码
	 */
	private int msgGesscenterId;
	
	private int port;
	
	public static Logger LOGGER=LoggerFactory.getLogger(Jtt809NettyServer.class);

	
	public void init() {
		//粘包分隔符
		ByteBuf delimiter = Unpooled.buffer(1);
		delimiter.writeByte(Message.MSG_TALL);
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		NioEventLoopGroup boos = new NioEventLoopGroup();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		serverBootstrap.group(boos, worker).channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
					protected void initChannel(NioSocketChannel ch) {
						ch.pipeline().addLast(new ReadTimeoutHandler(300));//5分钟没有消息断开
						ch.pipeline().addLast(new DelimiterBasedFrameDecoder(5000, delimiter));
						ch.pipeline().addLast(new Jtt809Decoder());//该处理器将信息转换成message对象
						ch.pipeline().addLast(new Jtt809Handler(msgGesscenterId));
					}
				}).bind(port);//32965
	}

}
