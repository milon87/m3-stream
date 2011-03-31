package com.yay.web.controller;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xuggle.ferry.IBuffer;
import com.xuggle.xuggler.Converter;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat.Type;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;

import java.io.IOException;


public class VMuktiController implements Controller {
	protected final Log log = LogFactory.getLog(getClass());
	
	//media parameters
	private static final int IN_HEIGHT = 320;
	private static final int IN_WIDTH = 480;
	private static final int OUT_HEIGHT = 320;
	private static final int OUT_WIDTH = 480;
	private static final int FPS = 15;

	//decoders
	private IStreamCoder videoDecoder;
	private IStreamCoder audioDecoder;

	//encoders
	private IContainer outContainer;
	private IContainerFormat outContainerFormat;
	private IStreamCoder outVideoCoder;
	private IStreamCoder outAudioCoder;
	private IStream outVideoStream;
	private IStream outAudioStream;

	private IVideoResampler videoResampler;
	private IAudioResampler audioResampler;
	
	//duration of 1 video frame
	int tsInterval = 1000/FPS;
	//last video presentation timestamp
	long lastVideoPts = 0;

	IPacket packet_out = IPacket.make();
	int lastPos = 0;
	int lastPos_out = 0;

	IVideoPicture videoPicture = null;
	IVideoPicture videoPicture_resampled = null;
	IAudioSamples audioSamples = null;
	IAudioSamples audioSamples_resampled = null;
	int audioFrameLength = 32;
	byte[] audioFrame = new byte[audioFrameLength];

	int frameType=0;
	int frameLength = 0;
	byte[] frame = new byte[0];
	int videoFrameCnt=0;
	int audioFrameCnt=0;
	int offset = 0;
	IBuffer iBuffer = null;
	IPacket packet = null;
	int retVal = 0;
	
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String urlOut="rtmp://localhost/oflaDemo/myStream";
		createVideoDecoder();
		createAudioDecoder();
		createOutput(urlOut);
		
		while(getNextFrame(frameType, frameLength, frame)) {
		    if(frameType == 0) {  //video frame
		        lastVideoPts += tsInterval;

		        iBuffer = IBuffer.make(null, frame, 0, frameLength);
		        packet = IPacket.make(iBuffer);
		        packet.setKeyPacket(true);
		        packet.setTimeBase(IRational.make(1,1000));
		        packet.setDuration(tsInterval);
		        packet.setPts(lastVideoPts);
		        packet.setDts(lastVideoPts);
		        packet.setPosition(lastPos);
		        lastPos += frameLength;
		        videoFrameCnt++;
		        int pksz = packet.getSize();
		        packet.setComplete(true, pksz);

		        int bytesDecoded = videoDecoder.decodeVideo(videoPicture, packet, offset);
		        if (bytesDecoded < 0) {
		        	log.error("error decoding video frame");
		            continue;
		        }

		        //resample  
		        videoPicture_resampled = IVideoPicture.make(videoResampler.getOutputPixelFormat(), videoResampler.getOutputWidth(), videoResampler.getOutputHeight());
		        videoResampler.resample(videoPicture_resampled, videoPicture);

		        retVal = outVideoCoder.encodeVideo(packet_out, videoPicture_resampled, 0);
		        if(retVal < 0) {
		            log.error("Could not encode video");
		            videoPicture_resampled.delete();
		            continue;
		        }
		   
		        videoPicture_resampled.delete();
		        packet_out.setDuration(tsInterval);
		        packet_out.setDts(lastVideoPts);
		        packet_out.setPts(lastVideoPts);
		        packet_out.setPosition(lastPos_out);
		        lastPos_out+=packet_out.getSize();
		        packet_out.setKeyPacket(true);

		        if(packet_out.isComplete()) {             
		            retVal = outContainer.writePacket(packet_out, true);
		        }          
		    }
		    if(frameType == 1) {  //audio frame
		        //we always have 32 bytes/sample                           
		        int numPack = frameLength / audioFrameLength;
		        int pos = 0;
		        for(int i = 0; i< numPack; i++) {
		            System.arraycopy(frame, pos, audioFrame, 0, audioFrameLength);
		            pos+=32;
		            iBuffer = IBuffer.make(null, audioFrame, 0, audioFrameLength);
		            packet = IPacket.make(iBuffer);
		            packet.setKeyPacket(true);
		            packet.setTimeBase(IRational.make(1,1000));
		            packet.setDuration(20);
		            packet.setDts(audioFrameCnt*20);
		            packet.setPts(audioFrameCnt*20);
		            packet.setStreamIndex(1);
		            packet.setPosition(lastPos);
		            lastPos+=audioFrameLength;
		            audioFrameCnt++;

		            int pksz = packet.getSize();
		            packet.setComplete(true, pksz);

		            /*
		            * A packet can actually contain multiple samples
		            */
		            offset = 0;
		            while(offset < packet.getSize()) {
		                int bytesDecoded = audioDecoder.decodeAudio(audioSamples, packet, offset);
		                if(bytesDecoded < 0)
		                    throw new RuntimeException("got error decoding audio ");
		                offset += bytesDecoded;
		                if(audioSamples.isComplete()) {
		                    audioResampler.resample(audioSamples_resampled, audioSamples, audioSamples.getNumSamples());
		                    audioSamples_resampled.setPts(Global.NO_PTS);
		                    int samplesConsumed = 0;
		                    while(samplesConsumed < audioSamples_resampled.getNumSamples()) {
		                        retVal = outAudioCoder.encodeAudio(packet_out, audioSamples_resampled, samplesConsumed);
		                        if(retVal <= 0)
		                            throw new RuntimeException("Could not encode audio");
		                        samplesConsumed += retVal;
		                        if(packet_out.isComplete()) {
		                            packet_out.setPosition(lastPos_out);
		                            packet_out.setStreamIndex(1);
		                            lastPos_out+=packet_out.getSize();
		                            retVal = outContainer.writePacket(packet_out);
		                        }
		                    }
		                }
		            }
		        }
		    }
		}
		
