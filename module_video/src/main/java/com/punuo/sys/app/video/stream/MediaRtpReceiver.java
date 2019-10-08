package com.punuo.sys.app.video.stream;

import android.util.Log;

import com.punuo.sip.H264Config;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

/**
 * Created by han.chen.
 * Date on 2019-09-18.
 **/
public class MediaRtpReceiver implements RTPAppIntf {
    private static final int VIDEO_PAY_LOAD_TYPE = 98;
    private static final int VOICE_PAY_LOAD_TYPE = 69;

    private final byte H264_STREAM_HEAD[] = {0x00, 0x00, 0x00, 0x01};
    private RTPSession mRTPSession;
    private StreamBuf mStreamBuf;
    private Participant mParticipant;
    private byte tempNal[] = new byte[100000];
    private int tempNalLen = 0;
    private int preSeq;
    private boolean isPacketLost = true;
    private SendActivePacket mSendActivePacket;

    public MediaRtpReceiver(String networkAddress, int remoteRtpPort) throws SocketException {
        DatagramSocket rtpSocket = new DatagramSocket();
        mRTPSession = new RTPSession(rtpSocket, null);
        mRTPSession.RTPSessionRegister(this, null, null);
        mParticipant = new Participant(networkAddress, remoteRtpPort, remoteRtpPort + 1);
        mRTPSession.addParticipant(mParticipant);
        mRTPSession.naivePktReception(false);
        mRTPSession.frameReconstruction(false);
        mStreamBuf = new StreamBuf(100, 5);
//        VideoInfo.track = new AudioTrack(
//                AudioManager.STREAM_MUSIC,
//                samp_rate,
//                AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                maxjitter,
//                AudioTrack.MODE_STREAM
//        );
//        VideoInfo.track.play();
        //开启视频心跳包活
        mSendActivePacket = new SendActivePacket();
        mSendActivePacket.startThread();
    }

    @Override
    public void receiveData(DataFrame frame, Participant participant) {
        if (frame.payloadType() == VIDEO_PAY_LOAD_TYPE) {
            StreamBufNode rtpFrameNode = new StreamBufNode(frame);
            mStreamBuf.addToBufBySeq(rtpFrameNode);
            if (mStreamBuf.isReady()) {
                StreamBufNode streamBufNode = mStreamBuf.getFromBuf();
                int seqNum = streamBufNode.getSeqNum();
                byte[] data = streamBufNode.getDataFrame().getConcatenatedData();

                int len = streamBufNode.getDataFrame().getTotalLength();
                getNalDm365(data, seqNum, len);
                MediaSample.getInstance().setMediaStatus(2);
                Log.i("MediaRtpReceiver", "len:" + len + "  seqNum:" + seqNum);
            }
        } else if (frame.payloadType() == VOICE_PAY_LOAD_TYPE) {
//            byte[] audioBuffer = new byte[frameSizeG711];
//            short[] audioData = new short[frameSizeG711];
//            audioBuffer = frame.getConcatenatedData();
//            G711.ulaw2linear(audioBuffer, audioData, frameSizeG711);
//            VideoInfo.track.write(audioData, 0, frameSizeG711);
        }
    }
    @Override
    public void userEvent(int type, Participant[] participant) {

    }

    //移除当前监听的端口
    public void removeParticipant() {
        mRTPSession.removeParticipant(mParticipant);
    }

    @Override
    public int frameSize(int payloadType) {
        return 1;
    }

