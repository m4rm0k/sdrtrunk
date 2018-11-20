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

package io.github.dsheirer.preference.identifier.talkgroup;

import io.github.dsheirer.identifier.talkgroup.TalkgroupIdentifier;
import io.github.dsheirer.preference.IntegerFormat;

public class APCO25TalkgroupFormatter extends IntegerFormatter
{
    public static final int GROUP_DECIMAL_WIDTH = 5;
    public static final int UNIT_DECIMAL_WIDTH = 8;
    public static final int GROUP_HEXADECIMAL_WIDTH = 4;
    public static final int UNIT_HEXADECIMAL_WIDTH = 6;

    public static String format(TalkgroupIdentifier identifier, IntegerFormat format, boolean fixedWidth)
    {
        if(fixedWidth)
        {
            switch(format)
            {
                case DECIMAL:
                    return toDecimal(identifier.getValue(),
                        (identifier.isGroup() ? GROUP_DECIMAL_WIDTH : UNIT_DECIMAL_WIDTH));
                case HEXADECIMAL:
                    return toHex(identifier.getValue(),
                        (identifier.isGroup() ? GROUP_HEXADECIMAL_WIDTH : UNIT_HEXADECIMAL_WIDTH));
                default:
                    throw new IllegalArgumentException("Unrecognized integer format: " + format);
            }
        }
        else
        {
            switch(format)
            {
                case DECIMAL:
                    return identifier.getValue().toString();
                case HEXADECIMAL:
                    return toHex(identifier.getValue());
                default:
                    throw new IllegalArgumentException("Unrecognized integer format: " + format);
            }
        }
    }
}
