/*
 * ******************************************************************************
 * sdrtrunk
 * Copyright (C) 2014-2018 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * *****************************************************************************
 */

package io.github.dsheirer.module.decode.p25.message.pdu.ambtc.osp;

import io.github.dsheirer.identifier.IIdentifier;
import io.github.dsheirer.identifier.integer.channel.APCO25Channel;
import io.github.dsheirer.identifier.integer.channel.APCO25ExplicitChannel;
import io.github.dsheirer.identifier.integer.channel.IAPCO25Channel;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25FromTalkgroup;
import io.github.dsheirer.module.decode.p25.message.FrequencyBandReceiver;
import io.github.dsheirer.module.decode.p25.message.pdu.PacketSequence;
import io.github.dsheirer.module.decode.p25.message.pdu.UnconfirmedDataBlock;
import io.github.dsheirer.module.decode.p25.message.pdu.ambtc.AMBTCMessage;
import io.github.dsheirer.module.decode.p25.reference.ServiceOptions;

import java.util.ArrayList;
import java.util.List;

public class AMBTCTelephoneInterconnectChannelGrantUpdate extends AMBTCMessage implements FrequencyBandReceiver
{
    private static final int[] HEADER_SERVICE_OPTIONS = {64, 65, 66, 67, 68, 69, 70, 71};
    private static final int[] HEADER_RESERVED = {72, 73, 74, 75, 76, 77, 78, 79};
    private static final int[] BLOCK_0_DOWNLINK_FREQUENCY_BAND = {0, 1, 2, 3};
    private static final int[] BLOCK_0_DOWNLINK_CHANNEL_NUMBER = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final int[] BLOCK_0_UPLINK_FREQUENCY_BAND = {16, 17, 18, 19};
    private static final int[] BLOCK_0_UPLINK_CHANNEL_NUMBER = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
    private static final int[] BLOCK_0_CALL_TIMER = {32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47};
    private static final int[] BLOCK_0_RESERVED = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63};

    private ServiceOptions mServiceOptions;
    private IAPCO25Channel mChannel;
    private IIdentifier mAnyAddress;
    private List<IIdentifier> mIdentifiers;
    private List<IAPCO25Channel> mChannels;

    public AMBTCTelephoneInterconnectChannelGrantUpdate(PacketSequence packetSequence, int nac, long timestamp)
    {
        super(packetSequence, nac, timestamp);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessageStub());
        sb.append(" ADDR:").append(getAnyAddress());
        if(getChannel() != null)
        {
            sb.append(" CHAN:").append(getChannel());
        }
        sb.append(" CALL TIMER:").append(getCallTimer()).append("ms");
        sb.append(" SERVICE OPTIONS:").append(getServiceOptions());
        return sb.toString();
    }

    public ServiceOptions getServiceOptions()
    {
        if(mServiceOptions == null)
        {
            mServiceOptions = new ServiceOptions(getHeader().getMessage().getInt(HEADER_SERVICE_OPTIONS));
        }

        return mServiceOptions;
    }

    public long getCallTimer()
    {
        if(hasDataBlock(0))
        {
            return getDataBlock(0).getMessage().getInt(BLOCK_0_CALL_TIMER) * 100; //milliseconds
        }

        return 0;
    }

    public IIdentifier getAnyAddress()
    {
        if(mAnyAddress == null)
        {
            mAnyAddress = APCO25FromTalkgroup.createIndividual(getHeader().getMessage().getInt(HEADER_ADDRESS));
        }

        return mAnyAddress;
    }

    public boolean isExtendedChannel()
    {
        return hasDataBlock(0) &&
            (getDataBlock(0).getMessage().getInt(BLOCK_0_DOWNLINK_CHANNEL_NUMBER) !=
                getDataBlock(0).getMessage().getInt(BLOCK_0_UPLINK_CHANNEL_NUMBER));
    }

    public IAPCO25Channel getChannel()
    {
        if(mChannel == null && hasDataBlock(0))
        {
            UnconfirmedDataBlock block = getDataBlock(0);

            if(isExtendedChannel())
            {
                mChannel = APCO25ExplicitChannel.create(block.getMessage().getInt(BLOCK_0_DOWNLINK_FREQUENCY_BAND),
                    block.getMessage().getInt(BLOCK_0_DOWNLINK_CHANNEL_NUMBER),
                    block.getMessage().getInt(BLOCK_0_UPLINK_FREQUENCY_BAND),
                    block.getMessage().getInt(BLOCK_0_UPLINK_CHANNEL_NUMBER));
            }
            else
            {
                mChannel = APCO25Channel.create(block.getMessage().getInt(BLOCK_0_DOWNLINK_FREQUENCY_BAND),
                    block.getMessage().getInt(BLOCK_0_DOWNLINK_CHANNEL_NUMBER));
            }
        }

        return mChannel;
    }

    @Override
    public List<IIdentifier> getIdentifiers()
    {
        if(mIdentifiers == null)
        {
            mIdentifiers = new ArrayList<>();
            mIdentifiers.add(getAnyAddress());
        }

        return mIdentifiers;
    }

    @Override
    public List<IAPCO25Channel> getChannels()
    {
        if(mChannels == null)
        {
            mChannels = new ArrayList<>();

            if(getChannel() != null)
            {
                mChannels.add(getChannel());
            }
        }

        return mChannels;
    }
}