    public void getNalDm365(byte[] data, int seqNum, int len) {
        switch (frameParseDm365(data)) {
            case MediaSample.TOTAL_PAGE:
                Log.i("MediaRtpReceiver", "getNalDm365: 整包");
                addCompleteRtpPacketToTemp(data, seqNum, len);
                copyFromTempToNal();
                isPacketLost = false;
                break;
            case MediaSample.FIRST_PAGE:
                Log.i("MediaRtpReceiver", "getNalDm365: 首包");
                addFirstRtpPacketToTemp(data, seqNum, len);
                isPacketLost = false;
                break;
            case MediaSample.CENTER_PAGE:
                Log.i("MediaRtpReceiver", "getNalDm365: 中包");
                if (!isPacketLost) {
                    if (preSeq + 1 == seqNum) {
                        addMiddleRtpPacketToTemp(data, seqNum, len);
                    } else {
                        jumpNal(seqNum);
                    }
                }
                break;
            case MediaSample.LAST_PAGE:
                Log.i("MediaRtpReceiver", "getNalDm365: 末包");
                if (!isPacketLost) {
                    if (preSeq + 1 == seqNum) {
                        addLastRtpPacketToTemp(data, seqNum, len);
                        copyFromTempToNal();
                    } else {
                        jumpNal(seqNum);
                    }
                } else {
                    isPacketLost = false;
                }
                break;
        }
    }

    public int frameParseDm365(byte[] data) {
        //先判断是否是分片
        if ((data[0] & 0x1f) == 28 || (data[0] & 0x1f) == 29) {
            if ((data[1] & 0xe0) == 0x80) {
                //分片首包
                return MediaSample.FIRST_PAGE;
            } else if ((data[1] & 0xe0) == 0x00) {
                //分片中包
                return MediaSample.CENTER_PAGE;
            } else {
                //分片末包
                return MediaSample.LAST_PAGE;
            }
        } else {
            //单包
            return MediaSample.TOTAL_PAGE;
        }
    }

    private void addCompleteRtpPacketToTemp(byte[] data, int seqNum, int len) {
        tempNal = new byte[100000];
        tempNal[0] = H264_STREAM_HEAD[0];
        tempNal[1] = H264_STREAM_HEAD[1];
        tempNal[2] = H264_STREAM_HEAD[2];
        tempNal[3] = H264_STREAM_HEAD[3];
        System.arraycopy(data, 0, tempNal, 4, len);
        tempNalLen = len + 4;
        preSeq = seqNum;
    }

    private void addFirstRtpPacketToTemp(byte[] data, int seqNum, int len) {
        tempNal = new byte[100000];
        tempNal[0] = H264_STREAM_HEAD[0];
        tempNal[1] = H264_STREAM_HEAD[1];
        tempNal[2] = H264_STREAM_HEAD[2];
        tempNal[3] = H264_STREAM_HEAD[3];
        System.arraycopy(data, 2, tempNal, 4, len - 2);
        tempNalLen = len + 2;
        preSeq = seqNum;
    }

    private void addMiddleRtpPacketToTemp(byte[] data, int seqNum, int len) {
        System.arraycopy(data, 2, tempNal, tempNalLen, len - 2);
        tempNalLen += len - 2;
        preSeq = seqNum;
    }

    private void addLastRtpPacketToTemp(byte[] data, int seqNum, int len) {
        System.arraycopy(data, 2, tempNal, tempNalLen, len - 2);
        tempNalLen += len - 2;
        preSeq = seqNum;
    }

    private void copyFromTempToNal() {
        MediaSample.getInstance().setNalBuf(tempNal, tempNalLen);
    }

    private void jumpNal(int seqNum) {
        MediaSample.getInstance().jumpNal();
        preSeq = seqNum;
        isPacketLost = true;
    }

    private void sendActivePacket(byte[] msg) {
        mRTPSession.payloadType(0x7a);
        for (int i = 0; i < 2; i++) {
            mRTPSession.sendData(msg);
        }
    }

    public void onDestroy() {
        mSendActivePacket.stopThread();
        mRTPSession.endSession();
        removeParticipant();
    }

    class SendActivePacket extends Thread {
        private boolean running = false;
        private byte[] msg = new byte[20];

        public SendActivePacket() {
            msg[0] = 0x00;
            msg[1] = 0x01;
            msg[2] = 0x00;
            msg[3] = 0x10;
            System.arraycopy(H264Config.getMagic(), 0, msg, 4, 16);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Log.d("SendActivePacket", "run: " + Thread.currentThread().getId());
                    sendActivePacket(msg);
                    sleep(20 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void startThread() {
            running = true;
            super.start();
        }

        public void stopThread() {
            running = false;
        }
    }
}
