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

package io.github.dsheirer.module.decode.p25.message.lc.motorola;

import io.github.dsheirer.bits.BinaryMessage;
import io.github.dsheirer.identifier.IIdentifier;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25PatchGroup;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25ToTalkgroup;

import java.util.ArrayList;
import java.util.List;

public class LCMotorolaPatchGroupAdd extends MotorolaLinkControlWord
{
    private static final int[] PATCH_GROUP = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
    private static final int[] PATCHED_GROUP_1 = {32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47};
    private static final int[] PATCHED_GROUP_2 = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63};

    private APCO25PatchGroup mPatchGroup;
    private IIdentifier mPatchedGroup1;
    private IIdentifier mPatchedGroup2;
    private List<IIdentifier> mIdentifiers;

    public LCMotorolaPatchGroupAdd(BinaryMessage message)
    {
        super(message);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("MOTOROLA ADD PATCH GROUP:").append(getPatchGroup());
        sb.append(" MSG:").append(getMessage().toHexString());

        return sb.toString();
    }

    /**
     * Patch Group
     */
    public IIdentifier getPatchGroup()
    {
        if(mPatchGroup == null)
        {
            mPatchGroup = APCO25PatchGroup.create(getMessage().getInt(PATCH_GROUP));
            mPatchGroup.addPatchedGroups(getPatchedGroups());
        }

        return mPatchGroup;
    }

    public List<IIdentifier> getPatchedGroups()
    {
        List<IIdentifier> patchedGroups = new ArrayList<>();

        if(hasPatchedGroup1())
        {
            patchedGroups.add(getPatchedGroup1());
        }

        if(hasPatchedGroup2())
        {
            patchedGroups.add(getPatchedGroup2());
        }

        return patchedGroups;
    }

    /**
     * Patched Group 1
     */
    public IIdentifier getPatchedGroup1()
    {
        if(mPatchedGroup1 == null)
        {
            mPatchedGroup1 = APCO25ToTalkgroup.createGroup(getMessage().getInt(PATCHED_GROUP_1));
        }

        return mPatchedGroup1;
    }

    public boolean hasPatchedGroup1()
    {
        return getMessage().getInt(PATCHED_GROUP_1) != 0 &&
            (getMessage().getInt(PATCH_GROUP) != getMessage().getInt(PATCHED_GROUP_1));
    }

    /**
     * Patched Group 2
     */
    public IIdentifier getPatchedGroup2()
    {
        if(mPatchedGroup2 == null)
        {
            mPatchedGroup2 = APCO25ToTalkgroup.createGroup(getMessage().getInt(PATCHED_GROUP_2));
        }

        return mPatchedGroup2;
    }

    public boolean hasPatchedGroup2()
    {
        return getMessage().getInt(PATCHED_GROUP_2) != 0 &&
            (getMessage().getInt(PATCH_GROUP) != getMessage().getInt(PATCHED_GROUP_2));
    }

    /**
     * List of identifiers contained in this message
     */
    @Override
    public List<IIdentifier> getIdentifiers()
    {
        if(mIdentifiers == null)
        {
            mIdentifiers = new ArrayList<>();
            mIdentifiers.add(getPatchGroup());
        }

        return mIdentifiers;
    }
}