		log.info("Return View");
		return new ModelAndView("/hello.jsp");
	}
	
	/**
	 * getNextFrame which should return a byte array containing the next available frame, from which we construct an “incoming” packet on-the-fly	
	 * @return
	 */
	private boolean getNextFrame(int frameType, int frameLength, byte[] frame) {
		return true;
	}

	private boolean createVideoDecoder() {
	    videoDecoder =IStreamCoder.make(IStreamCoder.Direction.DECODING);
	    videoDecoder.setCodec(ICodec.ID.CODEC_ID_MPEG4);
	    videoDecoder.setPixelType(Type.YUV420P);
	    videoDecoder.setHeight(IN_HEIGHT);
	    videoDecoder.setWidth(IN_WIDTH);
	    videoDecoder.setTimeBase(IRational.make(1, FPS));
	    videoDecoder.setFrameRate(IRational.make(FPS, 1));
	    if (videoDecoder.open() < 0)
	        return false;
	    return true;
	}

	private boolean createAudioDecoder() {
	    audioDecoder = IStreamCoder.make(IStreamCoder.Direction.DECODING);
	    audioDecoder.setCodec(ICodec.ID.CODEC_ID_AMR_NB);
	    audioDecoder.setSampleRate(8000);
	    audioDecoder.setChannels(1);
	    audioDecoder.setTimeBase(IRational.make(1,1000));
	    if (audioDecoder.open() < 0)
	        return false;
	    return true;
	}

	private boolean createOutput(String urlOut) {
	    outContainer = IContainer.make();
	    outContainerFormat = IContainerFormat.make();
	    outContainerFormat.setOutputFormat("flv", urlOut, null);
	    int retVal = outContainer.open(urlOut, IContainer.Type.WRITE, outContainerFormat);
	    if (retVal < 0) {
	    	log.info("Could not open output container");
	        return false;
	    }

	    outVideoStream = outContainer.addNewStream(0);
	    outVideoCoder = outVideoStream.getStreamCoder();
	    outVideoCoder.setCodec(ICodec.ID.CODEC_ID_FLV1);
	    outVideoCoder.setWidth(OUT_WIDTH);
	    outVideoCoder.setHeight(OUT_HEIGHT);
	    outVideoCoder.setPixelType(Type.YUV420P);
	    outVideoCoder.setNumPicturesInGroupOfPictures(12);
	    outVideoCoder.setProperty("nr", 0);
	    outVideoCoder.setProperty("mbd",0);
	    outVideoCoder.setTimeBase(IRational.make(1, FPS));
	    outVideoCoder.setFrameRate(IRational.make(FPS, 1));
	    outVideoCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
	    retVal = outVideoCoder.open();
	    if (retVal < 0) {
	        log.info("Could not open video coder");
	        return false;
	    }

	    outAudioStream = outContainer.addNewStream(1);
	    outAudioCoder = outAudioStream.getStreamCoder();
	    outAudioCoder.setCodec(ICodec.ID.CODEC_ID_MP3);
	    outAudioCoder.setSampleRate(11025);
	    outAudioCoder.setChannels(1);
	    retVal = outAudioCoder.open();
	    if (retVal < 0) {
	        log.info("Could not open audio coder");
	        return false;
	    }

	    //resamplers for both video and audio:
	    videoResampler = IVideoResampler.make(OUT_WIDTH, OUT_HEIGHT, Type.YUV420P, IN_WIDTH, IN_HEIGHT, Type.YUV420P);
	    audioResampler = IAudioResampler.make(1, // output channels
	                    1, // input channels
	                    11025, // new sample rate
	                    8000 // old sample rate
	    );

	    retVal = outContainer.writeHeader();
	    if (retVal < 0) {
	        log.info("Could not write output FLV header: ");
	        return false;
	    }
	    return true;
	}

}