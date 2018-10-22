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

package io.github.dsheirer.module.decode.p25.message.pdu.umbtc.isp;

import io.github.dsheirer.identifier.IIdentifier;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25FromTalkgroup;
import io.github.dsheirer.identifier.string.APCO25TelephoneNumber;
import io.github.dsheirer.message.IBitErrorProvider;
import io.github.dsheirer.module.decode.p25.message.pdu.PacketSequence;
import io.github.dsheirer.module.decode.p25.message.pdu.umbtc.UMBTCMessage;
import io.github.dsheirer.module.decode.p25.reference.Digit;
import io.github.dsheirer.module.decode.p25.reference.ServiceOptions;

import java.util.ArrayList;
import java.util.List;

public class UMBTCTelephoneInterconnectRequestExplicitDialing extends UMBTCMessage implements IBitErrorProvider
{
    private static final int[] BLOCK_0_DIGIT_COUNT = {8, 9, 10, 11, 12, 13, 14, 15};
    private static final int[] BLOCK_0_SERVICE_OPTIONS = {16, 17, 18, 19, 20, 21, 22, 23};

    private ServiceOptions mServiceOptions;
    private IIdentifier mSourceAddress;
    private IIdentifier mTelephoneNumber;
    private List<IIdentifier> mIdentifiers;

    public UMBTCTelephoneInterconnectRequestExplicitDialing(PacketSequence packetSequence, int nac, long timestamp)
    {
        super(packetSequence, nac, timestamp);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessageStub());
        sb.append(" FM:").append(getSourceAddress());
        if(getTelephoneNumber() != null)
        {
            sb.append(" TO:").append(getTelephoneNumber());
        }
        if(getServiceOptions() != null)
        {
            sb.append(" SERVICE OPTIONS:").append(getServiceOptions());
        }

        return sb.toString();
    }

    public ServiceOptions getServiceOptions()
    {
        if(mServiceOptions == null && hasDataBlock(0))
        {
            ;
        }
        {
            mServiceOptions = new ServiceOptions(getDataBlock(0).getMessage().getInt(BLOCK_0_SERVICE_OPTIONS));
        }

        return mServiceOptions;
    }

    public IIdentifier getSourceAddress()
    {
        if(mSourceAddress == null)
        {
            mSourceAddress = APCO25FromTalkgroup.createIndividual(getHeader().getMessage().getInt(HEADER_ADDRESS));
        }

        return mSourceAddress;
    }

    public IIdentifier getTelephoneNumber()
    {
        if(mTelephoneNumber == null && hasDataBlock(0))
        {
            int digitCount = getDataBlock(0).getMessage().getInt(BLOCK_0_DIGIT_COUNT);
            List<Integer> digits = new ArrayList<>();

            for(int x = 1; x <= digitCount; x++)
            {
                digits.add(getDigit(x));
            }

            mTelephoneNumber = APCO25TelephoneNumber.create(Digit.decode(digits));
        }

        return mTelephoneNumber;
    }

    /**
     * Returns the specified digit.
     *
     * @param digit in range 1 - 34
     * @return specified digit or Digit.UNKNOWN
     */
    private int getDigit(int digit)
    {
        if(digit <= 18)  //Block 0
        {
            if(hasDataBlock(0))
            {
                int startIndex = 24 + ((digit - 1) * 4);
                return getDataBlock(0).getMessage().getInt(startIndex, startIndex + 3);
            }
        }
        else if(digit <= 34)             //Block 1
        {
            if(hasDataBlock(1))
            {
                int startIndex = 0 + ((digit - 19) * 4);
                return getDataBlock(0).getMessage().getInt(startIndex, startIndex + 3);
            }
        }

        return -1;
    }

    @Override
    public List<IIdentifier> getIdentifiers()
    {
        if(mIdentifiers == null)
        {
            mIdentifiers = new ArrayList<>();
            mIdentifiers.add(getSourceAddress());
            if(getTelephoneNumber() != null)
            {
                mIdentifiers.add(getTelephoneNumber());
            }
        }

        return mIdentifiers;
    }
}
