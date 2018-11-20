/*******************************************************************************
 * sdr-trunk
 * Copyright (C) 2014-2018 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by  the Free Software Foundation, either version 3 of the License, or  (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without even the implied
 * warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License  along with this program.
 * If not, see <http://www.gnu.org/licenses/>
 *
 ******************************************************************************/
package io.github.dsheirer.record.binary;

import io.github.dsheirer.dsp.filter.channelizer.ContinuousReusableBufferProcessor;
import io.github.dsheirer.module.Module;
import io.github.dsheirer.sample.Listener;
import io.github.dsheirer.sample.buffer.IReusableByteBufferListener;
import io.github.dsheirer.sample.buffer.ReusableByteBuffer;
import io.github.dsheirer.util.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Binary recorder module for recording demodulated bit/byte streams
 *
 * Designed to record reusable byte buffers generated by a channel decoder.
 *
 * The contents of the file are the raw bytes as demodulated by the decoder with
 * no header or timestamps, other than the timestamp included in the filename.
 */
public class BinaryRecorder extends Module implements IReusableByteBufferListener
{
    private final static Logger mLog = LoggerFactory.getLogger(BinaryRecorder.class);
    private static final int MAX_RECORDING_BYTE_SIZE = 524288;  //500 kB

    private ContinuousReusableBufferProcessor<ReusableByteBuffer> mBufferProcessor =
        new ContinuousReusableBufferProcessor<>(500, 50);

    private AtomicBoolean mRunning = new AtomicBoolean();
    private Path mBaseRecordingPath;
    private String mRecordingIdentifier;
    private BinaryWriter mBinaryWriter = new BinaryWriter();
    private int mBytesRecordedCounter;
    private int mBitRate;

    /**
     * Constructs a binary recorder.
     *
     * @param baseRecordingPath where the recording should be created
     * @param recordingIdentifier to include in the recording file name.
     * @param bitRate to include as a value in the recording file name
     */
    public BinaryRecorder(Path baseRecordingPath, String recordingIdentifier, int bitRate)
    {
        mBaseRecordingPath = baseRecordingPath;
        mRecordingIdentifier = recordingIdentifier;
        mBufferProcessor.setListener(mBinaryWriter);
        mBitRate = bitRate;
    }

    public void start()
    {
        if(mRunning.compareAndSet(false, true))
        {
            try
            {
                mBinaryWriter.start(getRecordingPath());
                mBufferProcessor.start();
            }
            catch(IOException io)
            {
                mLog.error("Error starting binary baseband recorder", io);
            }
        }
    }

    public void stop()
    {
        if(mRunning.compareAndSet(true, false))
        {
            if(mBufferProcessor != null)
            {
                mBufferProcessor.flushAndStop();
                mBufferProcessor.setListener(null);

                try
                {
                    mBinaryWriter.stop();
                }
                catch(IOException ioe)
                {
                    mLog.error("Error stopping binary recorder", ioe);
                }
            }
        }
    }

    /**
     * Constructs a recording path/file name using the base path and a timestamped
     * filename with the recording identifier.
     *
     * @return path to use for the recording file
     */
    private Path getRecordingPath()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TimeStamp.getTimeStamp("_"));
        sb.append("_");
        sb.append(mBitRate).append("BPS_");
        sb.append(mRecordingIdentifier.trim());
        sb.append(".bits");

        return mBaseRecordingPath.resolve(sb.toString());
    }

    @Override
    public Listener<ReusableByteBuffer> getReusableByteBufferListener()
    {
        return mBufferProcessor;
    }

    @Override
    public void dispose()
    {
        stop();
    }

    @Override
    public void reset()
    {
    }

    /**
     * Binary writer implementation for reusable byte buffers delivered from buffer processor
     */
    public class BinaryWriter implements Listener<List<ReusableByteBuffer>>
    {
        private Path mCurrentPath;
        private WritableByteChannel mWritableByteChannel;

        public void start(Path path) throws IOException
        {
            synchronized(this)
            {
                mCurrentPath = path;
                mWritableByteChannel = Files.newByteChannel(path,
                    EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
//                mLog.info("Binary (bitstream) recording started: " + mCurrentPath.toString());
            }
        }

        public void stop() throws IOException
        {
            synchronized(this)
            {
                if(mWritableByteChannel != null)
                {
                    mWritableByteChannel.close();
                }

                mWritableByteChannel = null;
                mCurrentPath = null;
            }
        }

        /**
         * Closes the current recording and starts a new recording.  This method is normally used
         * when the current recording size has reached the maximum threshold.
         */
        private void cycleRecording()
        {
            synchronized(this)
            {
                try
                {
                    if(mWritableByteChannel != null)
                    {
                        mWritableByteChannel.close();
                    }

                    mCurrentPath = getRecordingPath();
                    mWritableByteChannel = Files.newByteChannel(mCurrentPath,
                        EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
                    mLog.info("Binary (bitstream) recording started: " + mCurrentPath.toString());
                }
                catch(IOException ioe)
                {
                    mLog.error("Error while cycling a max-size bit stream recorder", ioe);
                }

                mBytesRecordedCounter = 0;
            }
        }

        /**
         * Primary receive method for incoming byte buffers
         * @param reusableComplexBuffers to record
         */
        @Override
        public void receive(List<ReusableByteBuffer> reusableComplexBuffers)
        {
            for(ReusableByteBuffer buffer: reusableComplexBuffers)
            {
                if(mWritableByteChannel != null)
                {
                    try
                    {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer.getSamplesCopy());
                        mBytesRecordedCounter += mWritableByteChannel.write(byteBuffer);

                        if(mBytesRecordedCounter > MAX_RECORDING_BYTE_SIZE)
                        {
                            cycleRecording();
                        }
                    }
                    catch(IOException ioe)
                    {
                        mLog.error("Error recording demodulated bits to file [" +
                            (mCurrentPath != null ? mCurrentPath.toString() : "no file") + "] - stopping recorder");

                        try
                        {
                            stop();
                        }
                        catch(IOException ioe2)
                        {
                            mLog.error("Error stopping recorder after write error", ioe2.getLocalizedMessage());
                        }
                    }
                }

                buffer.decrementUserCount();
            }
        }
    }
}
