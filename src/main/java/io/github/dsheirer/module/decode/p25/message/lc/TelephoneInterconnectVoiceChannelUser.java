package io.github.dsheirer.module.decode.p25.message.lc;

import io.github.dsheirer.bits.BinaryMessage;
import io.github.dsheirer.identifier.IIdentifier;
import io.github.dsheirer.identifier.integer.talkgroup.APCO25AnyTalkgroup;
import io.github.dsheirer.module.decode.p25.reference.ServiceOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Current user of an APCO25 channel on both inbound and outbound channels
 */
public class TelephoneInterconnectVoiceChannelUser extends LinkControlWord
{
    private static final int[] RESERVED_1 = {8, 9, 10, 11, 12, 13, 14, 15};
    private static final int[] SERVICE_OPTIONS = {16, 17, 18, 19, 20, 21, 22, 23};
    private static final int[] RESERVED_2 = {24, 25, 26, 27, 28, 29, 30, 31};
    private static final int[] CALL_TIMER = {32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47};
    private static final int[] ADDRESS = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64,
            65, 66, 67, 68, 69, 70, 71};

    private ServiceOptions mServiceOptions;
    private IIdentifier mAddress;
    private List<IIdentifier> mIdentifiers;

    /**
     * Constructs a Link Control Word from the binary message sequence.
     *
     * @param message
     */
    public TelephoneInterconnectVoiceChannelUser(BinaryMessage message)
    {
        super(message);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessageStub());
        sb.append(" ID:").append(getAddress());
        sb.append(" CALL TIMER:").append(getCallTimerDuration()).append("MS");
        sb.append(" ").append(getServiceOptions());

        return sb.toString();
    }

    /**
     * Service Options for this channel
     */
    public ServiceOptions getServiceOptions()
    {
        if(mServiceOptions == null)
        {
            mServiceOptions = new ServiceOptions(getMessage().getInt(SERVICE_OPTIONS));
        }

        return mServiceOptions;
    }

    /**
     * Call timer duration in milliseconds.
     */
    public long getCallTimerDuration()
    {
        //Convert from 100 millisecond intervals to milliseconds.
        return getMessage().getInt(CALL_TIMER) * 100;
    }

    /**
     * To/From radio identifier communicating with a landline
     */
    public IIdentifier getAddress()
    {
        if(mAddress == null)
        {
            mAddress = APCO25AnyTalkgroup.create(getMessage().getInt(ADDRESS));
        }

        return mAddress;
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
            mIdentifiers.add(getAddress());
        }

        return mIdentifiers;
    }
}