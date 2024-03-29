/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.proxy;

import java.net.InetSocketAddress;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;

import android.util.Log;

@ChannelPipelineCoverage("one")
public class RtmpProxyHandler extends SimpleChannelUpstreamHandler {

    private final ClientSocketChannelFactory cf;
    private final String remoteHost;
    private final int remotePort;

    private volatile Channel outboundChannel;

    public RtmpProxyHandler(ClientSocketChannelFactory cf, String remoteHost, int remotePort) {
        this.cf = cf;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {        
        final Channel inboundChannel = e.getChannel();
        RtmpProxy.ALL_CHANNELS.add(inboundChannel);
        inboundChannel.setReadable(false);        
        ClientBootstrap cb = new ClientBootstrap(cf);
        cb.getPipeline().addLast("handshaker", new RtmpProxyHandshakeHandler());
        cb.getPipeline().addLast("handler", new OutboundHandler(e.getChannel()));
        ChannelFuture f = cb.connect(new InetSocketAddress(remoteHost, remotePort));
        outboundChannel = f.getChannel();
        f.addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                	Log.i(this.getClass().getName(), "connected to remote host: "+remoteHost+", port: "+ remotePort);
                    inboundChannel.setReadable(true);
                } else {                    
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        ChannelBuffer in = (ChannelBuffer) e.getMessage();
        // logger.debug(">>> [{}] {}", in.readableBytes(), ChannelBuffers.hexDump(in));
        outboundChannel.write(in);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
    	Log.i(this.getClass().getName(), "closing inbound channel");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    	Log.i(this.getClass().getName(), "inbound exception: "+ e.getCause().getMessage());
        closeOnFlush(e.getChannel());
    }

    @ChannelPipelineCoverage("one")
    private class OutboundHandler extends SimpleChannelUpstreamHandler {

        private final Channel inboundChannel;

        public OutboundHandler(Channel inboundChannel) {
        	Log.i(this.getClass().getName(), "opening outbound channel");
            this.inboundChannel = inboundChannel;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            ChannelBuffer in = (ChannelBuffer) e.getMessage();
            // logger.debug("<<< [{}] {}", in.readableBytes(), ChannelBuffers.hexDump(in));
            inboundChannel.write(in);
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        	Log.i(this.getClass().getName(), "closing outbound channel");
            closeOnFlush(inboundChannel);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        	Log.i(this.getClass().getName(), "outbound exception: "+ e.getCause().getMessage());
            closeOnFlush(e.getChannel());
        }
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isConnected()) {
            ch.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
