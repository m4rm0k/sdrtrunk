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
import io.github.dsheirer.identifier.integer.node.APCO25System;
import io.github.dsheirer.identifier.integer.node.APCO25Wacn;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25FromTalkgroup;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25ToTalkgroup;
import io.github.dsheirer.module.decode.p25.message.pdu.PDUSequence;
import io.github.dsheirer.module.decode.p25.message.pdu.ambtc.AMBTCMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Group affiliation response
 */
public class AMBTCGroupAffiliationResponse extends AMBTCMessage
{
    private static final int[] HEADER_SOURCE_WACN = {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79};
    private static final int[] BLOCK_0_SOURCE_WACN = {0, 1, 2, 3};
    private static final int[] BLOCK_0_SOURCE_SYSTEM = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final int[] BLOCK_0_SOURCE_ID = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
        33, 34, 35, 36, 37, 38, 39};
    private static final int[] BLOCK_0_GROUP_WACN = {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55,
        56, 57, 58, 59};
    private static final int[] BLOCK_0_GROUP_SYSTEM = {60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71};
    private static final int[] BLOCK_0_GROUP_ID = {72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87};
    private static final int[] BLOCK_0_ANNOUNCEMENT_GROUP_ID = {88, 89, 90, 91, 92, 93, 94, 95};
    private static final int[] BLOCK_1_ANNOUNCEMENT_GROUP_ID = {0, 1, 2, 3, 4, 5, 6, 7};

    private IIdentifier mTargetAddress;
    private IIdentifier mSourceWacn;
    private IIdentifier mSourceSystem;
    private IIdentifier mSourceId;
    private IIdentifier mGroupWacn;
    private IIdentifier mGroupSystem;
    private IIdentifier mGroupId;
    private IIdentifier mAnnouncementGroupId;
    private List<IIdentifier> mIdentifiers;

    public AMBTCGroupAffiliationResponse(PDUSequence PDUSequence, int nac, long timestamp)
    {
        super(PDUSequence, nac, timestamp);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessageStub());
        if(getTargetAddress() != null)
        {
            sb.append(" TO:").append(getTargetAddress());
        }
        if(getSourceWacn() != null)
        {
            sb.append(" FM WACN:").append(getSourceWacn());
        }
        if(getSourceSystem() != null)
        {
            sb.append(" FM SYSTEM:").append(getSourceSystem());
        }
        if(getSourceId() != null)
        {
            sb.append(" FM ID:").append(getSourceId());
        }
        if(getGroupWacn() != null)
        {
            sb.append(" GRP WACN:").append(getGroupWacn());
        }
        if(getGroupSystem() != null)
        {
            sb.append(" GRP SYSTEM:").append(getGroupSystem());
        }
        if(getGroupId() != null)
        {
            sb.append(" GRP ID:").append(getGroupId());
        }
        if(getAnnouncementGroupId() != null)
        {
            sb.append(" ANNOUNCEMENT GRP:").append(getAnnouncementGroupId());
        }

        return sb.toString();
    }

    public IIdentifier getTargetAddress()
    {
        if(mTargetAddress == null && hasDataBlock(0))
        {
            mTargetAddress = APCO25ToTalkgroup.createIndividual(getDataBlock(0).getMessage().getInt(HEADER_ADDRESS));
        }

        return mTargetAddress;
    }

    public IIdentifier getSourceWacn()
    {
        if(mSourceWacn == null && hasDataBlock(0))
        {
            int value = getHeader().getMessage().getInt(HEADER_SOURCE_WACN);
            value <<= 4;
            value += getDataBlock(0).getMessage().getInt(BLOCK_0_SOURCE_WACN);
            mSourceWacn = APCO25Wacn.create(value);
        }

        return mSourceWacn;
    }

    public IIdentifier getSourceSystem()
    {
        if(mSourceSystem == null && hasDataBlock(0))
        {
            mSourceSystem = APCO25System.create(getDataBlock(0).getMessage().getInt(BLOCK_0_SOURCE_SYSTEM));
        }

        return mSourceSystem;
    }

    public IIdentifier getSourceId()
    {
        if(mSourceId == null && hasDataBlock(0))
        {
            mSourceId = APCO25FromTalkgroup.createIndividual(getDataBlock(0).getMessage().getInt(BLOCK_0_SOURCE_ID));
        }

        return mSourceId;
    }

    public IIdentifier getGroupWacn()
    {
        if(mGroupWacn == null && hasDataBlock(0))
        {
            mGroupWacn = APCO25Wacn.create(getDataBlock(0).getMessage().getInt(BLOCK_0_GROUP_WACN));
        }

        return mGroupWacn;
    }

    public IIdentifier getGroupSystem()
    {
        if(mGroupSystem == null && hasDataBlock(0))
        {
            mGroupSystem = APCO25System.create(getDataBlock(0).getMessage().getInt(BLOCK_0_GROUP_SYSTEM));
        }

        return mGroupSystem;
    }

    public IIdentifier getGroupId()
    {
        if(mGroupId == null && hasDataBlock(0))
        {
            mGroupId = APCO25ToTalkgroup.createGroup(getDataBlock(0).getMessage().getInt(BLOCK_0_GROUP_ID));
        }

        return mGroupId;
    }

    public IIdentifier getAnnouncementGroupId()
    {
        if(mAnnouncementGroupId == null && hasDataBlock(0) && hasDataBlock(1))
        {
            int id = getDataBlock(0).getMessage().getInt(BLOCK_0_ANNOUNCEMENT_GROUP_ID);
            id <<= 8;
            id += getDataBlock(1).getMessage().getInt(BLOCK_1_ANNOUNCEMENT_GROUP_ID);

            mAnnouncementGroupId = APCO25ToTalkgroup.createGroup(id);
        }

        return mAnnouncementGroupId;
    }


    @Override
    public List<IIdentifier> getIdentifiers()
    {
        if(mIdentifiers == null)
        {
            mIdentifiers = new ArrayList<>();
            mIdentifiers.add(getTargetAddress());
            if(getSourceWacn() != null)
            {
                mIdentifiers.add(getSourceWacn());
            }
            if(getSourceSystem() != null)
            {
                mIdentifiers.add(getSourceSystem());
            }
            if(getSourceId() != null)
            {
                mIdentifiers.add(getSourceId());
            }
            if(getTargetAddress() != null)
            {
                mIdentifiers.add(getTargetAddress());
            }
            if(getGroupWacn() != null)
            {
                mIdentifiers.add(getGroupWacn());
            }
            if(getGroupSystem() != null)
            {
                mIdentifiers.add(getGroupSystem());
            }
            if(getGroupId() != null)
            {
                mIdentifiers.add(getGroupId());
            }
            if(getAnnouncementGroupId() != null)
            {
                mIdentifiers.add(getAnnouncementGroupId());
            }
        }

        return mIdentifiers;
    }
}