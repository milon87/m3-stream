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

package com.flazr.rtmp;

import android.util.Log;

import com.flazr.rtmp.message.ChunkSize;
import com.flazr.rtmp.message.Control;
import java.util.Iterator;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;

@ChannelPipelineCoverage("one")
public class RtmpEncoder extends SimpleChannelDownstreamHandler {

    private int chunkSize = 128;    
    private RtmpHeader[] channelPrevHeaders = new RtmpHeader[RtmpHeader.MAX_CHANNEL_ID];    

    private void clearPrevHeaders() {
    	Log.d(this.getClass().getName(), "clearing prev stream headers");
        channelPrevHeaders = new RtmpHeader[RtmpHeader.MAX_CHANNEL_ID];
    }

    @Override
    public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) {
        final RtmpMessage message = (RtmpMessage) e.getMessage();
        final Iterator<ChannelBuffer> chunks = encode(message);
        while(chunks.hasNext() && ctx.getChannel().isWritable()) {
            Channels.write(ctx, e.getFuture(), chunks.next());
        }
    }

    public Iterator<ChannelBuffer> encode(final RtmpMessage message) {
        final ChannelBuffer in = message.encode();
        final RtmpHeader header = message.getHeader();
        if(header.isChunkSize()) {
            final ChunkSize csMessage = (ChunkSize) message;
            Log.d(this.getClass().getName(), "encoder new chunk size: "+ csMessage);
            chunkSize = csMessage.getChunkSize();
        } else if(header.isControl()) {
            final Control control = (Control) message;
            if(control.getType() == Control.Type.STREAM_BEGIN) {
                clearPrevHeaders();
            }
        }
        final int channelId = header.getChannelId();
        header.setSize(in.readableBytes());
        final RtmpHeader prevHeader = channelPrevHeaders[channelId];       
        if(prevHeader != null // first stream message is always large
                && header.getStreamId() > 0 // all control messages always large
                && header.getTime() > 0) { // if time is zero, always large
            if(header.getSize() == prevHeader.getSize()) {
                header.setHeaderType(RtmpHeader.Type.SMALL);
            } else {
                header.setHeaderType(RtmpHeader.Type.MEDIUM);
            }
            final int deltaTime = header.getTime() - prevHeader.getTime();
            if(deltaTime < 0) {
            	Log.w(this.getClass().getName(), "negative time: "+ header);
                header.setDeltaTime(0);
            } else {
                header.setDeltaTime(deltaTime);
            }
        } // else will be default LARGE
        channelPrevHeaders[channelId] = header;        
        Log.d(this.getClass().getName(), ">> "+ message);
        return iterator(header, in);
    }

    private Iterator<ChannelBuffer> iterator(final RtmpHeader header, final ChannelBuffer in) {

        return new Iterator<ChannelBuffer>() {
            
            private byte[] headerMarker;
            private boolean first = true;

            @Override
            public boolean hasNext() {
                return in.readable();
            }

            @Override
            public ChannelBuffer next() {
                final int size = Math.min(chunkSize, in.readableBytes());
                final ChannelBuffer out;
                if(first) {
                    out = ChannelBuffers.buffer(size + RtmpHeader.MAX_ENCODED_SIZE);
                    header.encode(out);
                    first = false;
                } else {
                    out = ChannelBuffers.buffer(size + 1);
                    if(headerMarker == null) {
                        headerMarker = header.getTinyHeader(); // cache for performance
                    }
                    out.writeBytes(headerMarker);
                }
                in.readBytes(out, size);
                return out;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove() not supported");
            }

        };
    }

}
